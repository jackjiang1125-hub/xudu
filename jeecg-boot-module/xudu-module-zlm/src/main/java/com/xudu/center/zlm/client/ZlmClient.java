package com.xudu.center.zlm.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xudu.center.zlm.config.ZlmProperties;
import com.xudu.center.zlm.model.ZlmResponse;
import com.xudu.center.zlm.util.ZlmQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class ZlmClient {

    private final RestTemplate zlmRestTemplate;
    private final ZlmProperties props;
    private final ObjectMapper om = new ObjectMapper();

    private <T> ZlmResponse<T> get(String path, Map<String, ?> q, Class<T> dataType) {
        URI uri = ZlmQuery.build(props.getBaseUrl(), path, props.getSecret(), q);
        ResponseEntity<String> resp = zlmRestTemplate.exchange(uri, HttpMethod.GET, HttpEntity.EMPTY, String.class);
        try {
            // 先读成通用结构再映射 data
            ZlmResponse<?> base = om.readValue(resp.getBody(), new TypeReference<ZlmResponse<Object>>() {});
            ZlmResponse<T> out = new ZlmResponse<>();
            out.setCode(base.getCode());
            out.setMsg(base.getMsg());
            if (base.getCode() == 0 && base.getData() != null) {
                String dataJson = om.writeValueAsString(base.getData());
                T data = om.readValue(dataJson, dataType);
                out.setData(data);
            }
            return out;
        } catch (Exception e) {
            throw new RuntimeException("Parse ZLM response failed: " + e.getMessage() + ", raw=" + resp.getBody(), e);
        }
    }

    public ZlmResponse<Object> addStreamProxy(String schema, String app, String stream, String url,
                                              Integer rtpType, Boolean closeWhenNoConsumer) {
        return get("/index/api/addStreamProxy", Map.of(
            "schema", schema, "app", app, "stream", stream,
            "url", url, "rtp_type", rtpType == null ? 1 : rtpType,
            "close_when_no_consumer", closeWhenNoConsumer == null ? 1 : (closeWhenNoConsumer ? 1 : 0)
        ), Object.class);
    }

    public ZlmResponse<Object> getMediaList(String app, String stream) {
        return get("/index/api/getMediaList", Map.of("app", app, "stream", stream), Object.class);
    }

    public ZlmResponse<Map> addFFmpegSource(String ffmpegCmdKey, String srcUrl, String dstUrl, Integer timeoutMs) {
        return get("/index/api/addFFmpegSource", Map.of(
            "ffmpeg_cmd_key", ffmpegCmdKey,
            "src_url", srcUrl,
            "dst_url", dstUrl,
            "timeout_ms", timeoutMs == null ? 15000 : timeoutMs
        ), Map.class);
    }

    public ZlmResponse<Object> delFFmpegSource(String key) {
        return get("/index/api/delFFmpegSource", Map.of("key", key), Object.class);
    }

    public ZlmResponse<Map> addStreamPusherProxy(String schema, String app, String stream, String dstUrl) {
        return get("/index/api/addStreamPusherProxy", Map.of(
            "schema", schema, "vhost", "__defaultVhost__", "app", app, "stream", stream,
            "dst_url", dstUrl
        ), Map.class);
    }

    public ZlmResponse<Object> delStreamPusherProxy(String key) {
        return get("/index/api/delStreamPusherProxy", Map.of("key", key), Object.class);
    }

    public ZlmResponse<Object> closeStreams(String app, String stream) {
        return get("/index/api/close_streams", Map.of("app", app, "stream", stream), Object.class);
    }
}
