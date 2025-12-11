package org.jeecg.modules.pos.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 消费规则表单 DTO（新增 / 编辑）
 */
@Data
public class ConsumeRuleDTO {

    /** 主键ID，编辑时必填 */
    private String id;

    private String ruleCode;
    private String ruleName;
    private String ruleType;

    /** 状态：0停用 1启用 */
    private Integer status;

    /** 优先级 */
    private Integer priority;

    /** 可用钱包类型列表 ["CASH","SUBSIDY"] */
    private List<String> availableWallets;

    /** 钱包扣款顺序 */
    private List<String> walletPriority;

    /** 每日消费金额上限 */
    private BigDecimal limitDailyAmount;

    /** 每日消费次数上限 */
    private Integer limitDailyTimes;

    /** 每餐消费金额上限 */
    private BigDecimal limitPerMealAmount;

    /** 每餐消费次数上限 */
    private Integer limitPerMealTimes;

    /** 单笔消费金额上限 */
    private BigDecimal limitSingleAmount;

    /** 生效时间范围 */
    private Date effectiveStartTime;
    private Date effectiveEndTime;

    /** 适用星期（1-7） */
    private List<Integer> weekDays;

    /** 适用餐别编码列表 */
    private List<String> mealPeriods;

    /** 适用餐厅ID列表 */
    private List<String> restaurantIds;

    /** 适用设备ID列表（可选） */
    private List<String> deviceIds;

    private Boolean allowOrder;
    private Boolean allowPosConsume;
    private Boolean allowDelivery;
    private Boolean allowSelfPickup;

    private String remark;

    /** 额外配置 JSON，自由扩展 */
    private Object extConfig;
}
