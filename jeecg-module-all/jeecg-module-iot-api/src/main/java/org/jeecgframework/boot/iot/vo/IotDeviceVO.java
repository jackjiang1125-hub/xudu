package org.jeecgframework.boot.iot.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class IotDeviceVO{
    private static final long serialVersionUID = 1L;


    private String id;

    /**
     * Device serial number reported by the terminal.
     */

    private String sn;

    /**
     * Device type (acc, att, pos ...).
     */

    private String deviceType;

    /**
     * Human readable device name.
     */
    private String deviceName;

    /**
     * Firmware version string.
     */
    private String firmwareVersion;

    /**
     * Push protocol version reported by device.
     */
    private String pushVersion;

    /**
     * Number of locks reported by the device.
     */
    private Integer lockCount;

    /**
     * Number of readers reported by the device.
     */
    private Integer readerCount;

    /**
     * Machine type identifier (e.g. 101 for all-in-one devices). 设备类型 比如101 代表一体机
     */
    private Integer machineType;

    /**
     * IPv4 address configured on the device.
     */
    private String ipAddress;

    /**
     * Gateway IPv4 address configured on the device.
     */
    private String gatewayIp;

    /**
     * Netmask configured on the device.
     */
    private String netMask;

    /**
     * Last time the device registered itself.
     */
    private LocalDateTime lastRegistryTime;

    private Integer authorized;


}
