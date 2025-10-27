// service/OnDemandPlayService.java
package com.xudu.center.zlm.service;

import com.xudu.center.video.api.IVideoService;
import com.xudu.center.video.vo.VideoVO;
import com.xudu.center.zlm.api.IOnDemandPlayService;
import com.xudu.center.zlm.client.ZlmClient;
import com.xudu.center.zlm.config.ZlmPlayProperties;
import com.xudu.center.zlm.config.ZlmProperties;
import com.xudu.center.zlm.constants.PlayTarget;
import com.xudu.center.zlm.dto.CodecInfo;
import com.xudu.center.zlm.dto.NormalizeResult;
import com.xudu.center.zlm.dto.ProxyAndNormalizeReq;
import com.xudu.center.zlm.model.ZlmResponse;
import com.xudu.center.zlm.support.ZlmMediaInspector;
import com.xudu.center.zlm.util.InflightLocks;
import com.xudu.center.zlm.util.Waiter;
import com.xudu.center.zlm.util.ZlmUrls;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OnDemandPlayService implements IOnDemandPlayService {

  private final IVideoService videoService;
  private final ZlmService zlmService;           // 你已有的业务服务
  private final ZlmMediaInspector inspector;
  private final ZlmClient client;
  private final ZlmPlayProperties playProps;
  private final ZlmProperties zlmProperties;

  /** 前端基于 cameraId 发起准备：返回可播放的 URLs（HLS/FLV/RTSP/WHEP） */
  @Override
  public NormalizeResult prepareByCamera(String cameraId, PlayTarget target, boolean preferNvenc) {

      VideoVO videoVO = videoService.getById(cameraId);

      if (videoVO == null) throw new RuntimeException("camera not found: " + cameraId);
    String srcApp   = playProps.getSrcApp();
    String publicApp= playProps.getPublicApp();
    String stream   = videoVO.getStream();

    String lockKey = publicApp + "/" + stream;
    InflightLocks.acquire(lockKey);



    try {
      // 1) 若公共路径已存在，直接返回
      if (inspector.streamExists(publicApp, stream)) {
        return NormalizeResult.builder()
            .browserFriendly(true)
            .app(publicApp).stream(stream)
            .urls(com.xudu.center.zlm.util.ZlmUrls.publicPlayUrls(zlmProperties, publicApp, stream))
            .build();
      }

      // 2) 先把相机拉到 src/app
      if (!inspector.streamExists(srcApp, stream)) {
        zlmService.addProxy("rtsp", srcApp, stream, videoVO.getRtspUrl(), 0, true); // rtp_type=0 tcp, auto_close=true
        Waiter.waitOrThrow(
            () -> inspector.streamExists(srcApp, stream),
            playProps.getWaitReadyMs(), playProps.getPollIntervalMs(),
            "src stream not ready: " + srcApp + "/" + stream
        );
      }

      // 3) 判定是否浏览器友好
      CodecInfo ci = zlmService.probeCodec(srcApp, stream);

      boolean friendly = zlmService.isBrowserFriendly(ci, (target == PlayTarget.WEBRTC || target == PlayTarget.PC)? "WEBRTC" : "HLS_FLV");

        NormalizeResult out = null;

      if (friendly) {
        switch (playProps.getPromoteStrategy().toLowerCase()) {
          case "direct": // 直接用 src
            out =  NormalizeResult.builder()
                .browserFriendly(true)
                .app(srcApp).stream(stream)
                .urls(ZlmUrls.publicPlayUrls(zlmProperties, srcApp, stream))
                .videoCodec(ci.videoRaw()).audioCodec(ci.audioRaw())
                .build();
            break;

          case "ffcopy": { // FFmpeg -c copy，从 src → public
            if (!inspector.streamExists(publicApp, stream)) {
              String srcUrl = ZlmUrls.internalRtspRead(zlmProperties, srcApp, stream);
              String dstUrl = ZlmUrls.internalRtmpPublish(zlmProperties, publicApp, stream);
              ZlmResponse<Map> r = client.addFFmpegSource(playProps.getFfmpegCopyKey(), srcUrl, dstUrl, 15000);
              if (!r.ok()) throw new RuntimeException("ffcopy failed: " + r.getMsg());
            }
            Waiter.waitOrThrow(
                () -> inspector.streamExists(publicApp, stream),
                playProps.getWaitReadyMs(), playProps.getPollIntervalMs(),
                "public stream not ready: " + publicApp + "/" + stream
            );
            out =  NormalizeResult.builder()
                .browserFriendly(true)
                .app(publicApp).stream(stream)
                .urls(ZlmUrls.publicPlayUrls(zlmProperties, publicApp, stream))
                .videoCodec(ci.videoRaw()).audioCodec(ci.audioRaw())
                .build();
            break;
          }

          case "reproxy":
          default: { // 关闭 src，直接把相机拉到 public（最干净）
            client.closeStreams(srcApp, stream); // force close
            zlmService.addProxy("rtsp", publicApp, stream, videoVO.getRtspUrl(), 0, true);
            Waiter.waitOrThrow(
                () -> inspector.streamExists(publicApp, stream),
                playProps.getWaitReadyMs(), playProps.getPollIntervalMs(),
                "public stream not ready: " + publicApp + "/" + stream
            );
            out =  NormalizeResult.builder()
                .browserFriendly(true)
                .app(publicApp).stream(stream)
                .urls(ZlmUrls.publicPlayUrls(zlmProperties, publicApp, stream))
                .videoCodec(ci.videoRaw()).audioCodec(ci.audioRaw())
                .build();
          }
        }
      }else{
          // 4) 不友好 → 走规范化（用你已有的一步流）
          ProxyAndNormalizeReq req = new ProxyAndNormalizeReq();
          req.setSchema("rtsp");
          req.setApp(srcApp);
          req.setStream(stream);
          req.setUrl(videoVO.getRtspUrl());
          req.setTransApp(publicApp);
          req.setNaming("custom");
          req.setCustomStream(stream); // 让输出就是 publicApp/stream
          req.setPlayMode(target == PlayTarget.WEBRTC ? "WEBRTC" : "HLS_FLV");
          req.setPreferNvenc(preferNvenc);
          req.setRtpType(0);
          req.setCloseWhenNoConsumer(true);
          req.setWaitReadyMs(playProps.getWaitReadyMs());
          req.setPollIntervalMs(playProps.getPollIntervalMs());
          out = zlmService.addProxyAndNormalize(req);

      }

        videoVO.setAudioCodec(ci.audioRaw());//更新音频编码
        videoVO.setVideoCodec(ci.videoRaw());//更新视频编码
        //更新ffmpeg的key、hls、webrtc播放路径
        videoVO.setFfmpegCmdKey(out.getFfmpegCmdKey());
        videoVO.setHlsUrl(out.getUrls().getHls());
        videoVO.setWebRtcUrl(out.getUrls().getWhep());
        videoService.updateVideo(videoVO);
        return out;
    } finally {
      InflightLocks.release(lockKey);
    }
  }

  /** （可选）按 app/stream 准备——给 on_stream_not_found 用 */
  public void prepareByAppStream(String app, String stream) {
    VideoVO videoVO = videoService.findByAppStream(app, stream);
    if (videoVO == null) return; // 找不到就让这次失败
    // 默认按 PC/HLS 处理；需要更细可在 hook query param 上带 profile=mobile/webrtc
    prepareByCamera(videoVO.getId(), PlayTarget.PC, false);
  }
}
