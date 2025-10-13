package org.jeecg.modules.acc.vo;

import lombok.Data;

import java.util.List;

@Data
public class TimePeriodDetailVO {
    private String key;
    private String label;
    private List<TimeIntervalVO> segments;
}