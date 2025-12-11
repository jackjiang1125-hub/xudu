package org.jeecg.modules.pos.request;

import lombok.Data;

/**
 * 消费人群列表查询条件
 */
@Data
public class ConsumeGroupRequest {

    private String groupName;

    /** 人群类型：STUDENT/TEACHER/MIXED 等 */
    private String groupType;

    /** 状态：0/1 */
    private Integer status;
}
