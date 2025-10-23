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
 * Video entity for storing video stream configuration and status
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("xudu_video_device")
@Schema(description = "Video stream configuration and status")
public class Video extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @TableField("name")
    @Excel(name = "Name", width = 20)
    @Schema(description = "Video stream name")
    private String name;

    @TableField("username")
    @Excel(name = "Username", width = 15)
    @Schema(description = "Authentication username")
    private String username;

    @TableField("password")
    @Excel(name = "Password", width = 15)
    @Schema(description = "Authentication password")
    private String password;

    @TableField("ip")
    @Excel(name = "IP Address", width = 18)
    @Schema(description = "Device IP address")
    private String ip;

    @TableField("port")
    @Excel(name = "Port", width = 12)
    @Schema(description = "Device port")
    private String port;

    @TableField("manufacturer")
    @Excel(name = "Manufacturer", width = 20)
    @Schema(description = "Device manufacturer")
    private String manufacturer;

    @TableField("model")
    @Excel(name = "Model", width = 20)
    @Schema(description = "Device model")
    private String model;

    @TableField("type")
    @Excel(name = "Type", width = 15)
    @Schema(description = "Device type (nvr/ipc)")
    private String type;

    @TableField("app")
    @Excel(name = "App", width = 15)
    @Schema(description = "ZLM application name")
    private String app;

    @TableField("status")
    @Excel(name = "Status", width = 15)
    @Schema(description = "Online status")
    private String status;

    @TableField("rtsp_url")
    @Excel(name = "RTSP URL", width = 40)
    @Schema(description = "RTSP stream URL")
    private String rtspUrl;

    @TableField("hls_url")
    @Excel(name = "HLS URL", width = 40)
    @Schema(description = "HLS playback URL")
    private String hlsUrl;

    @TableField("webrtc_url")
    @Excel(name = "WebRTC URL", width = 40)
    @Schema(description = "WebRTC playback URL")
    private String webRtcUrl;

    @TableField("stream")
    @Excel(name = "Stream", width = 20)
    @Schema(description = "Stream name")
    private String stream;

    @TableField("ffmpeg_cmd_key")
    @Excel(name = "FFmpeg Command Key", width = 25)
    @Schema(description = "FFmpeg transcoding template key")
    private String ffmpegCmdKey;

    @TableField("video_codec")
    @Excel(name = "Video Codec", width = 15)
    @Schema(description = "Video codec information")
    private String videoCodec;

    @TableField("audio_codec")
    @Excel(name = "Audio Codec", width = 15)
    @Schema(description = "Audio codec information")
    private String audioCodec;
}
