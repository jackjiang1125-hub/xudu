package org.jeecg.modules.pos.vo;



import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 账户交易流水VO
 */
@Data
public class PosAccountTransactionVO {
    private String id;
    private String accountId;
    private String accountNo;
    private String transactionNo;


    private Date occurTime;
    private String bizType;
    private String direction;
    private String walletType;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String channel;
    private String bizNo;
    private String remark;
}
