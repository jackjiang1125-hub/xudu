package org.jeecg.modules.pos.vo;


import lombok.Data;

import java.math.BigDecimal;

/**
 * 账户限额信息
 */
@Data
public class PosAccountLimitVO {
    private BigDecimal dailyConsumptionLimit;
    private BigDecimal singleConsumptionLimit;
    private BigDecimal dailyRechargeLimit;
    private BigDecimal singleRechargeLimit;
}
