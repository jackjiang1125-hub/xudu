package org.jeecg.modules.hkclients.model.accesscontrol;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

/**
 * 对应：
 * <HttpHostNotificationList version="2.0" xmlns="http://www.isapi.org/ver20/XMLSchema">
 *   <HttpHostNotification>...</HttpHostNotification>
 * </HttpHostNotificationList>
 */
@Data
@JacksonXmlRootElement(localName = "HttpHostNotificationList",
        namespace = HttpHostNotificationList.HK_NS)
public class HttpHostNotificationList {

    public static final String HK_NS = "http://www.isapi.org/ver20/XMLSchema";

    @JacksonXmlProperty(isAttribute = true, localName = "version")
    private String version = "2.0";

    // 允许有多个 <HttpHostNotification>，所以用 List；useWrapping=false 表示不再包一层数组标签
    @JacksonXmlProperty(localName = "HttpHostNotification", namespace = HK_NS)
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<HttpHostNotification> httpHostNotifications;

    @Data
    public static class HttpHostNotification {

        @JacksonXmlProperty(localName = "id", namespace = HK_NS)
        private Integer id;

        @JacksonXmlProperty(localName = "url", namespace = HK_NS)
        private String url;

        @JacksonXmlProperty(localName = "protocolType", namespace = HK_NS)
        private String protocolType; // HTTP

        @JacksonXmlProperty(localName = "parameterFormatType", namespace = HK_NS)
        private String parameterFormatType; // JSON

        @JacksonXmlProperty(localName = "addressingFormatType", namespace = HK_NS)
        private String addressingFormatType; // ipaddress

        @JacksonXmlProperty(localName = "ipAddress", namespace = HK_NS)
        private String ipAddress;

        @JacksonXmlProperty(localName = "portNo", namespace = HK_NS)
        private Integer portNo;

        @JacksonXmlProperty(localName = "httpAuthenticationMethod", namespace = HK_NS)
        private String httpAuthenticationMethod; // none / 空字符串

        @JacksonXmlProperty(localName = "SubscribeEvent", namespace = HK_NS)
        private SubscribeEvent subscribeEvent;
    }

    @Data
    public static class SubscribeEvent {

        @JacksonXmlProperty(localName = "heartbeat", namespace = HK_NS)
        private Integer heartbeat; // 30

        @JacksonXmlProperty(localName = "eventMode", namespace = HK_NS)
        private String eventMode; // all / 手动选

        @JacksonXmlProperty(localName = "Event", namespace = HK_NS)
        @JacksonXmlElementWrapper(localName = "EventList", namespace = HK_NS)
        private List<Event> events;
    }

    @Data
    public static class Event {

        @JacksonXmlProperty(localName = "type", namespace = HK_NS)
        private String type; // AccessControllerEvent / IDCardInfoEvent...

        @JacksonXmlProperty(localName = "minorAlarm", namespace = HK_NS)
        private String minorAlarm;

        @JacksonXmlProperty(localName = "minorException", namespace = HK_NS)
        private String minorException;

        @JacksonXmlProperty(localName = "minorOperation", namespace = HK_NS)
        private String minorOperation;

        @JacksonXmlProperty(localName = "minorEvent", namespace = HK_NS)
        private String minorEvent;

        @JacksonXmlProperty(localName = "pictureURLType", namespace = HK_NS)
        private String pictureURLType; // binary
    }
}
