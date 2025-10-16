package org.jeecg.modules.acc.vo;

import lombok.Data;

/**
 * 权限组设备VO
 */
@Data
public class AccDeviceVO {
    private String id;
    private String deviceName;
    private String deviceCode;
    private String deviceType;
    private String location;
    private String status;
    private String ipAddress;
    private String remark;
    // ===== 前端展示补充字段（与 accgroup.data.ts 对齐）=====
    private String sn;         // 序列号（与 deviceCode 同步或来源于实体的 sn）
    private String authorized; // 授权状态展示（已授权/未授权）
}