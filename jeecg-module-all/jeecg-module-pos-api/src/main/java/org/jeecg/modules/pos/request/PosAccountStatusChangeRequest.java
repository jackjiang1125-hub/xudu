package org.jeecg.modules.pos.request;


import lombok.Data;



/**
 * 账户状态变更请求
 */
@Data
public class PosAccountStatusChangeRequest {


    private String accountId;

    private String targetStatus;
    private String reason;
}
