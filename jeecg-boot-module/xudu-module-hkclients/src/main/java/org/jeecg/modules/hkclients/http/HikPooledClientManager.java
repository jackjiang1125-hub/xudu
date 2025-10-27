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
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;

import javax.xml.stream.XMLInputFactory;
import java.nio.charset.StandardCharsets;
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

            // === 用 Jackson-XML 取代 JAXB，并优先匹配 ===
            List<HttpMessageConverter<?>> converters = new ArrayList<>(tpl.getMessageConverters());
            // 去掉 JAXB 转换器（避免命名空间严格校验）
            converters.removeIf(c -> c instanceof Jaxb2RootElementHttpMessageConverter);
            // 加入我们自定义的 Jackson-XML 转换器（忽略命名空间 / 放宽未知字段）
            converters.add(0, jacksonXmlConverter());

            tpl.setMessageConverters(converters);

            start();
            return tpl;
        });
    }

    /** Jackson-XML 转换器：忽略命名空间、放宽未知字段、带常见 XML 媒体类型 */
    private static MappingJackson2XmlHttpMessageConverter jacksonXmlConverter() {
        XmlFactory xmlFactory = new XmlFactory();
        // 关键：关闭命名空间感知，适配不同设备 xmlns（isapi.org / hikvision.com 等）
        xmlFactory.getXMLInputFactory().setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, false);

        XmlMapper xmlMapper = new XmlMapper(xmlFactory);
        xmlMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        xmlMapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true);

        MappingJackson2XmlHttpMessageConverter xmlConv = new MappingJackson2XmlHttpMessageConverter(xmlMapper);
        xmlConv.setSupportedMediaTypes(List.of(
                new MediaType("application","xml"),
                new MediaType("application","xml", StandardCharsets.UTF_8),
                new MediaType("text","xml"),
                new MediaType("text","xml", StandardCharsets.UTF_8)
        ));
        return xmlConv;
    }
}
