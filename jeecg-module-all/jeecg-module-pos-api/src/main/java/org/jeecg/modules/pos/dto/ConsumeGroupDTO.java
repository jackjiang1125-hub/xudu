package org.jeecg.modules.pos.dto;

import lombok.Data;

import java.util.List;

/**
 * 消费人群表单 DTO
 */
@Data
public class ConsumeGroupDTO {

    /** 主键ID，编辑时必填 */
    private String id;

    private String groupCode;
    private String groupName;
    private String groupType;

    /** 成员来源方式：PERSON/DEPT */
    private String memberMode;

    /** 状态：0停用 1启用 */
    private Integer status;

    private String description;

    private List<String> tags;

    /** 成员ID列表（按成员来源方式决定意义：ACCOUNT ID 列表或 DEPT ID 列表） */
    private List<String> memberIds;
}
