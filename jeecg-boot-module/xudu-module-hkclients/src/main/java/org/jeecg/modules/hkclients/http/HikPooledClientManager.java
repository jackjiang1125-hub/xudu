package org.jeecg.modules.hkclients.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class HikPooledClientManager {

    private final PoolingHttpClientConnectionManager cm;
    private final ScheduledExecutorService evictor;
    private final AtomicBoolean started = new AtomicBoolean(false);
    private final ConcurrentHashMap<String, RestTemplate> cache = new ConcurrentHashMap<>();

    public HikPooledClientManager() {
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .build();

        cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(300);
        cm.setDefaultMaxPerRoute(100);
        cm.setValidateAfterInactivity(2000);

        evictor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "hikclient-evictor");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() {
        if (started.compareAndSet(false, true)) {
            evictor.scheduleAtFixedRate(() -> {
                try {
                    cm.closeExpiredConnections();
                    cm.closeIdleConnections(30, TimeUnit.SECONDS);
                } catch (Throwable t) {
                    log.debug("Evictor error", t);
                }
            }, 30, 30, TimeUnit.SECONDS);
        }
    }

    public void shutdown() {
        try { evictor.shutdownNow(); } catch (Exception ignored) {}
        try { cm.shutdown(); } catch (Exception ignored) {}
    }

    private String key(String host, int port, String username, String password, int connectTimeout, int readTimeout) {
        return host + ":" + port + "|" + username + "|" + Objects.hashCode(password) + "|" + connectTimeout + "|" + readTimeout;
    }

    public RestTemplate getOrCreate(String host, int port, String username, String password,
                                    int connectTimeoutMs, int readTimeoutMs) {
        String key = key(host, port, username, password, connectTimeoutMs, readTimeoutMs);
        return cache.computeIfAbsent(key, k -> {
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(cm)
                    .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                    .setDefaultCredentialsProvider(HikRestTemplateFactory.credentials(host, port, username, password))
                    .build();

            RequestConfig rc = RequestConfig.custom()
                    .setConnectTimeout(connectTimeoutMs <= 0 ? 5000 : connectTimeoutMs)
                    .setConnectionRequestTimeout(5000)
                    .setSocketTimeout(readTimeoutMs <= 0 ? 10000 : readTimeoutMs)
                    .build();

            HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
            factory.setConnectTimeout(rc.getConnectTimeout());
            factory.setReadTimeout(rc.getSocketTimeout());

            RestTemplate tpl = new RestTemplate(factory);
            List<HttpMessageConverter<?>> converters = new ArrayList<>();
            converters.add(new Jaxb2RootElementHttpMessageConverter());
            converters.addAll(tpl.getMessageConverters());
            tpl.setMessageConverters(converters);

            start();
            return tpl;
        });
    }
}
