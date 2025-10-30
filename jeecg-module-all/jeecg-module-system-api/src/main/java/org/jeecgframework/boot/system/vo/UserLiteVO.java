package org.jeecgframework.boot.system.vo;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * 系统人员轻量信息 VO
 */
@Data
@EqualsAndHashCode
@Accessors(chain = true)
public class UserLiteVO {
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
}