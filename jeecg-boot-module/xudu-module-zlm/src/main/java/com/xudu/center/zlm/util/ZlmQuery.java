package com.xudu.center.zlm.util;

import org.springframework.web.util.UriComponentsBuilder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ZlmQuery {
    public static URI build(String base, String path, String secret, Map<String, ?> params) {
        UriComponentsBuilder b = UriComponentsBuilder.fromHttpUrl(base + path)
            .queryParam("secret", secret);
        if (params != null) {
            params.forEach((k, v) -> {
                if (v != null) b.queryParam(k, v.toString());
            });
        }
        return b.build(true).encode(StandardCharsets.UTF_8).toUri();
    }
}
