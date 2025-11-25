package org.jeecgframework.boot.acc.vo;

import lombok.Data;

@Data
public class DeviceCapacityVO {
  private String sn;
  private String deviceName;
  private String personCount;
  private String biophotoCount;
  private String fingerCount;
  private String fingerVersion;
  private String fingerVeinCount;
  private String faceCount;
  private String faceVersion;
  private String palmVeinCount;
  private String palmVeinVersion;
}