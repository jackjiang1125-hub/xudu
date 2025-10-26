package org.jeecg.modules.hkclients.model.network;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Data @NoArgsConstructor
@XmlRootElement(name = "NetworkInterface", namespace = NetworkInterface.HK_NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class NetworkInterface {
    public static final String HK_NS = "http://www.hikvision.com/ver20/XMLSchema";

    @XmlElement(name = "id", namespace = HK_NS) private Integer id;
    @XmlElement(name = "IPAddress", namespace = HK_NS) private IPAddress ipAddress;

    @Data @NoArgsConstructor @XmlAccessorType(XmlAccessType.FIELD)
    public static class IPAddress {
        @XmlElement(name = "ipVersion", namespace = HK_NS) private String ipVersion;
        @XmlElement(name = "addressingType", namespace = HK_NS) private String addressingType;
        @XmlElement(name = "ipAddress", namespace = HK_NS) private String ipAddress;
        @XmlElement(name = "subnetMask", namespace = HK_NS) private String subnetMask;
        @XmlElement(name = "gateway", namespace = HK_NS) private String gateway;
    }
}
