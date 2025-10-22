package com.xudu.center.video.camera.vo;


import lombok.Data;
import com.xudu.center.video.camera.constant.CameraRtspConstants;

@Data
public class CameraVO {


    private String id;
    private String deviceName;
    private String type;

    private String username;
    private String password;
    private String rtspUrl;
    private String ip;
    private String port;
    private String vendor;
    private String onvifUsername;
    private String onvifPassword;

    private String streamId;
    private String rtspTransport="tcp";//tcp

    private Boolean onDemand = true;

    private String role;
    private String parentId;
    private Integer channelIndex;
    private String sourceToken;
    private String profileToken;
    private String profileKind;
    private String profileName;
    private String onvifRtspUrl;
    private Integer mediaVersion;
    private Integer channelCount;
    private String stateHash;
    private String deviceManufacturer;
    private String deviceModel;
    private String deviceFirmwareVersion;
    private String deviceSerialNumber;
    private String deviceHardwareId;
    private String capabilitiesJson;
    private String rawOnvifPayload;

    public String getRtspUrl() {
        if (rtspUrl != null && !rtspUrl.isEmpty()) {
            return rtspUrl;
        }
        if (onvifRtspUrl != null && !onvifRtspUrl.isEmpty()) {
            return onvifRtspUrl;
        }
        String vendorCode = (vendor != null && !vendor.isEmpty()) ? vendor : type;
        return CameraRtspConstants.buildRtspUrl(vendorCode, ip, port);
    }


}
