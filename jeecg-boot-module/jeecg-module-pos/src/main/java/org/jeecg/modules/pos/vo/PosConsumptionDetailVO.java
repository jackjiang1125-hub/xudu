package org.jeecg.modules.pos.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 消费记录明细VO
 */
@Data
@Schema(name = "PosConsumptionDetailVO", description = "消费记录明细VO")
public class PosConsumptionDetailVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "消费记录ID")
    private String recordId;

    @Schema(description = "SKU编码")
    private String skuCode;

    @Schema(description = "商品名称")
    private String productName;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "小计金额")
    private BigDecimal totalAmount;
}