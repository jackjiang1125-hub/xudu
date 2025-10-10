package org.jeecg.modules.events.acc;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RegisterAccDeviceEvent {

    private String sn;

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
     * 原始初始化请求载荷
     */
    private String initPayload;

    /**
     * 原始注册载荷
     */
    private String registryPayload;

    /**
     * 最后检测到的IPv4地址
     */
    private String lastKnownIp;

    /**
     * 最后初始化握手时间
     */
    private LocalDateTime lastInitTime;

    /**
     * 最后注册时间
     */
    private LocalDateTime lastRegistryTime;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeatTime;

    /**
     * 授权状态
     */
    private String status;

    /**
     * 是否已授权
     */
    private Boolean authorized;

    /**
     * 注册码
     */
    private String registryCode;

    /**
     * 备注
     */
    private String remark;

}
