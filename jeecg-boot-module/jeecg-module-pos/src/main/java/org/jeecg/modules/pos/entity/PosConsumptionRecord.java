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
import java.util.Date;

/**
 * 消费记录实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pos_consumption_record")
@Schema(name = "PosConsumptionRecord", description = "消费记录")
public class PosConsumptionRecord extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 消费单号
     */
    @Excel(name = "消费单号", width = 20)
    @Schema(description = "消费单号")
    @TableField("record_no")
    private String recordNo;

    /**
     * 卡号
     */
    @Excel(name = "卡号", width = 15)
    @Schema(description = "卡号")
    @TableField("card_no")
    private String cardNo;

    /**
     * 人员编号
     */
    @Excel(name = "人员编号", width = 15)
    @Schema(description = "人员编号")
    @TableField("customer_id")
    private String customerId;

    /**
     * 人员姓名
     */
    @Excel(name = "人员姓名", width = 15)
    @Schema(description = "人员姓名")
    @TableField("customer_name")
    private String customerName;

    /**
     * 人员类型
     */
    @Excel(name = "人员类型", width = 15)
    @Schema(description = "人员类型")
    @TableField("customer_type")
    private String customerType;

    /**
     * 消费类型 (product: 商品消费, meal: 餐饮消费, recharge: 充值扣减, service: 服务扣费)
     */
    @Excel(name = "消费类型", width = 15, dicCode = "consumption_type")
    @Schema(description = "消费类型")
    @TableField("type")
    private String type;

    /**
     * 消费金额
     */
    @Excel(name = "消费金额", width = 15)
    @Schema(description = "消费金额")
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 折扣金额
     */
    @Excel(name = "折扣金额", width = 15)
    @Schema(description = "折扣金额")
    @TableField("discount_amount")
    private BigDecimal discountAmount;

    /**
     * 折扣百分比
     */
    @Excel(name = "折扣百分比", width = 15)
    @Schema(description = "折扣百分比")
    @TableField("discount_percent")
    private BigDecimal discountPercent;

    /**
     * 消费后余额
     */
    @Excel(name = "消费后余额", width = 15)
    @Schema(description = "消费后余额")
    @TableField("balance_after")
    private BigDecimal balanceAfter;

    /**
     * 设备名称
     */
    @Excel(name = "设备名称", width = 20)
    @Schema(description = "设备名称")
    @TableField("device_name")
    private String deviceName;

    /**
     * 设备序列号
     */
    @Excel(name = "设备序列号", width = 20)
    @Schema(description = "设备序列号")
    @TableField("device_code")
    private String deviceCode;

    /**
     * 餐厅编码
     */
    @Excel(name = "餐厅编码", width = 15)
    @Schema(description = "餐厅编码")
    @TableField("restaurant_code")
    private String restaurantCode;

    /**
     * 餐厅名称
     */
    @Excel(name = "餐厅名称", width = 20)
    @Schema(description = "餐厅名称")
    @TableField("scene")
    private String scene;

    /**
     * 验证方式
     */
    @Excel(name = "验证方式", width = 15)
    @Schema(description = "验证方式")
    @TableField("verify_method")
    private String verifyMethod;

    /**
     * 消费渠道
     */
    @Excel(name = "消费渠道", width = 15)
    @Schema(description = "消费渠道")
    @TableField("channel")
    private String channel;

    /**
     * 操作员
     */
    @Excel(name = "操作员", width = 15)
    @Schema(description = "操作员")
    @TableField("operator")
    private String operator;

    /**
     * 消费时间
     */
    @Excel(name = "消费时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "消费时间")
    @TableField("consume_time")
    private Date consumeTime;

    /**
     * 备注
     */
    @Excel(name = "备注", width = 30)
    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}