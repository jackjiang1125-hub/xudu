package org.jeecg.modules.pos.vo;


import lombok.Data;

import java.math.BigDecimal;

/**
 * 当日收支汇总
 */
@Data
public class PosAccountTodaySummaryVO {
    private Integer totalCount = 0;
    private BigDecimal rechargeAmount = BigDecimal.ZERO;
    private Integer rechargeCount = 0;
    private BigDecimal refundAmount = BigDecimal.ZERO;
    private Integer refundCount = 0;
    private BigDecimal expenseAmount = BigDecimal.ZERO;
    private Integer expenseCount = 0;

    public static PosAccountTodaySummaryVO empty() {
        return new PosAccountTodaySummaryVO();
    }
}
