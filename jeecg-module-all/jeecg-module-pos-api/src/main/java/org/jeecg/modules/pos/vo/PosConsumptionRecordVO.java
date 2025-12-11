package org.jeecg.modules.pos.vo;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * 消费记录VO
 */
@Data
public class PosConsumptionRecordVO {
    private String id;
    private String recordNo;
    private String cardNo;
    private String customerId;
    private String customerName;
    private String customerType;
    private String type;
    private BigDecimal amount;
    private BigDecimal discountAmount;
    private BigDecimal discountPercent;
    private BigDecimal balanceAfter;
    private String deviceName;
    private String deviceCode;
    private String restaurantCode;
    private String scene;
    private String verifyMethod;
    private String channel;
    private String operator;
    private Date consumeTime;
    private String remark;
    private List<PosConsumptionDetailVO> details;
    private String createBy;
    private Date createTime;
    private String updateBy;
    private Date updateTime;
}