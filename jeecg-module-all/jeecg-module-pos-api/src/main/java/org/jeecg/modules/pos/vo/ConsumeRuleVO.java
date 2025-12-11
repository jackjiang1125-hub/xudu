package org.jeecg.modules.pos.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 消费规则 VO，用于列表 / 详情展示
 */
@Data
public class ConsumeRuleVO {

    private String id;
    private String ruleCode;
    private String ruleName;
    private String ruleType;

    private Integer status;
    private String statusText;

    private Integer priority;

    private List<String> availableWallets;
    private List<String> walletPriority;

    private BigDecimal limitDailyAmount;
    private Integer limitDailyTimes;
    private BigDecimal limitPerMealAmount;
    private Integer limitPerMealTimes;
    private BigDecimal limitSingleAmount;

    private Date effectiveStartTime;
    private Date effectiveEndTime;

    private List<Integer> weekDays;
    private List<String> mealPeriods;
    private List<String> restaurantIds;
    private List<String> deviceIds;

    /** 适用餐厅名称列表（通过 join / 字典转换） */
    private List<String> restaurantNames;

    private Boolean allowOrder;
    private Boolean allowPosConsume;
    private Boolean allowDelivery;
    private Boolean allowSelfPickup;

    private String remark;
}
