package org.jeecg.modules.acc.vo;

import lombok.Data;

import java.util.List;

@Data
public class TimePeriodVO {
    private String id;
    private String name;
    private String remark;
    /** 排序（从1开始递增） */
    private Integer sortOrder;
    /** 格式：yyyy-MM-dd HH:mm */
    private String updatedAt;
    /** 创建或更新人 */
    private String creator;
    private List<TimePeriodDetailVO> detail;
}