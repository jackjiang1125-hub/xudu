package org.jeecg.modules.acc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 门禁时间段-按天配置实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("acc_time_period_detail")
public class AccTimePeriodDetail extends JeecgEntity {

    /**
     * 时间段ID
     */
    @TableField("period_id")
    private String periodId;

    /**
     * 日期键（mon/tue/.../holiday1/holiday2/holiday3）
     */
    @TableField("day_key")
    private String dayKey;

    /**
     * 显示标签（星期一/假日类型1等）
     */
    @TableField("label")
    private String label;

    @TableField("segment1_start")
    private String segment1Start;
    @TableField("segment1_end")
    private String segment1End;

    @TableField("segment2_start")
    private String segment2Start;
    @TableField("segment2_end")
    private String segment2End;

    @TableField("segment3_start")
    private String segment3Start;
    @TableField("segment3_end")
    private String segment3End;
}