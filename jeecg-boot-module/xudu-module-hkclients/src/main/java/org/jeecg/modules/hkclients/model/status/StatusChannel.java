package org.jeecg.modules.hkclients.model.status;

import lombok.Data;

import javax.xml.bind.annotation.*;

@Data
@XmlAccessorType(XmlAccessType.FIELD)
public class StatusChannel {
    public static final String HK_NS = "http://www.hikvision.com/ver20/XMLSchema";

    @XmlElement(name = "id", namespace = HK_NS) private Integer id;
    @XmlElement(name = "video", namespace = HK_NS) private Video video;
    @XmlElement(name = "audio", namespace = HK_NS) private Audio audio;

    @Data @XmlAccessorType(XmlAccessType.FIELD)
    public static class Video {
        @XmlElement(name = "codecType", namespace = HK_NS) private String codecType;
    }
    @Data @XmlAccessorType(XmlAccessType.FIELD)
    public static class Audio {
        @XmlElement(name = "codecType", namespace = HK_NS) private String codecType;
    }

    @XmlTransient
    private String contentType;
}
