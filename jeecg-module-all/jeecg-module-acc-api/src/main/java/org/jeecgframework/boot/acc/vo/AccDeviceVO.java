package org.jeecgframework.boot.acc.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 门禁设备 VO
 * @author system
 * @date 2025-01-03
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class AccDeviceVO {
    private static final long serialVersionUID = 1L;

    private String id;

    /**
     * 设备序列号
     */
    private String sn;

    /**
     * 设备类型 (acc, att, pos ...)
     */
    private String deviceType;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 固件版本
     */
    private String firmwareVersion;

    /**
     * 推送协议版本
     */
    private String pushVersion;

    /**
     * 支持的锁数量
     */
    private Integer lockCount;

    /**
     * 支持的读头数量
     */
    private Integer readerCount;

    /**
     * 机器类型标识 (如101代表一体机)
     */
    private Integer machineType;

    /**
     * IPv4地址
     */
    private String ipAddress;

    /**
     * 网关IPv4地址
     */
    private String gatewayIp;

    /**
     * 子网掩码
     */
    private String netMask;

    /**
     * 最后注册时间
     */
    private LocalDateTime lastRegistryTime;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatTime;

    /**
     * 是否已授权
     */
    private Integer authorized;

    /**
     * 注册码
     */
    private String registryCode;

    /**
     * 备注
     */
    private String remark;
}
