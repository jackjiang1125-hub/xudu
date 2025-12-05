package org.jeecgframework.boot.iot.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class IotWaterControlDeviceVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String sn;
    private String deviceName;
    private String ipAddress;
    private String deviceType;
    private String status; // PENDING, ONLINE, OFFLINE
    private LocalDateTime lastInitTime;
    private LocalDateTime lastHeartbeatTime;
}
