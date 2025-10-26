package org.jeecg.modules.hkclients.model.search;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.jeecg.modules.hkclients.util.RtspUriUtils;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Data @NoArgsConstructor
@XmlRootElement(name = "CMSearchResult", namespace = CMSearchResult.HK_NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class CMSearchResult {
    public static final String HK_NS = "http://www.hikvision.com/ver20/XMLSchema";

    @XmlElement(name = "searchID", namespace = HK_NS) private String searchID;
    @XmlElement(name = "numOfMatches", namespace = HK_NS) private Integer numOfMatches;

    @XmlElementWrapper(name = "matchList", namespace = HK_NS)
    @XmlElement(name = "searchMatchItem", namespace = HK_NS)
    private List<SearchMatchItem> matchList = new ArrayList<>();

    @Data @NoArgsConstructor @XmlAccessorType(XmlAccessType.FIELD)
    public static class SearchMatchItem {
        @XmlElement(name = "trackID", namespace = HK_NS) private Integer trackID;
        @XmlElement(name = "timeSpan", namespace = HK_NS) private TimeSpan timeSpan;
        @XmlElement(name = "mediaSegmentDescriptor", namespace = HK_NS) private MediaSegmentDescriptor mediaSegmentDescriptor;
    }

    @Data @NoArgsConstructor @XmlAccessorType(XmlAccessType.FIELD)
    public static class TimeSpan {
        @XmlElement(name = "startTime", namespace = HK_NS) private String startTime;
        @XmlElement(name = "endTime", namespace = HK_NS) private String endTime;
    }

    @Data @NoArgsConstructor @XmlAccessorType(XmlAccessType.FIELD)
    public static class MediaSegmentDescriptor {
        @XmlElement(name = "contentType", namespace = HK_NS) private String contentType;
        @XmlElement(name = "codecType", namespace = HK_NS) private String codecType;
        @XmlElement(name = "playbackURI", namespace = HK_NS) private String playbackURI;
        @XmlElement(name = "name", namespace = HK_NS) private String name;
        @XmlElement(name = "lockStatus", namespace = HK_NS) private String lockStatus;

        private String playTrueUri;
    }
}
