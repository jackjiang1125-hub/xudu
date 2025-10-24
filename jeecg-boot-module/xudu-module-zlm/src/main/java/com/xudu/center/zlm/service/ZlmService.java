package com.xudu.center.zlm.service;

import com.xudu.center.zlm.api.IZlmService;
import com.xudu.center.zlm.client.ZlmClient;
import com.xudu.center.zlm.config.ZlmProperties;
import com.xudu.center.zlm.dto.*;
import com.xudu.center.zlm.model.ZlmResponse;
import com.xudu.center.zlm.params.Audio;
import com.xudu.center.zlm.params.Video;
import com.xudu.center.zlm.selector.FfmpegTemplateSelector;
import com.xudu.center.zlm.support.ZlmMediaInspector;
import com.xudu.center.zlm.util.NamingUtil;
import com.xudu.center.zlm.util.SchemaUtil;
import com.xudu.center.zlm.util.Waiter;
import com.xudu.center.zlm.util.ZlmUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ZlmService implements IZlmService {

    private final ZlmClient client;
    private final ZlmProperties props;
    private final ZlmMediaInspector inspector;
    private final FfmpegTemplateSelector ffSelector;

    // -------- 基础能力 --------
    @Override
    public ZlmResponse<Object> addProxy(String schema, String app, String stream, String url, Integer rtpType, Boolean closeWhenNoConsumer) {
        return client.addStreamProxy(schema, app, stream, url, rtpType, closeWhenNoConsumer);
    }

    @Override
    public CodecInfo probeCodec(String app, String stream) {
        return inspector.probeCodec(app, stream);
    }

    @Override
    public boolean isBrowserFriendly(CodecInfo c, String playMode){
        boolean hlsFriendly    = (c.videoKind()==Video.H264) && (c.audioKind()==Audio.AAC || c.audioKind()==Audio.NONE);
        boolean webrtcFriendly = (c.videoKind()==Video.H264 || c.videoKind()==Video.VP8 || c.videoKind()==Video.VP9 || c.videoKind()==Video.AV1)
                && (c.audioKind()==Audio.OPUS || c.audioKind()==Audio.AAC || c.audioKind()==Audio.NONE);
        return "WEBRTC".equalsIgnoreCase(playMode) ? webrtcFriendly : hlsFriendly;
    }

    // -------- 规范化（仅当不友好时转码）--------
    @Override
    public NormalizeResult normalizeForBrowser(NormalizeReq req){
        CodecInfo ci = inspector.probeCodec(req.getApp(), req.getStream());
        if (isBrowserFriendly(ci, req.getPlayMode())) {
            return NormalizeResult.builder()
                    .browserFriendly(true)
                    .app(req.getApp()).stream(req.getStream())
                    .urls(ZlmUrls.publicPlayUrls(props, req.getApp(), req.getStream()))
                    .videoCodec(ci.videoRaw()).audioCodec(ci.audioRaw())
                    .build();
        }

        String ffKey  = ffSelector.select(ci, req.isPreferNvenc());
        String outApp = req.getTransApp();
        String outSt  = NamingUtil.chooseOutStreamName(req.getNaming(), req.getCustomStream(), req.getStream(), ci);

        if (!inspector.streamExists(outApp, outSt)) {
            String srcUrl = ZlmUrls.internalRtspRead(props, req.getApp(), req.getStream());
            String dstUrl = ZlmUrls.internalRtmpPublish(props, outApp, outSt);
            ZlmResponse<Map> r = client.addFFmpegSource(ffKey, srcUrl, dstUrl, 15000);
            if (!r.ok()) throw new RuntimeException("addFFmpegSource failed: " + r.getMsg());
        }

        return NormalizeResult.builder()
                .browserFriendly(false)
                .app(outApp).stream(outSt)
                .urls(ZlmUrls.publicPlayUrls(props, outApp, outSt))
                .ffmpegCmdKey(ffKey)
                .videoCodec(ci.videoRaw()).audioCodec(ci.audioRaw())
                .build();
    }

    // -------- 一步到位：addProxy + normalize --------
    @Override
    public NormalizeResult addProxyAndNormalize(ProxyAndNormalizeReq req) {
        final String schema = (req.getSchema() == null || req.getSchema().isBlank())
                ? SchemaUtil.inferFromUrl(req.getUrl()) : req.getSchema();

        if (!inspector.streamExists(req.getApp(), req.getStream())) {
            ZlmResponse<Object> add = client.addStreamProxy(schema, req.getApp(), req.getStream(), req.getUrl(),
                    req.getRtpType(), req.getCloseWhenNoConsumer());
            if (!add.ok()) throw new RuntimeException("addStreamProxy failed: " + add.getMsg());
        }

        // 等源流就绪
        Waiter.waitOrThrow(
                () -> inspector.streamExists(req.getApp(), req.getStream()),
                req.getWaitReadyMs(), req.getPollIntervalMs(),
                "stream not ready in time: " + req.getApp() + "/" + req.getStream()
        );

        // 规范化
        NormalizeReq nreq = new NormalizeReq();
        nreq.setApp(req.getApp());
        nreq.setStream(req.getStream());
        nreq.setTransApp(req.getTransApp());
        nreq.setNaming(req.getNaming());
        nreq.setCustomStream(req.getCustomStream());
        nreq.setPreferNvenc(req.isPreferNvenc());
        nreq.setPlayMode(req.getPlayMode());
        return normalizeForBrowser(nreq);
    }
}
