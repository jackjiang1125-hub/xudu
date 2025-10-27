package org.jeecg.modules.hkclients.model.record;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "trackDailyParam")
public class TrackDailyParam {

    @JacksonXmlProperty(localName = "year")
    private int year;

    @JacksonXmlProperty(localName = "monthOfYear")
    private String monthOfYear;

    public TrackDailyParam(int year, int month) {
        this.year = year;
        this.monthOfYear = String.format("%02d", month);
    }
}
