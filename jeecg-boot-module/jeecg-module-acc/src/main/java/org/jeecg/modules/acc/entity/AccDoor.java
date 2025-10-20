package org.jeecg.modules.acc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.time.LocalDateTime;

/**
 * 门禁门实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("acc_door")
public class AccDoor extends JeecgEntity {

    private static final long serialVersionUID = 1L;

    /** 设备名称 */
    @TableField("device_name")
    private String deviceName;

    /** 门名称 */
    @TableField("door_name")
    private String doorName;

    /** 验证方式（仅密码/仅卡/密码或卡/密码和卡） */
    @TableField("verification_method")
    private String verificationMethod;

    /** 操作间隔(秒)，范围0-254 */
    @TableField("operation_interval")
    private Integer operationInterval;

    /** 入反潜时长(分)，范围0-120 */
    @TableField("anti_backtracking_duration")
    private Integer antiBacktrackingDuration;

    /** 胁迫密码，最大6位整数 */
    @TableField("coercion_password")
    private String coercionPassword;

    /** 紧急状态密码，8位整数 */
    @TableField("emergency_password")
    private String emergencyPassword;

    /** 主机出入状态（入/出/双向） */
    @TableField("host_access_status")
    private String hostAccessStatus;

    /** 从机出入状态（入/出/双向） */
    @TableField("slave_access_status")
    private String slaveAccessStatus;

    /** 门编号 */
    @TableField("door_number")
    private Integer doorNumber;

    /** 门有效时间段 */
    @TableField("door_valid_time_range")
    private String doorValidTimeRange;

    /** 锁驱动时长(秒)，范围1-254 */
    @TableField("lock_drive_duration")
    private Integer lockDriveDuration;

    /** 门磁类型（无/常闭/常开） */
    @TableField("door_contact_type")
    private String doorContactType;

    /** 门磁延时(秒) */
    @TableField("door_contact_delay")
    private Integer doorContactDelay;

    /** 门常开时间段 */
    @TableField("door_always_open_time")
    private String doorAlwaysOpenTime;

    /** 上下班时间段 */
    @TableField("work_schedule_time")
    private String workScheduleTime;

    /** 多人开门操作间隔(秒)，范围5-60 */
    @TableField("multi_person_open_interval")
    private Integer multiPersonOpenInterval;

    /** 禁用报警提醒 */
    @TableField("disable_alarm_reminder")
    private Boolean disableAlarmReminder;

    /** 设备IP地址 */
    @TableField("ip_address")
    private String ipAddress;

}