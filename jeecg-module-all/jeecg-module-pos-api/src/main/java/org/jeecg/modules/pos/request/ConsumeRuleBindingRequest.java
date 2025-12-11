package org.jeecg.modules.pos.request;

import lombok.Data;

/**
 * 规则绑定列表查询条件
 */
@Data
public class ConsumeRuleBindingRequest {

    private String ruleName;

    /** 绑定类型：PERSON/DEPT/GROUP */
    private String bindType;
}
