package org.jeecg.modules.pos.request;


import lombok.Data;
import lombok.EqualsAndHashCode;



/**
 * 账户退款/取款请求
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PosAccountRefundRequest extends PosAccountRechargeRequest {


    private String originalBizNo;
}
