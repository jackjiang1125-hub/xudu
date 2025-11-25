package org.jeecg.modules.hkclients.clients;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.jeecg.modules.hkclients.AbstractHkClient;
import org.jeecg.modules.hkclients.dto.HkConn;
import org.jeecg.modules.hkclients.exception.HKClientException;
import org.jeecg.modules.hkclients.http.HikPooledClientManager;
import org.jeecg.modules.hkclients.model.accesscontrol.*;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class HkAccessControlClient extends AbstractHkClient {



    // 单独给 FDLib 表单用一个轻量 mapper（不受 RestTemplate 配置影响）
    private static final ObjectMapper FACE_JSON_MAPPER = new ObjectMapper();

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class FaceDataRecordPayload {

        @JsonProperty("faceLibType")
        private String faceLibType;

        @JsonProperty("FDID")
        private String fdid;

        @JsonProperty("FPID")
        private String fpid;

        @JsonProperty("name")
        private String name;

    }


    public HkAccessControlClient(HikPooledClientManager clientManager) {
        super(clientManager);
    }

    /* ========== 周计划 & 模板 ========== */

    /** PUT 周计划：/ISAPI/AccessControl/UserRightWeekPlanCfg/{id}?format=json */
    public boolean putWeekPlan(HkConn conn, int weekPlanId, UserRightWeekPlanCfg dto) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn,
                "/ISAPI/AccessControl/UserRightWeekPlanCfg/" + weekPlanId + "?format=json");
        ResponseEntity<String> resp = tpl.exchange(
                URI.create(url), HttpMethod.PUT, entityJson(dto), String.class);
        return resp.getStatusCode().is2xxSuccessful();
    }

    /** GET 周计划（DTO）：/ISAPI/AccessControl/UserRightWeekPlanCfg/{id}?format=json */
    public UserRightWeekPlanCfg getUserRightWeekPlan(HkConn conn, int weekPlanId) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn,
                "/ISAPI/AccessControl/UserRightWeekPlanCfg/" + weekPlanId + "?format=json");
        try {
            return tpl.getForObject(url, UserRightWeekPlanCfg.class);
        } catch (HKClientException e) {
            if (e.getHttpStatus() == HttpStatus.NOT_FOUND.value()) {
                url = buildUrl(conn,
                        "/ISAPI/AccessControl/UserRightWeekPlanCfg/" + weekPlanId);
                return tpl.getForObject(url, UserRightWeekPlanCfg.class);
            }
            throw e;
        }
    }

    /** PUT 模板：/ISAPI/AccessControl/UserRightPlanTemplate/{no}?format=json */
    public boolean putUserRightPlanTemplate(HkConn conn, int templateNo, UserRightPlanTemplate dto) {
        RestTemplate tpl = getTemplate(conn);
        dto.setTemplateNo(templateNo);
        String url = buildUrl(conn,
                "/ISAPI/AccessControl/UserRightPlanTemplate/" + templateNo + "?format=json");
        ResponseEntity<String> resp = tpl.exchange(
                URI.create(url), HttpMethod.PUT, entityJson(dto), String.class);
        return resp.getStatusCode().is2xxSuccessful();
    }

    /** GET 模板：/ISAPI/AccessControl/UserRightPlanTemplate/{no}?format=json */
    public UserRightPlanTemplate getUserRightPlanTemplate(HkConn conn, int templateNo) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn,
                "/ISAPI/AccessControl/UserRightPlanTemplate/" + templateNo + "?format=json");
        try {
            return tpl.getForObject(url, UserRightPlanTemplate.class);
        } catch (HKClientException e) {
            if (e.getHttpStatus() == HttpStatus.NOT_FOUND.value()) {
                url = buildUrl(conn,
                        "/ISAPI/AccessControl/UserRightPlanTemplate/" + templateNo);
                return tpl.getForObject(url, UserRightPlanTemplate.class);
            }
            throw e;
        }
    }

    /* ========== 人员 / 卡 ========== */

    /** 新增人员：POST /ISAPI/AccessControl/UserInfo/Record?format=json */
    public boolean postUser(HkConn conn, UserInfo dto) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/AccessControl/UserInfo/Record?format=json");
        ResponseEntity<String> resp = tpl.exchange(
                URI.create(url), HttpMethod.POST, entityJson(dto), String.class);
        return resp.getStatusCode().is2xxSuccessful();
    }

    /** 修改人员：PUT /ISAPI/AccessControl/UserInfo/Modify?format=json */
    public boolean modifyUser(HkConn conn, UserInfo dto) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/AccessControl/UserInfo/Modify?format=json");
        ResponseEntity<String> resp = tpl.exchange(
                URI.create(url), HttpMethod.PUT, entityJson(dto), String.class);
        return resp.getStatusCode().is2xxSuccessful();
    }

    /** 删除人员（按工号）：PUT /ISAPI/AccessControl/UserInfo/Delete?format=json */
    public boolean deleteUser(HkConn conn, String employeeNo) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/AccessControl/UserInfo/Delete?format=json");

        Map<String, Object> cond = Map.of(
                "EmployeeNoList", List.of(Map.of("employeeNo", employeeNo))
        );
        Map<String, Object> body = Map.of("UserInfoDelCond", cond);

        ResponseEntity<String> resp = tpl.exchange(
                URI.create(url), HttpMethod.PUT, entityJson(body), String.class);
        return resp.getStatusCode().is2xxSuccessful();
    }

    /** 下发卡：POST /ISAPI/AccessControl/CardInfo/Record?format=json */
    public boolean postCard(HkConn conn, CardInfo dto) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/AccessControl/CardInfo/Record?format=json");
        ResponseEntity<String> resp = tpl.exchange(
                URI.create(url), HttpMethod.POST, entityJson(dto), String.class);
        return resp.getStatusCode().is2xxSuccessful();
    }

    /** 删除卡：PUT /ISAPI/AccessControl/CardInfo/Delete?format=json */
    public boolean deleteCard(HkConn conn, String employeeNo, String cardNo) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/AccessControl/CardInfo/Delete?format=json");

        Map<String, Object> cond = Map.of(
                "EmployeeNoList", List.of(Map.of("employeeNo", employeeNo)),
                "CardNoList",     List.of(Map.of("cardNo", cardNo))
        );
        Map<String, Object> body = Map.of("CardInfoDelCond", cond);

        ResponseEntity<String> resp = tpl.exchange(
                URI.create(url), HttpMethod.PUT, entityJson(body), String.class);
        return resp.getStatusCode().is2xxSuccessful();
    }



    // ... 其他方法省略

    /**
     * 远程控门（XML 版）：
     * PUT /ISAPI/AccessControl/RemoteControl/door/{doorId}
     *
     * @param doorId 门编号（文档里的 {doorid}）
     * @param cmd    open / close / alwaysOpen / alwaysClose
     */
    public boolean remoteControlDoor(HkConn conn,
                                     int doorId,
                                     RemoteControlDoor.Cmd cmd) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/AccessControl/RemoteControl/door/" + doorId);

        RemoteControlDoor body = new RemoteControlDoor();
        body.setCmd(cmd);

        ResponseEntity<String> resp = tpl.exchange(
                URI.create(url),
                HttpMethod.PUT,
                entityXml(body),          // ★ 自动转成 XML
                String.class
        );
        return resp.getStatusCode().is2xxSuccessful();
    }



    /* ========== 查询：人员 / 卡 ========== */

    /** 按工号查人员：POST /ISAPI/AccessControl/UserInfo/Search?format=json */
    public UserInfoSearchResult searchUserByEmployeeNo(HkConn conn, String employeeNo) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/AccessControl/UserInfo/Search?format=json");

        Map<String, Object> cond = Map.of(
                "searchID", "1",
                "searchResultPosition", 0,
                "maxResults", 20,
                "EmployeeNoList", List.of(Map.of("employeeNo", employeeNo))
        );
        Map<String, Object> body = Map.of("UserInfoSearchCond", cond);

        return tpl.postForObject(url, entityJson(body), UserInfoSearchResult.class);
    }

    /** 多工号批量查询人员 */
    public UserInfoSearchResult searchUsersByEmployeeNos(HkConn conn, List<String> employeeNos) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/AccessControl/UserInfo/Search?format=json");

        List<Map<String, String>> empList = employeeNos.stream()
                .map(no -> Map.of("employeeNo", no))
                .toList();

        Map<String, Object> cond = Map.of(
                "searchID", "u-by-no",
                "searchResultPosition", 0,
                "maxResults", 50,
                "EmployeeNoList", empList
        );
        Map<String, Object> body = Map.of("UserInfoSearchCond", cond);

        return tpl.postForObject(url, entityJson(body), UserInfoSearchResult.class);
    }

    /** 卡信息分页查询：POST /ISAPI/AccessControl/CardInfo/Search?format=json */
    public CardInfoSearchResult searchCardsPage(HkConn conn, int position, int maxResults) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/AccessControl/CardInfo/Search?format=json");

        Map<String, Object> cond = Map.of(
                "searchID", "c-1",
                "searchResultPosition", position,
                "maxResults", maxResults
        );
        Map<String, Object> body = Map.of("CardInfoSearchCond", cond);

        return tpl.postForObject(url, entityJson(body), CardInfoSearchResult.class);
    }

    /* ========== 工具方法：图片、人脸库 ==========
       这些跟根节点无关，基本不用动
    */

    public byte[] getPicByUrl(HkConn conn, String picUrl) {
        RestTemplate tpl = getTemplate(conn);
        String url = picUrl.startsWith("http")
                ? picUrl
                : buildUrl(conn, picUrl.startsWith("/") ? picUrl : ("/" + picUrl));
        ResponseEntity<byte[]> resp = tpl.getForEntity(url, byte[].class);
        return resp.getBody();
    }

    /** 新增人脸（FDLib/FaceDataRecord + multipart），FPID = employeeNo */
    public boolean postFaceFDLibMultipart(HkConn conn,
                                          String employeeNo,
                                          byte[] jpegBytes,
                                          String name) {
        if (jpegBytes == null || jpegBytes.length == 0) {
            throw new HKClientException(400, null, "empty image bytes", null);
        }
        String safeName = (name != null && !name.isEmpty()) ? name : employeeNo;

        FaceDataRecordPayload payload = new FaceDataRecordPayload();
        payload.setFaceLibType("blackFD");  // 如果以后有多个库，可以做成参数
        payload.setFdid("1");               // 同上
        payload.setFpid(employeeNo);
        payload.setName(safeName);

        String recordJson = toFaceJson(payload);

        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/Intelligent/FDLib/FaceDataRecord?format=json");

        HttpEntity<MultiValueMap<String, Object>> req =
                buildFaceMultipartEntity("FaceDataRecord", recordJson, jpegBytes);

        ResponseEntity<String> resp = tpl.exchange(
                URI.create(url), HttpMethod.POST, req, String.class);
        return resp.getStatusCode().is2xxSuccessful();
    }


    /**
     * 修改人脸：PUT /ISAPI/Intelligent/FDLib/FDModify?format=json
     * 这里按你的需求只传最小集字段：faceLibType / FDID / FPID / name
     */
    public boolean modifyFaceFDLibMultipart(HkConn conn,
                                            String fpid,
                                            byte[] jpegBytes,
                                            String name) {
        if (jpegBytes == null || jpegBytes.length == 0) {
            throw new HKClientException(400, null, "empty image bytes", null);
        }
        String safeName = (name != null && !name.isEmpty()) ? name : fpid;

        FaceDataRecordPayload payload = new FaceDataRecordPayload();
        payload.setFaceLibType("blackFD");   // e.g. "blackFD"
        payload.setFdid("1");                 // e.g. "1"
        payload.setFpid(fpid);                 // employeeNo
        payload.setName(safeName);

        String modifyJson = toFaceJson(payload);

        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/Intelligent/FDLib/FDModify?format=json");

        // 注意：这里 JSON 的表单字段名是 "faceURL"
        HttpEntity<MultiValueMap<String, Object>> req =
                buildFaceMultipartEntity("faceURL", modifyJson, jpegBytes);

        ResponseEntity<String> resp = tpl.exchange(
                URI.create(url), HttpMethod.PUT, req, String.class);
        return resp.getStatusCode().is2xxSuccessful();
    }



    private static String toFaceJson(FaceDataRecordPayload payload) {
        try {
            return FACE_JSON_MAPPER.writeValueAsString(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Failed to serialize face payload", e);
        }
    }


    private static HttpEntity<MultiValueMap<String, Object>> buildFaceMultipartEntity(
            String jsonFieldName,
            String json,
            byte[] jpegBytes) {

        // JSON part
        HttpHeaders jsonHeaders = new HttpHeaders();
        jsonHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> jsonPart = new HttpEntity<>(json, jsonHeaders);

        // img part
        HttpHeaders imgHeaders = new HttpHeaders();
        imgHeaders.setContentType(MediaType.IMAGE_JPEG);
        HttpEntity<ByteArrayResource> imgPart = new HttpEntity<>(
                new ByteArrayResource(jpegBytes) {
                    @Override
                    public String getFilename() {
                        return "face.jpg";
                    }
                },
                imgHeaders
        );

        MultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        form.add(jsonFieldName, jsonPart);
        form.add("img", imgPart);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        return new HttpEntity<>(form, headers);
    }

    /**
     * 构造门禁 RTSP 地址:
     *   rtsp://{user}:{pass}@{host}:554/ISAPI/Streaming/channels/{channelId}
     *
     * @param conn       连接参数（里边带 host / user / password）
     * @param channelNo  通道号，一般门禁就是 1
     * @param streamType 码流类型：1=主码流(101)、2=辅码流(102)
     */
    public String buildDoorRtspUrl(HkConn conn, int channelNo, int streamType) {
        int channelId = channelNo * 100 + streamType;  // 1,1 -> 101; 1,2 -> 102

        String user = conn.getUsername();    // 比如 "admin"
        String rawPass = conn.getPassword(); // 比如 "Zkteco@124"

        // ★ 密码里有 @ 等特殊字符，必须 URL 编码，否则会把 host 部分冲掉
        String encPass = UriUtils.encode(rawPass, StandardCharsets.UTF_8);

        return String.format(
                "rtsp://%s:%s@%s:554/ISAPI/Streaming/channels/%d",
                user,
                encPass,
                conn.getHost(),
                channelId
        );
    }

    /**
     * 便捷方法：获取门禁主码流（通常 101）
     */
    public String getDoorMainRtsp(HkConn conn) {
        return buildDoorRtspUrl(conn, 1, 1);   // channelNo=1, streamType=1 -> 101
    }

    /**
     * 配置事件 HTTP 上报主机：
     *   PUT /ISAPI/Event/notification/httpHosts
     *
     * @param localIp       本机对设备可达的 IP（例如 192.168.51.225）
     * @param localPort     本机对设备开放的 HTTP 端口（例如 8080）
     * @param callbackPath  事件回调路径（例如 "/test" 或 "/hk/event"）
     */
    public boolean configHttpEventCallback(HkConn conn,
                                           String localIp,
                                           int localPort,
                                           String callbackPath) {
        RestTemplate tpl = getTemplate(conn);
        if (!callbackPath.startsWith("/")) {
            callbackPath = "/" + callbackPath;
        }

        // ===== 组装 Event 列表 =====
        HttpHostNotificationList.Event evtAccess = new HttpHostNotificationList.Event();
        evtAccess.setType("AccessControllerEvent");
        evtAccess.setMinorAlarm("");
        evtAccess.setMinorException("");
        evtAccess.setMinorOperation("");
        evtAccess.setMinorEvent("");
        evtAccess.setPictureURLType("binary");

        HttpHostNotificationList.Event evtCard = new HttpHostNotificationList.Event();
        evtCard.setType("IDCardInfoEvent");
        evtCard.setMinorAlarm("");
        evtCard.setMinorException("");
        evtCard.setMinorOperation("");
        evtCard.setMinorEvent("");
        evtCard.setPictureURLType("binary");

        HttpHostNotificationList.SubscribeEvent subscribeEvent =
                new HttpHostNotificationList.SubscribeEvent();
        subscribeEvent.setHeartbeat(30);
        subscribeEvent.setEventMode("all");
        subscribeEvent.setEvents(List.of(evtAccess, evtCard));

        // ===== 组装 HttpHostNotification =====
        HttpHostNotificationList.HttpHostNotification host =
                new HttpHostNotificationList.HttpHostNotification();
        host.setId(1); // 通常从 1 开始，你也可以做成参数
        host.setUrl(callbackPath);
        host.setProtocolType("HTTP");
        host.setParameterFormatType("JSON");
        host.setAddressingFormatType("ipaddress");
        host.setIpAddress(localIp);
        host.setPortNo(localPort);
        host.setHttpAuthenticationMethod(""); // 或者 "none"
        host.setSubscribeEvent(subscribeEvent);

        HttpHostNotificationList list = new HttpHostNotificationList();
        list.setHttpHostNotifications(List.of(host));

        String url = buildUrl(conn, "/ISAPI/Event/notification/httpHosts");

        // 用 AbstractHkClient 里已有的 entityXml(...) 发 XML
        ResponseEntity<String> resp = tpl.exchange(
                URI.create(url),
                HttpMethod.PUT,
                entityXml(list),
                String.class
        );
        return resp.getStatusCode().is2xxSuccessful();
    }

}
