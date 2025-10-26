package org.jeecg.modules.hkclients.model.content;

import lombok.Data;

import javax.xml.bind.annotation.*;

@Data
@XmlRootElement(name = "downloadRequest", namespace = DownloadRequest.HK_NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class DownloadRequest {
    public static final String HK_NS = "http://www.hikvision.com/ver20/XMLSchema";

    @XmlAttribute(name = "version")
    private String version = "1.0";

    @XmlElement(name = "playbackURI", namespace = HK_NS)
    private String playbackURI;
}
