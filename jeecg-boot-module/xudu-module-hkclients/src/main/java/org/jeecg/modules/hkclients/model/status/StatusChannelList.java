package org.jeecg.modules.hkclients.model.status;

import lombok.Data;

import javax.xml.bind.annotation.*;

@Data
@XmlRootElement(name = "StatusChannelList", namespace = StatusChannelList.HK_NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class StatusChannelList {
    public static final String HK_NS = "http://www.hikvision.com/ver20/XMLSchema";

    @XmlElement(name = "StatusChannel", namespace = HK_NS)
    private java.util.List<StatusChannel> channels;
}
