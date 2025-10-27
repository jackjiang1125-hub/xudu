package org.jeecg.modules.acc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 门禁时间段实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("acc_time_period")
public class AccTimePeriod extends JeecgEntity {

    /**
     * 时间段名称
     */
    @TableField("name")
    private String name;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;

    /**
     * 排序（从1开始递增）
     */
    @TableField("sort_order")
    private Integer sortOrder;
}