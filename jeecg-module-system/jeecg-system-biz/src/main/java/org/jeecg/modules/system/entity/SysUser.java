package org.jeecg.modules.system.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 登录账号
     */
    @Excel(name = "登录账号", width = 15)
    private String username;

    /**
     * 真实姓名
     */
    @Excel(name = "真实姓名", width = 15)
    private String realname;

    /**
     * 密码
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    /**
     * md5密码盐
     */
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String salt;

    /**
     * 头像
     */
    @Excel(name = "头像", width = 15,type = 2)
    private String avatar;

    /**
     * 人脸抠图
     */
    @Excel(name = "人脸抠图", width = 15,type = 2)
    private String faceCutout;

    /**
     * 用户类型(1系统用户,2业务用户)
     */
    @Excel(name = "用户类型", width = 15, dicCode = "user_type")
    @Dict(dicCode = "user_type")
    private Integer userType;

    /**
     * 生日
     */
    @Excel(name = "生日", width = 15, format = "yyyy-MM-dd")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private Date birthday;

    /**
     * 性别（1：男 2：女）
     */
    @Excel(name = "性别", width = 15,dicCode="sex")
    @Dict(dicCode = "sex")
    private Integer sex;

    /**
     * 电子邮件
     */
    @Excel(name = "电子邮件", width = 15)
    private String email;

    /**
     * 电话
     */
    @Excel(name = "电话", width = 15)
    private String phone;

    /**
     * 登录选择部门编码
     */
    private String orgCode;
    /**
     * 登录选择租户ID
     */
    private Integer loginTenantId;

    /**部门名称*/
    private transient String orgCodeTxt;

    /**
     * 状态(1：正常  2：冻结 ）
     */
    @Excel(name = "状态", width = 15,dicCode="user_status")
    @Dict(dicCode = "user_status")
    private Integer status;

    /**
     * 删除状态（0，正常，1已删除）
     */
    @Excel(name = "删除状态", width = 15,dicCode="del_flag")
    @TableLogic
    private Integer delFlag;

    /**
     * 工号，唯一键
     */
    @Excel(name = "工号", width = 15)
    private String workNo;

    /**
     * 职务，关联职务表
     */
    @Excel(name = "职务", width = 15)
    @Dict(dictTable ="sys_position",dicText = "name",dicCode = "id")
    @TableField(exist = false)
    private String post;

    /**
     * 座机号
     */
    @Excel(name = "座机号", width = 15)
    private String telephone;

    /**
     * 卡号（业务用户可选）
     */
    @Excel(name = "卡号", width = 15)
    @TableField("card_number")
    private String cardNumber;

    /**
     * 验证密码（业务用户可选，仅写入，不回显）
     */
    // @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @TableField("verify_password")
    private String verifyPassword;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;
    /**
     * 同步工作流引擎1同步0不同步
     */
    private Integer activitiSync;

    /**
     * 身份（0 普通成员 1 上级）
     */
    @Excel(name="（1普通成员 2上级）",width = 15)
    private Integer userIdentity;

    /**
     * 负责部门
     */
    @Excel(name="负责部门",width = 15,dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    @Dict(dictTable ="sys_depart",dicText = "depart_name",dicCode = "id")
    private String departIds;

    /**
     * 多租户ids临时用，不持久化数据库(数据库字段不存在)
     */
    @TableField(exist = false)
    private String relTenantIds;

    /**设备id uniapp推送用*/
    private String clientId;

    /**
     * 登录首页地址
     */
    @TableField(exist = false)
    private String homePath;

    /**
     * 职位名称
     */
    @TableField(exist = false)
    private String postText;

    /**
     * 流程状态
     */
    private String bpmStatus;

    /**
     * 超级用户(0否,1是)
     */
    @Excel(name = "超级用户(0否,1是)", width = 15)
    @TableField("super_user")
    private Integer superUser;

    /**
     * 设备操作权限(1一般人员,2管理员,3操作员)
     */
    @Excel(name = "设备操作权限(1一般人员,2管理员,3操作员)", width = 20)
    @TableField("device_op_perm")
    private Integer deviceOpPerm;

    /**
     * 延长通行(0否,1是)
     */
    @Excel(name = "延长通行(0否,1是)", width = 15)
    @TableField("extend_access")
    private Boolean extendAccess;

    /**
     * 禁止名单(0否,1是)
     */
    @Excel(name = "禁止名单(0否,1是)", width = 15)
    @TableField("prohibited_roster")
    private Boolean prohibitedRoster;

    /**
     * 启用有效时间(0否,1是)
     */
    @Excel(name = "启用有效时间(0否,1是)", width = 18)
    @TableField("valid_time_enabled")
    private Boolean validTimeEnabled;

    /**
     * 有效开始时间
     */
    @Excel(name = "有效开始时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("valid_start_time")
    private Date validStartTime;

    /**
     * 有效结束时间
     */
    @Excel(name = "有效结束时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("valid_end_time")
    private Date validEndTime;

    /**
     * 是否已经绑定第三方
     */
    @TableField(exist = false)
    private boolean izBindThird;
}
