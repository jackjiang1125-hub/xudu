package org.jeecg.modules.iot.device.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class HkEventWrapper {

    private String ipAddress;
    private String ipv6Address;
    private Integer portNo;
    private String protocol;
    private String macAddress;

    @JsonProperty("channelID")
    private Integer channelId;

    private String dateTime;
    private Integer activePostCount;
    private String eventType;
    private String eventState;
    private String eventDescription;

    @JsonProperty("shortSerialNumber")
    private String shortSerialNumber;

    @JsonProperty("AccessControllerEvent")
    private HkAccessControllerEvent accessControllerEvent;
}

