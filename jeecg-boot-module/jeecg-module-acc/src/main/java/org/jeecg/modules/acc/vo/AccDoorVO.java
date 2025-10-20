package org.jeecg.modules.acc.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 门列表 VO
 */
@Data
public class AccDoorVO {
    private Integer id;
    private String deviceName;
    private String doorName;
    private String verificationMethod;
    private Integer operationInterval;
    private Integer antiBacktrackingDuration;
    private String coercionPassword;
    private String emergencyPassword;
    private String hostAccessStatus;
    private String slaveAccessStatus;
    private Integer doorNumber;
    private String doorValidTimeRange;
    private Integer lockDriveDuration;
    private String doorContactType;
    private Integer doorContactDelay;
    private String doorAlwaysOpenTime;
    private String workScheduleTime;
    private Integer multiPersonOpenInterval;
    private Boolean disableAlarmReminder;
    private String ipAddress;
}