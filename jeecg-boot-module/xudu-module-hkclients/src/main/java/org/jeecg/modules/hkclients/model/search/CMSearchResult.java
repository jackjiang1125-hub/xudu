package org.jeecg.modules.hkclients.model.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

/**
 * Jackson-XML 版 CMSearchResult
 * 适配：/ISAPI/ContentMgmt/search
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "CMSearchResult")
public class CMSearchResult {

    @JacksonXmlProperty(localName = "searchID")
    private String searchID;

    @JacksonXmlProperty(localName = "responseStatusStrg")
    private String responseStatusStrg;

    @JacksonXmlProperty(localName = "numOfMatches")
    private Integer numOfMatches;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "matchList")
    private List<SearchMatchItem> matchList;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SearchMatchItem {

        @JacksonXmlProperty(localName = "trackID")
        private Integer trackID;

        @JacksonXmlProperty(localName = "timeSpan")
        private TimeSpan timeSpan;

        @JacksonXmlProperty(localName = "mediaSegmentDescriptor")
        private MediaSegmentDescriptor mediaSegmentDescriptor;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TimeSpan {
        @JacksonXmlProperty(localName = "startTime")
        private String startTime; // 例如 2025-10-24T23:43:14Z

        @JacksonXmlProperty(localName = "endTime")
        private String endTime;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class MediaSegmentDescriptor {
        @JacksonXmlProperty(localName = "contentType")
        private String contentType;

        @JacksonXmlProperty(localName = "codecType")
        private String codecType;

        @JacksonXmlProperty(localName = "playbackURI")
        private String playbackURI;

        @JacksonXmlProperty(localName = "name")
        private String name;

        @JacksonXmlProperty(localName = "lockStatus")
        private String lockStatus;
    }
}