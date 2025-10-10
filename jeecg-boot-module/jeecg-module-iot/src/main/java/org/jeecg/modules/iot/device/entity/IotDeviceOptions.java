package org.jeecg.modules.iot.device.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.time.LocalDateTime;

/**
 * 设备参数表，存储设备上报的各种参数信息
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_device_options")
public class IotDeviceOptions extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 设备ID，关联iot_acc_device表
     */
    @TableField("device_id")
    private String deviceId;

    /**
     * 设备序列号
     */
    @TableField("sn")
    private String sn;

    /**
     * 参数名称
     */
    @TableField("param_name")
    private String paramName;

    /**
     * 参数值
     */
    @TableField("param_value")
    private String paramValue;

    /**
     * 参数类型(STRING,INTEGER,BOOLEAN)
     */
    @TableField("param_type")
    private String paramType;

    /**
     * 上报时间
     */
    @TableField("report_time")
    private LocalDateTime reportTime;

    /**
     * 原始payload数据
     */
    @TableField("raw_payload")
    private String rawPayload;

    /**
     * 客户端IP
     */
    @TableField("client_ip")
    private String clientIp;
}
