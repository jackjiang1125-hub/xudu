package org.jeecgframework.boot.acc.query;

import lombok.Data;

/**
 * 门禁设备查询条件 DTO
 * @author system
 * @date 2025-01-03
 */
@Data
public class AccDeviceQuery {
    /**
     * 设备序列号
     */
    private String sn;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 设备名称
     */
    private String deviceName;
    
    /**
     * 设备类型
     */
    private String deviceType;
    
    /**
     * 是否已授权
     */
    private Boolean authorized;
}
