package org.jeecg.modules.pos.dto;

import lombok.Data;

import java.util.List;

/**
 * 规则绑定表单 DTO
 */
@Data
public class ConsumeRuleBindingDTO {

    /** 主键ID，编辑（单条）时可选 */
    private String id;

    /** 规则ID */
    private String ruleId;

    /** 绑定类型：PERSON/DEPT/GROUP */
    private String bindType;

    /** 绑定对象ID列表 */
    private List<String> targetIds;

    /** 备注 */
    private String remark;
}
