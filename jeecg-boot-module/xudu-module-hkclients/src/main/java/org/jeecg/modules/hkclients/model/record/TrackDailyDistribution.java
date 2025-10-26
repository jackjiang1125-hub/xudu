package org.jeecg.modules.hkclients.model.record;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.xml.bind.annotation.*;
import java.util.ArrayList;
import java.util.List;

@Data @NoArgsConstructor
@XmlRootElement(name = "trackDailyDistribution", namespace = TrackDailyDistribution.HK_NS)
@XmlAccessorType(XmlAccessType.FIELD)
public class TrackDailyDistribution {
    public static final String HK_NS = "http://www.hikvision.com/ver20/XMLSchema";

    @XmlElementWrapper(name = "dayList", namespace = HK_NS)
    @XmlElement(name = "day", namespace = HK_NS)
    private List<Day> dayList = new ArrayList<>();

    @Data @NoArgsConstructor @XmlAccessorType(XmlAccessType.FIELD)
    public static class Day {
        @XmlElement(name = "id", namespace = HK_NS) private Integer id;
        @XmlElement(name = "dayOfMonth", namespace = HK_NS) private Integer dayOfMonth;
        @XmlElement(name = "record", namespace = HK_NS) private Boolean record;
        @XmlElement(name = "recordType", namespace = HK_NS) private String recordType;
    }
}
