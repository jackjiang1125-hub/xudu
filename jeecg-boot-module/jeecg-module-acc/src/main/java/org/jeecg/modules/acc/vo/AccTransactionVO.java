package org.jeecg.modules.acc.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "门禁交易记录视图对象")
public class AccTransactionVO {
    private String id;
    private String sn;
    private String deviceName;
    private LocalDateTime logTime;
    private String pin;
    private String cardNo;
    private Integer eventAddr;
    private Integer eventCode;
    private Integer inoutStatus;
    private Integer verifyType;
    private Integer recordIndex;
    private Integer siteCode;
    private Integer linkId;
    private Integer maskFlag;
    private Integer temperature;
    private Integer convTemperature;
    private String clientIp;
    private String rawPayload;
    private String mediaFile;
    private Integer timeLevel;
    private String readerName;
    private String doorName;
}