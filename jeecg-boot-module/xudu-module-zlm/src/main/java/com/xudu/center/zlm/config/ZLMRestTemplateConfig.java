package com.xudu.center.zlm.config;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class ZLMRestTemplateConfig {

    @Bean
    public HttpClientConnectionManager zlmConnManager(ZlmProperties p) {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        cm.setMaxTotal(p.getMaxTotal());
        cm.setDefaultMaxPerRoute(p.getDefaultMaxPerRoute());
        return cm;
    }

    @Bean
    public CloseableHttpClient zlmHttpClient(HttpClientConnectionManager cm, ZlmProperties p) {
        RequestConfig rc = RequestConfig.custom()
            .setConnectTimeout(p.getConnectTimeoutMs())
            .setConnectionRequestTimeout(p.getConnectTimeoutMs())
            .setSocketTimeout(p.getReadTimeoutMs())
            .build();
        return HttpClients.custom()
            .setConnectionManager(cm)
            .setDefaultRequestConfig(rc)
            .evictIdleConnections(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();
    }

    @Bean("zlmRestTemplate")
    public RestTemplate zlmRestTemplate(CloseableHttpClient httpClient, ZlmProperties p) {
        HttpComponentsClientHttpRequestFactory f = new HttpComponentsClientHttpRequestFactory(httpClient);
        f.setConnectTimeout(p.getConnectTimeoutMs());
        f.setReadTimeout(p.getReadTimeoutMs());
        return new RestTemplateBuilder().requestFactory(() -> f).build();
    }
}
