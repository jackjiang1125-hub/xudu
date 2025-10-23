package com.xudu.center.video.camera.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * Persistent camera device entity storing the configuration coming from CameraVO.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("ipc_camera_device")
@Schema(description = "IPC camera device configuration")
public class CameraDevice extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @TableField("device_name")
    @Excel(name = "Device Name", width = 20)
    @Schema(description = "Human readable device name")
    private String deviceName;

    @TableField("camera_type")
    @Excel(name = "Camera Type", width = 15)
    @Schema(description = "Camera type or vendor category")
    private String type;

    @TableField("vendor")
    @Excel(name = "Vendor", width = 15, dicCode = "camera_vendor")
    @Schema(description = "Camera vendor (e.g. Hikvision, Dahua)")
    private String vendor;

    @TableField("username")
    @Excel(name = "Username", width = 15)
    @Schema(description = "RTSP authentication username")
    private String username;

    @TableField("password")
    @Excel(name = "Password", width = 15)
    @Schema(description = "RTSP authentication password")
   private String password;

    @TableField("onvif_username")
    @Excel(name = "ONVIF Username", width = 18)
    @Schema(description = "ONVIF service username")
    private String onvifUsername;

    @TableField("onvif_password")
    @Excel(name = "ONVIF Password", width = 18)
    @Schema(description = "ONVIF service password")
    private String onvifPassword;

    @TableField("ip_address")
    @Excel(name = "IP Address", width = 18)
    @Schema(description = "Camera IP address or hostname")
    private String ip;

    @TableField("port")
    @Excel(name = "Port", width = 12)
    @Schema(description = "RTSP port, defaults to 554")
    private String port;

    @TableField("stream_id")
    @Excel(name = "Stream ID", width = 20)
    @Schema(description = "Stream identifier used when registering to ZLM")
    private String streamId;

    @TableField("rtsp_transport")
    @Excel(name = "RTSP Transport", width = 12)
    @Schema(description = "RTSP transport protocol (tcp/udp)")
    private String rtspTransport;

    @TableField("on_demand")
    @Excel(name = "On Demand", width = 12, dicCode = "yn")
    @Schema(description = "Whether the stream is pulled on demand")
    private Boolean onDemand;

    @TableField("rtsp_path")
    @Excel(name = "RTSP Path", width = 18)
    @Schema(description = "RTSP tail path (for example /h264)")
    private String rtspPath;

    @TableField("hls_url")
    @Excel(name = "HLS URL", width = 30)
    @Schema(description = "HLS playback URL returned by ZLM")
    private String hlsUrl;

    @TableField("flv_url")
    @Excel(name = "FLV URL", width = 30)
    @Schema(description = "HTTP-FLV playback URL returned by ZLM")
    private String flvUrl;

    @TableField("webrtc_api")
    @Excel(name = "WebRTC API", width = 40)
    @Schema(description = "WebRTC API play URL returned by ZLM")
    private String webrtcApi;

    @TableField("parent_id")
    @Excel(name = "Parent ID", width = 32)
    @Schema(description = "Parent device id when representing a channel under an NVR")
    private String parentId;

    @TableField("role")
    @Excel(name = "Role", width = 12)
    @Schema(description = "ONVIF role, e.g. nvr, camera, channel")
    private String role;

    @TableField("channel_index")
    @Excel(name = "Channel Index", width = 12)
    @Schema(description = "Sequential channel index for NVR channel devices")
    private Integer channelIndex;

    @TableField("source_token")
    @Excel(name = "Source Token", width = 24)
    @Schema(description = "ONVIF source token for the channel")
    private String sourceToken;

    @TableField("profile_token")
    @Excel(name = "Profile Token", width = 24)
    @Schema(description = "Selected ONVIF profile token")
    private String profileToken;

    @TableField("profile_kind")
    @Excel(name = "Profile Kind", width = 12)
    @Schema(description = "ONVIF profile kind (main/sub)")
    private String profileKind;

    @TableField("profile_name")
    @Excel(name = "Profile Name", width = 20)
    @Schema(description = "ONVIF profile name")
    private String profileName;

    @TableField("onvif_rtsp_url")
    @Excel(name = "ONVIF RTSP", width = 40)
    @Schema(description = "RTSP url discovered from ONVIF service")
    private String onvifRtspUrl;

    @TableField("media_version")
    @Excel(name = "Media Version", width = 12)
    @Schema(description = "ONVIF media service version")
    private Integer mediaVersion;

    @TableField("channel_count")
    @Excel(name = "Channel Count", width = 12)
    @Schema(description = "Total channels reported by the ONVIF service")
    private Integer channelCount;

    @TableField("state_hash")
    @Excel(name = "State Hash", width = 32)
    @Schema(description = "ONVIF device state hash value")
    private String stateHash;

    @TableField("device_manufacturer")
    @Excel(name = "Manufacturer", width = 20)
    @Schema(description = "ONVIF device manufacturer")
    private String deviceManufacturer;

    @TableField("device_model")
    @Excel(name = "Model", width = 20)
    @Schema(description = "ONVIF device model")
    private String deviceModel;

    @TableField("device_firmware_version")
    @Excel(name = "Firmware", width = 20)
    @Schema(description = "ONVIF device firmware version")
    private String deviceFirmwareVersion;

    @TableField("device_serial_number")
    @Excel(name = "Serial", width = 20)
    @Schema(description = "ONVIF device serial number")
    private String deviceSerialNumber;

    @TableField("device_hardware_id")
    @Excel(name = "Hardware", width = 20)
    @Schema(description = "ONVIF device hardware id")
    private String deviceHardwareId;

    @TableField("capabilities_json")
    @Schema(description = "JSON encoded ONVIF capabilities payload")
    private String capabilitiesJson;

    @TableField("raw_onvif_payload")
    @Schema(description = "Raw ONVIF payload for future inspection")
    private String rawOnvifPayload;
}
