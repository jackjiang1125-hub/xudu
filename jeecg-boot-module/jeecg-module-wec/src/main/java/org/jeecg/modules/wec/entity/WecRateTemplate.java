package org.jeecg.modules.wec.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("wec_rate_template")
public class WecRateTemplate extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @TableField("template_name")
    private String templateName;

    @TableField("type")
    private String type;

    @TableField("free_seconds")
    private Integer freeSeconds;

    @TableField("work_mode")
    private String workMode;

    @TableField("deduction_method")
    private String deductionMethod;

    @TableField("real_time_amount")
    private BigDecimal realTimeAmount;

    @TableField("real_time_duration")
    private Integer realTimeDuration;

    @TableField("pre_deduct_time")
    private Integer preDeductTime;

    @TableField("pre_deduct_rate")
    private BigDecimal preDeductRate;

    @TableField("pre_deduct_amount")
    private BigDecimal preDeductAmount;

    @TableField("per_time_duration")
    private Integer perTimeDuration;
}
