package org.jeecg.modules.pos.vo;

import lombok.Data;

import java.util.List;

/**
 * 消费人群 VO
 */
@Data
public class ConsumeGroupVO {

    private String id;

    private String groupCode;
    private String groupName;
    private String groupType;
    private String groupTypeText;

    private String memberMode;
    private String memberModeText;

    private Integer status;
    private String statusText;

    private String description;
    private List<String> tags;

    /** 预估成员数量（从成员表统计） */
    private Integer memberCount;
}
