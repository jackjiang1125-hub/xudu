// HttpHostNotification.java
package org.jeecg.modules.hkclients.model.accesscontrol;
import lombok.Data;

@Data
public class HttpHostNotification {
    private Integer id;                     // 与路径一致
    private String url = "/isapi/notify";   // 设备回调 URL（相对路径)
    private String protocolType = "HTTP";
    private String parameterFormatType = "JSON";
    private String addressingFormatType = "ipaddress";
    private String ipAddress;               // 你的后端 IP
    private Integer portNo;                 // 你的后端端口
    private String httpAuthenticationMethod = "none"; // 或 basic/digest
    // 可选心跳/事件模式（部分固件支持）
    private SubscribeEvent SubscribeEvent;

    @Data
    public static class SubscribeEvent {
        private Integer heartbeat = 30;
        private String eventMode = "all";   // all / custom
    }
}
