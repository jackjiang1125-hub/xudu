package org.jeecg.modules.hkclients.model.record;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;

@Data @NoArgsConstructor
@XmlRootElement(name = "trackDailyParam", namespace = TrackDailyParam.HK_NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class TrackDailyParam {
    public static final String HK_NS = "http://www.hikvision.com/ver20/XMLSchema";
    @XmlElement(name = "year", namespace = HK_NS)
    private int year;
    @XmlElement(name = "monthOfYear", namespace = HK_NS)
    private String monthOfYear;
    public TrackDailyParam(int year, int month) {
        this.year = year;
        this.monthOfYear = String.format("%02d", month);
    }
}
