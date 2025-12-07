package org.jeecgframework.boot.iot.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class WaterRateConfigVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 工作模式 (0:实时扣费 1:预扣费 2:计次消费)
     */
    private Integer workMode;

    /**
     * 扣费方式 (0:计时 1:脉冲)
     */
    private Integer deductionMethod;

    /**
     * 免费时间 (秒)
     */
    private Integer freeSeconds;

    /**
     * 实时扣费金额 (分)
     */
    private Integer realTimeAmount;

    /**
     * 实时扣费单位时间(秒) 或 单位脉冲数
     */
    private Integer realTimeDuration;

    /**
     * 预扣费时间(秒) 或 预扣费流量(脉冲)
     */
    private Integer preDeductDuration;

    /**
     * 计次时间(秒) 或 计次脉冲数
     */
    private Integer perTimeDuration;

    /**
     * 计次金额 (分)
     */
    private Integer perTimeAmount;
}
