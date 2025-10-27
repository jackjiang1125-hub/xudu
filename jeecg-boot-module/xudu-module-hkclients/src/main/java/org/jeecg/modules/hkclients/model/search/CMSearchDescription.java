package org.jeecg.modules.hkclients.model.search;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Data @NoArgsConstructor
@XmlRootElement(name = "CMSearchDescription", namespace = CMSearchDescription.HK_NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class CMSearchDescription {
    public static final String HK_NS = "http://www.hikvision.com/ver20/XMLSchema";

    @XmlElement(name = "searchID", namespace = HK_NS) private String searchID;

    @XmlElementWrapper(name = "trackList", namespace = HK_NS)
    @XmlElement(name = "trackID", namespace = HK_NS)
    private List<String> trackList = new ArrayList<>();

    @XmlElementWrapper(name = "timeSpanList", namespace = HK_NS)
    @XmlElement(name = "timeSpan", namespace = HK_NS)
    private List<TimeSpan> timeSpanList = new ArrayList<>();

    @XmlElement(name = "maxResults", namespace = HK_NS) private Integer maxResults;
    @XmlElement(name = "searchResultPostion", namespace = HK_NS) private Integer searchResultPostion;

    @XmlElementWrapper(name = "metadataList", namespace = HK_NS)
    @XmlElement(name = "metadataDescriptor", namespace = HK_NS)
    private List<String> metadataList = new ArrayList<>();

    @Data @NoArgsConstructor @XmlAccessorType(XmlAccessType.FIELD)
    public static class TimeSpan {
        @XmlElement(name = "startTime", namespace = HK_NS) private String startTime;
        @XmlElement(name = "endTime", namespace = HK_NS) private String endTime;
    }
}
