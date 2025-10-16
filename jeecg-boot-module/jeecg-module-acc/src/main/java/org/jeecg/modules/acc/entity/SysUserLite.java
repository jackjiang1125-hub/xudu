package org.jeecg.modules.acc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 轻量级系统用户实体，仅用于跨模块读取必要字段。
 */
@Data
@TableName("sys_user")
public class SysUserLite {
    @TableId
    private String id;

    private String username;

    private String realname;

    private String phone;

    @TableField("org_code")
    private String orgCode;

    private Integer status;
}