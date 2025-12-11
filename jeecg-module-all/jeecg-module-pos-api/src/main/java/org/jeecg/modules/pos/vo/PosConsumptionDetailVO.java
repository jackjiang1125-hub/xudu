package org.jeecg.modules.pos.vo;


import lombok.Data;

import java.math.BigDecimal;

/**
 * 消费记录明细VO
 */
@Data
public class PosConsumptionDetailVO {
    private String id;
    private String recordId;
    private String skuCode;
    private String productName;
    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalAmount;
}