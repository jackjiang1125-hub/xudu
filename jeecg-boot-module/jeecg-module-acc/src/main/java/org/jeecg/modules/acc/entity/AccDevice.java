package org.jeecg.modules.acc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.time.LocalDateTime;

/**
 * 门禁设备实体
 * @author system
 * @date 2025-01-03
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("acc_device")
public class AccDevice extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 设备序列号
     */
    @TableField("sn")
    private String sn;

    /**
     * 设备类型 (acc, att, pos ...)
     */
    @TableField("device_type")
    private String deviceType;

    /**
     * 设备名称
     */
    @TableField("device_name")
    private String deviceName;

    /**
     * 固件版本
     */
    @TableField("firmware_version")
    private String firmwareVersion;

    /**
     * 推送协议版本
     */
    @TableField("push_version")
    private String pushVersion;

    /**
     * 支持的锁数量
     */
    @TableField("lock_count")
    private Integer lockCount;

    /**
     * 支持的读头数量
     */
    @TableField("reader_count")
    private Integer readerCount;

    /**
     * 机器类型标识 (如101代表一体机)
     */
    @TableField("machine_type")
    private Integer machineType;

    /**
     * IPv4地址
     */
    @TableField("ip_address")
    private String ipAddress;

    /**
     * 网关IPv4地址
     */
    @TableField("gateway_ip")
    private String gatewayIp;

    /**
     * 子网掩码
     */
    @TableField("net_mask")
    private String netMask;

    /**
     * 原始初始化请求载荷
     */
    @TableField("init_payload")
    private String initPayload;

    /**
     * 原始注册载荷
     */
    @TableField("registry_payload")
    private String registryPayload;

    /**
     * 最后检测到的IPv4地址
     */
    @TableField("last_known_ip")
    private String lastKnownIp;

    /**
     * 最后初始化握手时间
     */
    @TableField("last_init_time")
    private LocalDateTime lastInitTime;

    /**
     * 最后注册时间
     */
    @TableField("last_registry_time")
    private LocalDateTime lastRegistryTime;

    /**
     * 最后心跳时间
     */
    @TableField("last_heartbeat_time")
    private LocalDateTime lastHeartbeatTime;

    /**
     * 授权状态
     */
    @TableField("status")
    private String status;

    /**
     * 是否已授权
     */
    @TableField("authorized")
    private Boolean authorized;

    /**
     * 注册码
     */
    @TableField("registry_code")
    private String registryCode;

    /**
     * 备注
     */
    @TableField("remark")
    private String remark;
}
