package org.jeecg.modules.pos.request;


import lombok.Data;

import java.util.Date;

/**
 * 交易流水分页查询
 */
@Data
public class PosAccountTransactionPageQuery {


    private String accountId;


    private Integer pageNo = 1;


    private Integer pageSize = 10;
    private String bizType;
    private String direction;


    private Date occurTimeStart;

    private Date occurTimeEnd;
    private String keyword;
    private Boolean showAll = Boolean.FALSE;
}
