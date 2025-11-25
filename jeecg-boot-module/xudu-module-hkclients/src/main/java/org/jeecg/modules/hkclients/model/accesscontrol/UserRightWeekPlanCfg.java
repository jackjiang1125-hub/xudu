package org.jeecg.modules.hkclients.model.accesscontrol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

import java.util.List;

@Data
@JsonRootName("UserRightWeekPlanCfg")          // ★ 告诉 Jackson 根节点名
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserRightWeekPlanCfg {

    @JsonProperty("enable")
    private Boolean enable = true;

    @JsonProperty("WeekPlanCfg")               // ★ JSON 里是大写开头
    private List<Day> weekPlanCfg;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Day {

        @JsonProperty("week")
        private String week;

        @JsonProperty("id")
        private Integer id = 1;

        @JsonProperty("enable")
        private Boolean enable = true;

        @JsonProperty("TimeSegment")           // ★ JSON 里是 TimeSegment
        private TimeSegment timeSegment;

     //   @JsonProperty("authenticationTimesEnabled")
       // private Boolean authenticationTimesEnabled;

    //    @JsonProperty("authenticationTimes")
      //  private Integer authenticationTimes;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TimeSegment {

        @JsonProperty("beginTime")
        private String beginTime;

        @JsonProperty("endTime")
        private String endTime;
    }
}
