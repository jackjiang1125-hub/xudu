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
    public static final String HK_NS = "http://www.hikvision.com/ver20/XMLSchema";

    @XmlElement(name = "InputProxyChannel", namespace = HK_NS)
    private List<InputProxyChannel> channels = new ArrayList<>();

    @Data @NoArgsConstructor @XmlAccessorType(XmlAccessType.FIELD)
    public static class InputProxyChannel {
        @XmlElement(name = "id", namespace = HK_NS) private Integer id;
        @XmlElement(name = "name", namespace = HK_NS) private String name;
        @XmlElement(name = "online", namespace = HK_NS) private Boolean online;
        @XmlElement(name = "sourceInputPortDescriptor", namespace = HK_NS)
        private SourceInputPortDescriptor sourceInputPortDescriptor;
    }

    @Data @NoArgsConstructor @XmlAccessorType(XmlAccessType.FIELD)
    public static class SourceInputPortDescriptor {
        @XmlElement(name = "ipAddress", namespace = HK_NS)
        private String ipAddress;
        @XmlElement(name = "manufacturer", namespace = HK_NS)
        private String manufacturer;
        @XmlElement(name = "model", namespace = HK_NS)
        private String model = "未知";

        @XmlElement(name = "serialNumber", namespace = HK_NS)
        private String serialNumber;
        @XmlElement(name = "userName", namespace = HK_NS)
        private String userName;
    }
}
