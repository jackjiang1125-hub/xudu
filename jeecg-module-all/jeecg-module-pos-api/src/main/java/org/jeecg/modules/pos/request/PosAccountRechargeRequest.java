package org.jeecg.modules.pos.request;


import lombok.Data;


import java.math.BigDecimal;

/**
 * 账户充值请求
 */
@Data
public class PosAccountRechargeRequest {


    private String accountId;


    private String walletType;


    private BigDecimal amount;
    private String channel;
    private String referenceNo;
    private String remark;
}
