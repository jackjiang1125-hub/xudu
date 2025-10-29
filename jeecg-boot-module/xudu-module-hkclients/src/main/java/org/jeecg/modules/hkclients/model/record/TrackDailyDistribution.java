package org.jeecg.modules.hkclients.model.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "trackDailyDistribution")
public class TrackDailyDistribution {

    @JacksonXmlElementWrapper(localName = "dayList")
    @JacksonXmlProperty(localName = "day")
    private List<Day> dayList = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Day {
        @JacksonXmlProperty(localName = "id")
        private Integer id;

        @JacksonXmlProperty(localName = "dayOfMonth")
        private Integer dayOfMonth;

        @JacksonXmlProperty(localName = "record")
        private Boolean record;

        @JacksonXmlProperty(localName = "recordType")
        private String recordType;
    }
}
