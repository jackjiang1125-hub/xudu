package org.jeecg.modules.acc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 门禁设备临时实体（写入 acc_device_temp）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("acc_device_temp")
public class AccDeviceTemp extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 设备序列号
     */
    @TableField("sn")
    private String sn;

    /**
     * 设备名称
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 是否重启（0/1）
     */
    @TableField("is_reboot")
    private Boolean isReboot;
}