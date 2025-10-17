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
}