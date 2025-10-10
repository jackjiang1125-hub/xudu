package org.jeecgframework.boot.iot.query;

import lombok.Data;

@Data
public class IotDeviceQuery {
    private String sn;
    private String ipAddress;
    private String deviceType;
}
