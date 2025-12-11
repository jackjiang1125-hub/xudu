package org.jeecg.modules.wec.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WecRateTemplateVO {
    private String id;
    private String templateName;
    private String type;
    private Integer freeSeconds;
    private String workMode;
    private String deductionMethod;
    private BigDecimal realTimeAmount;
    private Integer realTimeDuration;
    private Integer preDeductTime;
    private BigDecimal preDeductRate;
    private BigDecimal preDeductAmount;
    private Integer perTimeDuration;
}
