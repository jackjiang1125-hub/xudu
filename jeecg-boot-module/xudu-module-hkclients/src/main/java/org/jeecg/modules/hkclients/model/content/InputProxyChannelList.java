package org.jeecg.modules.hkclients.model.content;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Data @NoArgsConstructor
@XmlRootElement(name = "InputProxyChannelList", namespace = InputProxyChannelList.HK_NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class InputProxyChannelList {
    public static final String HK_NS = "http://www.isapi.org/ver20/XMLSchema";

    @XmlAttribute(name = "version")
    private String version;

    @XmlAttribute(name = "size")
    private Integer size;

    @XmlElement(name = "InputProxyChannel", namespace = HK_NS)
    private List<InputProxyChannel> channels = new ArrayList<>();

    @Data @NoArgsConstructor @XmlAccessorType(XmlAccessType.FIELD)
    public static class InputProxyChannel {
        @XmlElement(name = "id", namespace = HK_NS) private Integer id;
        @XmlElement(name = "name", namespace = HK_NS) private String name;
        @XmlElement(name = "online", namespace = HK_NS) private Boolean online;
        @XmlElement(name = "certificateValidationEnabled", namespace = HK_NS)
        private Boolean certificateValidationEnabled;
        @XmlElement(name = "defaultAdminPortEnabled", namespace = HK_NS)
        private Boolean defaultAdminPortEnabled;
        @XmlElement(name = "enableAnr", namespace = HK_NS)
        private Boolean enableAnr;
        @XmlElement(name = "enableTiming", namespace = HK_NS)
        private Boolean enableTiming;
        @XmlElement(name = "sourceInputPortDescriptor", namespace = HK_NS)
        private SourceInputPortDescriptor sourceInputPortDescriptor;
    }

    @Data @NoArgsConstructor @XmlAccessorType(XmlAccessType.FIELD)
    public static class SourceInputPortDescriptor {
        @XmlElement(name = "proxyProtocol", namespace = HK_NS)
        private String proxyProtocol;
        @XmlElement(name = "addressingFormatType", namespace = HK_NS)
        private String addressingFormatType;
        @XmlElement(name = "ipAddress", namespace = HK_NS)
        private String ipAddress;
        @XmlElement(name = "managePortNo", namespace = HK_NS)
        private Integer managePortNo;
        @XmlElement(name = "srcInputPort", namespace = HK_NS)
        private Integer srcInputPort;
        @XmlElement(name = "manufacturer", namespace = HK_NS)
        private String manufacturer;
        @XmlElement(name = "model", namespace = HK_NS)
        private String model = "未知";

        @XmlElement(name = "serialNumber", namespace = HK_NS)
        private String serialNumber;
        @XmlElement(name = "userName", namespace = HK_NS)
        private String userName;
        @XmlElement(name = "password", namespace = HK_NS)
        private String password;
        @XmlElement(name = "streamType", namespace = HK_NS)
        private String streamType;
        @XmlElement(name = "deviceID", namespace = HK_NS)
        private String deviceID;
        @XmlElement(name = "getStream", namespace = HK_NS)
        private Boolean getStream;
    }
}
