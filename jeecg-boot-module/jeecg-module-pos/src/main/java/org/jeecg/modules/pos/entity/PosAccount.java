package org.jeecg.modules.pos.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * POS账户实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "pos_account", autoResultMap = true)
public class PosAccount extends JeecgEntity {

    private static final long serialVersionUID = 1L;

    /** 账户编号 */
    @TableField("account_no")
    private String accountNo;

    /** 关联系统用户ID */
    @TableField("user_id")
    private String userId;

    /** 系统用户名 */
    @TableField("user_name")
    private String userName;

    /** 真实姓名 */
    @TableField("real_name")
    private String realName;

    /** 性别 */
    @TableField("gender")
    private String gender;

    /** 手机号 */
    @TableField("phone")
    private String phone;

    /** 部门ID */
    @TableField("dept_id")
    private String deptId;

    /** 部门名称（冗余） */
    @TableField("dept_name")
    private String deptName;

    /** 岗位/职位 */
    @TableField("position")
    private String position;

    /** 身份证号 */
    @TableField("id_card_no")
    private String idCardNo;

    /** 工牌编号 */
    @TableField("badge_no")
    private String badgeNo;

    /** 账户类型 */
    @TableField("account_type")
    private String accountType;

    /** 账户等级 */
    @TableField("account_level")
    private String accountLevel;

    /** 账户状态 */
    @TableField("account_status")
    private String accountStatus;

    /** 注册渠道 */
    @TableField("register_channel")
    private String registerChannel;

    /** 注册时间 */
    @TableField("register_time")
    private Date registerTime;

    /** 最近活跃时间 */
    @TableField("last_active_time")
    private Date lastActiveTime;

    /** 现金钱包余额 */
    @TableField("cash_wallet")
    private BigDecimal cashWallet;

    /** 补贴钱包余额 */
    @TableField("subsidy_wallet")
    private BigDecimal subsidyWallet;

    /** 礼品钱包余额 */
    @TableField("gift_wallet")
    private BigDecimal giftWallet;

    /** 冻结金额 */
    @TableField("frozen_amount")
    private BigDecimal frozenAmount;

    /** 授信额度 */
    @TableField("credit_limit")
    private BigDecimal creditLimit;

    /** 欠费金额 */
    @TableField("arrears_amount")
    private BigDecimal arrearsAmount;

    /** 总余额 */
    @TableField("total_balance")
    private BigDecimal totalBalance;

    /** 每日消费限额 */
    @TableField("daily_consumption_limit")
    private BigDecimal dailyConsumptionLimit;

    /** 单笔消费限额 */
    @TableField("single_consumption_limit")
    private BigDecimal singleConsumptionLimit;

    /** 每日充值限额 */
    @TableField("daily_recharge_limit")
    private BigDecimal dailyRechargeLimit;

    /** 单笔充值限额 */
    @TableField("single_recharge_limit")
    private BigDecimal singleRechargeLimit;

    /** 自动充值开关 */
    @TableField("auto_recharge_enabled")
    private Boolean autoRechargeEnabled;

    /** 自动充值阈值 */
    @TableField("auto_recharge_threshold")
    private BigDecimal autoRechargeThreshold;

    /** 自动充值金额 */
    @TableField("auto_recharge_amount")
    private BigDecimal autoRechargeAmount;

    /** 订餐权限 */
    @TableField("allow_meal_order")
    private Boolean allowMealOrder;

    /** 外送权限 */
    @TableField("allow_delivery")
    private Boolean allowDelivery;

    /** 自取权限 */
    @TableField("allow_self_pickup")
    private Boolean allowSelfPickup;

    /** 绑定设备 */
    @TableField(value = "bind_devices", typeHandler = JacksonTypeHandler.class)
    private List<String> bindDevices;

    /** 关联卡号 */
    @TableField(value = "associated_cards", typeHandler = JacksonTypeHandler.class)
    private List<String> associatedCards;

    /** 标签 */
    @TableField(value = "tags", typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /** 是否已挂失 */
    @TableField("loss_reported")
    private Boolean lossReported;

    /** 最近挂失时间 */
    @TableField("last_loss_report_time")
    private Date lastLossReportTime;

    /** 最近支付密码重置时间 */
    @TableField("last_password_reset_time")
    private Date lastPasswordResetTime;

    /** 最近补卡时间 */
    @TableField("last_card_reissue_time")
    private Date lastCardReissueTime;

    /** 备注 */
    @TableField("remark")
    private String remark;
}
