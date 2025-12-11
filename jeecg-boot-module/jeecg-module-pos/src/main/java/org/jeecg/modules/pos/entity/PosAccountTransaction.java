package org.jeecg.modules.pos.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * POS账户交易流水
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pos_account_transaction")
public class PosAccountTransaction extends JeecgEntity {

    private static final long serialVersionUID = 1L;

    /** 账户ID */
    @TableField("account_id")
    private String accountId;

    /** 账户编号 */
    @TableField("account_no")
    private String accountNo;

    /** 流水编号 */
    @TableField("transaction_no")
    private String transactionNo;

    /** 发生时间 */
    @TableField("occur_time")
    private Date occurTime;

    /** 业务类型 */
    @TableField("biz_type")
    private String bizType;

    /** 收支方向 */
    @TableField("direction")
    private String direction;

    /** 钱包类型 */
    @TableField("wallet_type")
    private String walletType;

    /** 金额 */
    @TableField("amount")
    private BigDecimal amount;

    /** 变动后余额 */
    @TableField("balance_after")
    private BigDecimal balanceAfter;

    /** 渠道 */
    @TableField("channel")
    private String channel;

    /** 业务单号 */
    @TableField("biz_no")
    private String bizNo;

    /** 备注 */
    @TableField("remark")
    private String remark;
}
