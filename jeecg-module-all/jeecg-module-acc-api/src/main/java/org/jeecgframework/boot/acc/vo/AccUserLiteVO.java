package org.jeecgframework.boot.acc.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import java.util.Date;

/**
 * 系统人员轻量信息 VO
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class AccUserLiteVO {
    private String id;
    private String username;
    private String realname;
    private String phone;
    private String orgCode;
    private String workNo;
    /** 头像（URL/相对路径/Base64） */
    private String avatar;
    /** 人脸抠图（URL/相对路径/Base64） */
    private String faceCutout;
    /** 人员卡号（SysUser.cardNumber） */
    private String cardNumber;
    /** 人员管理密码（SysUser.verifyPassword） */
    private String verifyPassword;

    /** 超级用户(0否,1是) 对应 SysUser.superUser */
    private Integer superUser;
    /** 设备操作权限 对应 SysUser.deviceOpPerm */
    private Integer deviceOpPerm;
    /** 扩展权限开关 对应 SysUser.extendAccess */
    private Boolean extendAccess;
    /** 禁止名单(0否,1是) 对应 SysUser.prohibitedRoster */
    private Boolean prohibitedRoster;
    /** 启用有效期 对应 SysUser.validTimeEnabled */
    private Boolean validTimeEnabled;
    /** 有效期开始 对应 SysUser.validStartTime */
    private Date validStartTime;
    /** 有效期结束 对应 SysUser.validEndTime */
    private Date validEndTime;
}