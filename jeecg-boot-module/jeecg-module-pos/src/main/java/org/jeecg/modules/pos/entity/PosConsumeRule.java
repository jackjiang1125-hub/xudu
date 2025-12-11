package org.jeecg.modules.pos.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 消费规则
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "pos_consume_rule", autoResultMap = true)
public class PosConsumeRule extends JeecgEntity {

    private static final long serialVersionUID = 1L;

    /** 规则编码 */
    @TableField("rule_code")
    private String ruleCode;

    /** 规则名称 */
    @TableField("rule_name")
    private String ruleName;

    /** 规则类型：NORMAL / SUBSIDY 等 */
    @TableField("rule_type")
    private String ruleType;

    /** 状态：0停用 1启用 */
    @TableField("status")
    private Integer status;

    /** 优先级，数字越小优先级越高 */
    @TableField("priority")
    private Integer priority;

    /** 可用钱包类型列表，例如 ["CASH","SUBSIDY"] */
    @TableField(value = "available_wallets", typeHandler = JacksonTypeHandler.class)
    private List<String> availableWallets;

    /** 钱包扣款顺序，例如 ["SUBSIDY","CASH"] */
    @TableField(value = "wallet_priority", typeHandler = JacksonTypeHandler.class)
    private List<String> walletPriority;

    /** 每日消费金额上限 */
    @TableField("limit_daily_amount")
    private BigDecimal limitDailyAmount;

    /** 每日消费次数上限 */
    @TableField("limit_daily_times")
    private Integer limitDailyTimes;

    /** 每餐消费金额上限 */
    @TableField("limit_per_meal_amount")
    private BigDecimal limitPerMealAmount;

    /** 每餐消费次数上限 */
    @TableField("limit_per_meal_times")
    private Integer limitPerMealTimes;

    /** 单笔消费金额上限 */
    @TableField("limit_single_amount")
    private BigDecimal limitSingleAmount;

    /** 生效开始时间 */
    @TableField("effective_start_time")
    private Date effectiveStartTime;

    /** 生效结束时间 */
    @TableField("effective_end_time")
    private Date effectiveEndTime;

    /** 适用星期（1-7） */
    @TableField(value = "week_days", typeHandler = JacksonTypeHandler.class)
    private List<Integer> weekDays;

    /** 适用餐别编码列表，例如 ["BREAKFAST","LUNCH"] */
    @TableField(value = "meal_periods", typeHandler = JacksonTypeHandler.class)
    private List<String> mealPeriods;

    /** 适用餐厅ID列表 */
    @TableField(value = "restaurant_ids", typeHandler = JacksonTypeHandler.class)
    private List<String> restaurantIds;

    /** 适用设备ID列表（可选） */
    @TableField(value = "device_ids", typeHandler = JacksonTypeHandler.class)
    private List<String> deviceIds;

    /** 是否允许订餐 */
    @TableField("allow_order")
    private Boolean allowOrder;

    /** 是否允许POS现场消费 */
    @TableField("allow_pos_consume")
    private Boolean allowPosConsume;

    /** 是否允许外送 */
    @TableField("allow_delivery")
    private Boolean allowDelivery;

    /** 是否允许自取 */
    @TableField("allow_self_pickup")
    private Boolean allowSelfPickup;

    /** 备注 */
    @TableField("remark")
    private String remark;

    /** 扩展配置JSON */
    @TableField(value = "ext_config", typeHandler = JacksonTypeHandler.class)
    private Object extConfig;
}
