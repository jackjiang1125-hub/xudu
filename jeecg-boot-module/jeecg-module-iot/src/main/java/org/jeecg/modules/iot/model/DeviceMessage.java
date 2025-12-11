package org.jeecg.modules.iot.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.Map;

/**
 * Represents a message sent by a connected device through the HTTP based private protocol.
 * <p>
 * 统一封装：
 * - 基本信息：uri/method/headers/body/path/query/clientIp/contentType
 * - 表单字段：formParameters（针对 application/x-www-form-urlencoded、multipart/form-data）
 * - 文件字段：fileParameters（针对 multipart/form-data，例如海康 Picture）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceMessage {

    /** 原始请求 URI，例如 /event/record?devIndex=xxx */
    private String uri;

    /** HTTP 方法，例如 GET/POST */
    private String method;

    /** HTTP 请求头（只读视图，见 getter） */
    @Builder.Default
    private Map<String, String> headers = Collections.emptyMap();

    /**
     * 原始 payload 文本内容。
     * - 对于普通 text/plain / application/json：这里就是完整 body 字符串
     * - 对于 multipart/form-data：建议填 event_log 或者空字符串（真正的字段放 formParameters/fileParameters 中）
     */
    private String payload;

    /** 去掉 query string 的请求路径，例如 /iclock/cdata、/event/record */
    private String path;

    /** URL 查询参数（?sn=xxx&table=rtlog），key -> 第一个值 */
    @Builder.Default
    private Map<String, String> queryParameters = Collections.emptyMap();

    /** 客户端 IP 地址 */
    private String clientIp;

    /** Content-Type 原值，例如 application/json; charset=utf-8、multipart/form-data; boundary=xxx */
    private String contentType;

    // ================== 新增：为海康等 multipart 请求服务 ==================

    /**
     * 表单字段：
     * - 对于 application/x-www-form-urlencoded：这里放所有字段
     * - 对于 multipart/form-data：这里放所有文本字段（如 event_log）
     */
    @Builder.Default
    private Map<String, String> formParameters = Collections.emptyMap();

    /**
     * 文件字段：
     * - 对于 multipart/form-data：这里放所有文件，如 Picture
     * - key 为表单字段名，例如 "Picture"
     */
    @Builder.Default
    private Map<String, DeviceFilePart> fileParameters = Collections.emptyMap();

    // ================== 安全 Getter：避免 NPE & 保护内部 Map ==================

    public Map<String, String> getHeaders() {
        if (headers == null || headers.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(headers);
    }

    public Map<String, String> getQueryParameters() {
        if (queryParameters == null || queryParameters.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(queryParameters);
    }

    public Map<String, String> getFormParameters() {
        if (formParameters == null || formParameters.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(formParameters);
    }

    public Map<String, DeviceFilePart> getFileParameters() {
        if (fileParameters == null || fileParameters.isEmpty()) {
            return Collections.emptyMap();
        }
        return Collections.unmodifiableMap(fileParameters);
    }

    // ================== 小工具方法（可选，用起来更顺手） ==================

    /**
     * 先从 query 中找，再从 form 中找第一个字段值。
     * 适合设备既可能放在 query 也可能放在 form 里的情况（比如 sn）。
     */
    public String getParam(String name) {
        if (name == null) {
            return null;
        }
        String v = getQueryParameters().get(name);
        if (v != null) {
            return v;
        }
        return getFormParameters().get(name);
    }

    /**
     * 仅从 formParameters 中取字段，例如 event_log。
     */
    public String getFormParam(String name) {
        if (name == null) {
            return null;
        }
        return getFormParameters().get(name);
    }

    /**
     * 获取上传的文件字段，例如 Picture。
     */
    public DeviceFilePart getFile(String name) {
        if (name == null) {
            return null;
        }
        return getFileParameters().get(name);
    }

    /**
     * 原始二进制数据 (针对非HTTP协议)
     */
    private byte[] rawBody;
}
