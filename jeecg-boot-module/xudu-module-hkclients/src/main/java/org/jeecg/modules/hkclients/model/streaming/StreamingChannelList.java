package org.jeecg.modules.hkclients.model.streaming;

import lombok.Data;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * 根元素在 HK 命名空间；列表元素 StreamingChannel 在 ISAPI 命名空间
 */
@Data
@XmlRootElement(name = "StreamingChannelList", namespace = StreamingChannelList.HK_NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class StreamingChannelList {
    public static final String HK_NS   = "http://www.hikvision.com/ver20/XMLSchema";
    public static final String ISAPI_NS = "http://www.isapi.org/ver20/XMLSchema";

    @XmlElement(name = "StreamingChannel", namespace = ISAPI_NS)
    private List<StreamingChannel> streamingChannel;
}
