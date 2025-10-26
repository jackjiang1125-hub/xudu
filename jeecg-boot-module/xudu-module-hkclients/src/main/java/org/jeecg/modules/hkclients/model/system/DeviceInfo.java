package org.jeecg.modules.hkclients.model.system;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Data @NoArgsConstructor
@XmlRootElement(name = "DeviceInfo", namespace = DeviceInfo.HK_NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class DeviceInfo {
    public static final String HK_NS = "http://www.hikvision.com/ver20/XMLSchema";

    @XmlElement(name = "deviceName", namespace = HK_NS) private String deviceName;
    @XmlElement(name = "deviceID", namespace = HK_NS) private String deviceID;
    @XmlElement(name = "model", namespace = HK_NS) private String model;
    @XmlElement(name = "serialNumber", namespace = HK_NS) private String serialNumber;
    @XmlElement(name = "macAddress", namespace = HK_NS) private String macAddress;
    @XmlElement(name = "firmwareVersion", namespace = HK_NS) private String firmwareVersion;
    @XmlElement(name = "firmwareReleasedDate", namespace = HK_NS) private String firmwareReleasedDate;
    @XmlElement(name = "encoderVersion", namespace = HK_NS) private String encoderVersion;
    @XmlElement(name = "encoderReleasedDate", namespace = HK_NS) private String encoderReleasedDate;
    @XmlElement(name = "deviceType", namespace = HK_NS) private String deviceType;
    @XmlElement(name = "telecontrolID", namespace = HK_NS) private String telecontrolID;
    @XmlElement(name = "hardwareVersion", namespace = HK_NS) private String hardwareVersion;
    @XmlElement(name = "manufacturer", namespace = HK_NS) private String manufacturer;
    @XmlElement(name = "ipv4Address", namespace = HK_NS) private String ipv4Address;
}
