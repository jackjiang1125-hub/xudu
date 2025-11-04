package org.jeecg.modules.pos.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 消费记录VO
 */
@Data
@Schema(name = "PosConsumptionRecordVO", description = "消费记录VO")
public class PosConsumptionRecordVO {

    @Schema(description = "ID")
    private String id;

    @Schema(description = "消费单号")
    private String recordNo;

    @Schema(description = "卡号")
    private String cardNo;

    @Schema(description = "人员编号")
    private String customerId;

    @Schema(description = "人员姓名")
    private String customerName;

    @Schema(description = "人员类型")
    private String customerType;

    @Schema(description = "消费类型")
    private String type;

    @Schema(description = "消费金额")
    private BigDecimal amount;

    @Schema(description = "折扣金额")
    private BigDecimal discountAmount;

    @Schema(description = "折扣百分比")
    private BigDecimal discountPercent;

    @Schema(description = "消费后余额")
    private BigDecimal balanceAfter;

    @Schema(description = "设备名称")
    private String deviceName;

    @Schema(description = "设备序列号")
    private String deviceCode;

    @Schema(description = "餐厅编码")
    private String restaurantCode;

    @Schema(description = "餐厅名称")
    private String scene;

    @Schema(description = "验证方式")
    private String verifyMethod;

    @Schema(description = "消费渠道")
    private String channel;

    @Schema(description = "操作员")
    private String operator;

    @Schema(description = "消费时间")
    private Date consumeTime;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "明细列表")
    private List<PosConsumptionDetailVO> details;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "更新时间")
    private Date updateTime;
}