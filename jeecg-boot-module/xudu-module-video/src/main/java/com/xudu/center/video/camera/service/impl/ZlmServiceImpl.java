// ZlmService.java
package com.xudu.center.video.camera.service.impl;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import com.xudu.center.video.camera.service.IZlmService;
import com.xudu.center.video.camera.vo.CameraVO;
import com.xudu.center.video.camera.vo.PlayUrlVO;
import com.xudu.center.video.camera.vo.ZlmApiResp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@Slf4j
public class ZlmServiceImpl implements IZlmService {

    @Autowired
    private  RestTemplate restTemplate;

//    @Autowired
//    private  ZlmProperties props;

//    @Autowired
//    private CameraService cameraService;


    /**
     * 调用 ZLM /index/api/addStreamProxy 注册一路 RTSP
     */

    /** JSON 方式 addStreamProxy；部分老版本若返回 415，会自动回退为表单提交 */
    public ZlmApiResp addStreamProxyJson(String api, String stream, String rtspUrl) {
        //String api = baseUrl + "/index/api/addStreamProxy";
        ZlmApiResp resp = null;
        Map<String, Object> body = new LinkedHashMap<>();
//        body.put("secret", props.getSecret());
//        body.put("vhost", "__defaultVhost__");
//        body.put("app", props.getApp());
//        body.put("stream", stream);
//        body.put("url", rtspUrl);     // e.g. rtsp://.../Streaming/Channels/102?...
//        body.put("rtp_type", 0);                      // 0=tcp（更稳）；1=udp
//        body.put("enable_hls", 1);
        // 可选项按需加：retry_count、enable_hls/rtsp/rtmp/ts/fmp4、*_demand 等
        // body.put("retry_count", -1);

        String json;
        try {
            json = new ObjectMapper().writeValueAsString(body);
        } catch (Exception e) {
            throw new IllegalStateException("序列化 JSON 失败", e);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        headers.setContentLength(bytes.length);

        try {
            resp = restTemplate.postForObject(api, new HttpEntity<>(body, headers), ZlmApiResp.class);
        } catch (HttpClientErrorException.UnsupportedMediaType e) {
            // 老版本 ZLM 仅支持 x-www-form-urlencoded；回退一次
          //  return addStreamProxyForm(app, stream, rtspUrl);
        }
        return resp;
    }

    /** 在 rtsp(s):// 中嵌入 user:pass@ ，并对 user/pass 做百分号编码（避免特殊字符破坏 URL） */
    private static String embedUserInfo(String rtspUrl, String user, String pass) {
        // 粗暴但实用的 RFC3986 userinfo 编码：保留字母数字，其他做 %XX
        java.util.function.Function<String,String> enc = s -> {
            StringBuilder sb = new StringBuilder();
            for (char c : s.toCharArray()) {
                if (Character.isLetterOrDigit(c) || c=='-'||c=='_'||c=='.'||c=='~') sb.append(c);
                else sb.append('%').append(String.format("%02X", (int)c));
            }
            return sb.toString();
        };
        String ui = enc.apply(user) + ":" + enc.apply(pass) + "@";
        // 插入到 scheme:// 后面
        int p = rtspUrl.indexOf("://");
        if (p > 0) return rtspUrl.substring(0, p+3) + ui + rtspUrl.substring(p+3);
        // 没有 scheme 时，直接前缀
        return "rtsp://" + ui + rtspUrl;
    }


    @Override
    public PlayUrlVO addStreamProxy(CameraVO req) {
        Assert.hasText(req.getStreamId(), "streamId 不能为空");
        Assert.hasText(req.getRtspUrl(), "rtspUrl 不能为空");




//
//        String api = props.getBaseUrl().replaceAll("/+$","") + "/index/api/addStreamProxy";
//
//        String rtspUrl = embedUserInfo(req.getRtspUrl(), req.getUsername(), req.getPassword());
//
//
//        ZlmApiResp resp = addStreamProxyJson(api, req.getStreamId(), rtspUrl);
//        if (resp == null || resp.getCode() != 0) {
//            String msg = (resp == null) ? "null response" : resp.getMsg();
//            throw new IllegalStateException("ZLM addStreamProxy 失败: " + msg);
//        }
//
//        // 组装播放地址（按 ZLM 默认 URL 规则）
//        String host = props.getBaseUrl().replaceAll("/+$","");
//        String app = props.getApp();
//        String stream = req.getStreamId();
//
//        String hls = host + "/" + app + "/" + stream + "/hls.m3u8";
//        String flv = host + "/" + app + "/" + stream + ".live.flv";
//        String webrtcApi = host + "/index/api/webrtc?app=" + app + "&stream=" + stream + "&type=play";
//
//        return new PlayUrlVO(app, stream, hls, flv, webrtcApi);
        return null;
    }

    @Override
    public void removeStreamProxy(String streamId) {

    }
}
