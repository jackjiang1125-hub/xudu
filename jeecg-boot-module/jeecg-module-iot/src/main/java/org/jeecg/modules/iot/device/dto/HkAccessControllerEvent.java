package org.jeecg.modules.iot.device.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HkAccessControllerEvent {

    private String deviceName;

    private Integer majorEventType;
    private Integer subEventType;

    private String cardNo;
    private Integer cardType;
    private String name;

    private Integer cardReaderKind;
    private Integer cardReaderNo;

    private Integer doorNo;

    private Integer verifyNo;

    @JsonProperty("employeeNoString")
    private String employeeNo;

    private Integer serialNo;

    private String userType;
    private String currentVerifyMode;

    private Integer statusValue;
    private String mask;
    private String helmet;

    private Integer picturesNumber;

    private Boolean purePwdVerifyEnable;
}
