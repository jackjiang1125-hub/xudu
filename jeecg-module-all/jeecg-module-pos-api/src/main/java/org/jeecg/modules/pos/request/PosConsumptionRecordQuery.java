package org.jeecg.modules.pos.request;

import lombok.Data;

import java.util.Date;

/**
 * 消费记录查询条件
 */
@Data
public class PosConsumptionRecordQuery {
    private String cardNo;
    private String customerId;
    private String customerName;
    private String type;
    private String deviceCode;
    private String deviceName;
    private String restaurantCode;
    private Date consumeTimeStart;
    private Date consumeTimeEnd;
}
