package org.jeecg.modules.hkclients.http;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
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
import org.jeecg.modules.hkclients.exception.HkResponseErrorHandler;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.http.converter.xml.MappingJackson2XmlHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

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

    /** 用于解析海康返回的 JSON 错误体 */
    private final ObjectMapper jsonMapper;

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

        // JSON mapper：宽松一些，忽略未知字段
        jsonMapper = new ObjectMapper();
        jsonMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
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

    private String key(String host, int port, String username, String password,
                       int connectTimeout, int readTimeout) {
        return host + ":" + port + "|" + username + "|" + Objects.hashCode(password)
                + "|" + connectTimeout + "|" + readTimeout;
    }

    public RestTemplate getOrCreate(String host, int port, String username, String password,
                                    int connectTimeoutMs, int readTimeoutMs) {
        String key = key(host, port, username, password, connectTimeoutMs, readTimeoutMs);
        return cache.computeIfAbsent(key, k -> {
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(cm)
                    .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy())
                    .setDefaultCredentialsProvider(
                            HikRestTemplateFactory.credentials(host, port, username, password)
                    )
                    .build();

            RequestConfig rc = RequestConfig.custom()
                    .setConnectTimeout(connectTimeoutMs <= 0 ? 5000 : connectTimeoutMs)
                    .setConnectionRequestTimeout(5000)
                    .setSocketTimeout(readTimeoutMs <= 0 ? 10000 : readTimeoutMs)
                    .build();

            HttpComponentsClientHttpRequestFactory factory =
                    new HttpComponentsClientHttpRequestFactory(httpClient);
            factory.setConnectTimeout(rc.getConnectTimeout());
            factory.setReadTimeout(rc.getSocketTimeout());

            RestTemplate tpl = new RestTemplate(factory);

            // === 配置消息转换器 ===
            List<HttpMessageConverter<?>> converters = new ArrayList<>();
            for (HttpMessageConverter<?> c : tpl.getMessageConverters()) {
                // 不要默认 JAXB 和默认 Jackson-XML，我们自己放一个 Jackson-XML
                if (c instanceof Jaxb2RootElementHttpMessageConverter) {
                    continue;
                }
                if (c instanceof MappingJackson2XmlHttpMessageConverter) {
                    continue;
                }
                if(c instanceof MappingJackson2HttpMessageConverter) {
                    continue;
                }
                converters.add(c);
            }
            // 最前面放我们自定义的 XML 转换器（忽略 namespace 等）
            converters.add(0, jacksonXmlConverter());
            converters.add(hikJsonConverter());
            // 默认 JSON 转换器（MappingJackson2HttpMessageConverter）保留在列表中

            tpl.setMessageConverters(converters);

            // === 统一错误处理：4xx / 5xx 自动转 HKClientException ===
            tpl.setErrorHandler(new HkResponseErrorHandler(jsonMapper));

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

        MappingJackson2XmlHttpMessageConverter xmlConv =
                new MappingJackson2XmlHttpMessageConverter(xmlMapper);
        xmlConv.setSupportedMediaTypes(List.of(
                new MediaType("application", "xml"),
                new MediaType("application", "xml", StandardCharsets.UTF_8),
                new MediaType("text", "xml"),
                new MediaType("text", "xml", StandardCharsets.UTF_8)
        ));
        return xmlConv;
    }

    private static MappingJackson2HttpMessageConverter hikJsonConverter() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        // ★ 关键两行：根节点自动包裹/拆掉
        mapper.enable(SerializationFeature.WRAP_ROOT_VALUE);
        mapper.enable(DeserializationFeature.UNWRAP_ROOT_VALUE);

        MappingJackson2HttpMessageConverter jsonConv =
                new MappingJackson2HttpMessageConverter(mapper);

        jsonConv.setSupportedMediaTypes(List.of(
                MediaType.APPLICATION_JSON,
                new MediaType("application", "json", StandardCharsets.UTF_8)
        ));
        return jsonConv;
    }

}
