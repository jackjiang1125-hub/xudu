package org.jeecg.modules.acc.vo;

import lombok.Data;

import java.util.List;

/**
 * 权限组VO（与前端accgroup界面字段对齐）
 */
@Data
public class AccGroupVO {
    private String id;
    private String groupName;
    /** 展示用时间段名称 */
    private String periodName;
    /** 绑定的时间段ID */
    private String periodId;
    private Integer memberCount;
    private Integer deviceCount;
    /** 创建时间（yyyy-MM-dd HH:mm:ss） */
    private String createTime;
    private String remark;

    /** 关联ID集合 */
    private List<String> members;
    private List<String> devices;

    /** 业务配置：移除旧时间相关字段 */
}