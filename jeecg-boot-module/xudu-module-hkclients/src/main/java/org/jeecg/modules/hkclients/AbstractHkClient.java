package org.jeecg.modules.hkclients;

import org.jeecg.modules.hkclients.dto.HkConn;
import org.jeecg.modules.hkclients.http.HikPooledClientManager;
import org.jeecg.modules.hkclients.model.system.DeviceInfo;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public abstract class AbstractHkClient {

    protected static final String XML_CT  = "application/xml; charset=UTF-8";
    protected static final String JSON_CT = "application/json; charset=UTF-8";

    protected final HikPooledClientManager clientManager;

    protected AbstractHkClient(HikPooledClientManager clientManager) {
        this.clientManager = clientManager;
    }

    /** 统一获取带连接池 + 认证的 RestTemplate */
    protected RestTemplate getTemplate(HkConn conn) {
        return clientManager.getOrCreate(
                conn.getHost(),
                conn.getPort(),
                conn.getUsername(),
                conn.getPassword(),
                conn.getConnectTimeoutMs(),
                conn.getReadTimeoutMs()
        );
    }

    /** 统一拼 URL，按你之前的 buildUrl 逻辑 */
    protected String buildUrl(HkConn conn, String path) {
        if (path == null || path.isEmpty()) {
            path = "/";
        }
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return conn.baseUrl() + path;
    }

    /** XML HttpEntity（NVR 那些 XML ISAPI 用这个） */
    protected <T> HttpEntity<T> entityXml(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", XML_CT);
        headers.set("Content-Type", XML_CT);
        return body == null ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
    }

    /** JSON HttpEntity（简单 JSON） */
    protected <T> HttpEntity<T> entityJson(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", JSON_CT);
        headers.set("Content-Type", JSON_CT);
        return body == null ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
    }

    /** JSON HttpEntity，外面包一层 root，比如 {"UserInfo": {...}} */
    protected <T> HttpEntity<Map<String, T>> entityJsonWrapped(String root, T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", JSON_CT);
        headers.set("Content-Type", JSON_CT);
        return new HttpEntity<>(Map.of(root, body), headers);
    }

    /* ===== 通用能力：设备信息 ===== */

    /** 通用设备信息（NVR、K1T 都能用）：GET /ISAPI/System/deviceInfo */
    public DeviceInfo getDeviceInfo(HkConn conn) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/System/deviceInfo");
        return tpl.exchange(URI.create(url), HttpMethod.GET,
                entityXml(null), DeviceInfo.class
        ).getBody();
    }

    /** 调试用：获取 /ISAPI/System/deviceInfo 原始 XML 字符串 */
    public String getDeviceInfoRaw(HkConn conn) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/System/deviceInfo");
        return tpl.getForObject(url, String.class);
    }

    /** 如果你想完全绕开 converter，看最原始响应，可以用这个 */
    public String getDeviceInfoRawDebug(HkConn conn) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/System/deviceInfo");

        return tpl.execute(URI.create(url), HttpMethod.GET,
                request -> request.getHeaders().set("Accept", XML_CT),
                response -> StreamUtils.copyToString(
                        response.getBody(),
                        StandardCharsets.UTF_8
                )
        );
    }
}
