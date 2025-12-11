package org.jeecg.modules.pos.request;

import lombok.Data;

import java.util.List;

/**
 * 消费规则列表查询条件
 */
@Data
public class ConsumeRuleRequest {

    /** 规则名称（模糊） */
    private String ruleName;

    /** 状态：0/1 */
    private Integer status;

    /** 规则类型：NORMAL/SUBSIDY 等 */
    private String ruleType;

    /** 适用餐厅ID（可选，用于过滤） */
    private String restaurantId;

    /** 适用餐别（例如 BREAKFAST/LUNCH） */
    private String mealPeriod;

    /** 适用星期（1-7） */
    private Integer weekDay;

    /** 规则ID列表（批量查询时用，可选） */
    private List<String> ruleIds;
}
