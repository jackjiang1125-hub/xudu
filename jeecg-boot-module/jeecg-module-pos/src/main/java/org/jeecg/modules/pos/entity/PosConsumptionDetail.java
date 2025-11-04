package org.jeecg.modules.pos.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;

import java.math.BigDecimal;

/**
 * 消费记录明细实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pos_consumption_detail")
@Schema(name = "PosConsumptionDetail", description = "消费记录明细")
public class PosConsumptionDetail extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 消费记录ID
     */
    @Schema(description = "消费记录ID")
    @TableField("record_id")
    private String recordId;

    /**
     * SKU编码
     */
    @Excel(name = "SKU编码", width = 15)
    @Schema(description = "SKU编码")
    @TableField("sku_code")
    private String skuCode;

    /**
     * 商品名称
     */
    @Excel(name = "商品名称", width = 25)
    @Schema(description = "商品名称")
    @TableField("product_name")
    private String productName;

    /**
     * 单价
     */
    @Excel(name = "单价", width = 10)
    @Schema(description = "单价")
    @TableField("unit_price")
    private BigDecimal unitPrice;

    /**
     * 数量
     */
    @Excel(name = "数量", width = 10)
    @Schema(description = "数量")
    @TableField("quantity")
    private Integer quantity;

    /**
     * 小计金额
     */
    @Excel(name = "小计金额", width = 15)
    @Schema(description = "小计金额")
    @TableField("total_amount")
    private BigDecimal totalAmount;
}