package org.jeecgframework.boot.wec.vo;

import lombok.Data;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class WecConsumeRecordDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tradeNo;
    private String cardNo;
    private String userId;
    private String userName;
    private String deviceId; // IoT Device ID or SN
    private String deviceName;
    private BigDecimal amount;
    private BigDecimal balance;
    private String type; // 1:Consume
    private String status; // 1:Success
    private Date consumeTime;
    private Integer recordNo;
}
