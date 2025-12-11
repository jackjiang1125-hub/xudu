package org.jeecg.modules.pos.request;


import lombok.Data;



/**
 * 通用账户操作请求
 */
@Data
public class PosAccountSimpleOperateRequest {


    private String accountId;
    private String remark;
}
