package org.jeecg.modules.pos.vo;

import lombok.Data;

/**
 * 规则绑定 VO
 */
@Data
public class ConsumeRuleBindingVO {

    private String id;

    private String ruleId;
    private String ruleName;

    private String bindType;
    private String bindTypeText;

    /** 某条记录对应的单个 targetId */
    private String targetId;
    private String targetName;

    /** 用于列表展示的汇总说明（例如 “按人：张三、李四等 2 人”） */
    private String targetSummary;

    private String remark;
}
