package com.xudu.center.video.camera.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("xudu_video_stream")
@Schema(description = "Video stream detail for IPC channels under NVR")
public class VideoStream extends JeecgEntity {

    @TableField("video_id")
    @Excel(name = "Video ID", width = 36)
    @Schema(description = "Related video id (IPC)")
    private String videoId;

    @TableField("stream_no")
    @Excel(name = "Stream No", width = 12)
    @Schema(description = "Stream sequence number (1 main, 2 sub...)")
    private Integer streamNo;

    @TableField("track_id")
    @Excel(name = "Track ID", width = 12)
    @Schema(description = "Hikvision track id")
    private Integer trackId;

    @TableField("stream_name")
    @Excel(name = "Stream Name", width = 16)
    @Schema(description = "Stream readable name (main/sub/third)")
    private String streamName;

    @TableField("rtsp_url")
    @Excel(name = "RTSP URL", width = 40)
    @Schema(description = "RTSP url for the stream")
    private String rtspUrl;

    @TableField("video_codec")
    @Excel(name = "Video Codec", width = 15)
    @Schema(description = "Video codec")
    private String videoCodec;

    @TableField("width")
    @Excel(name = "Width", width = 10)
    @Schema(description = "Video width")
    private Integer width;

    @TableField("height")
    @Excel(name = "Height", width = 10)
    @Schema(description = "Video height")
    private Integer height;

    @TableField("frame_rate")
    @Excel(name = "Frame Rate", width = 12)
    @Schema(description = "Frame rate")
    private Integer frameRate;

    @TableField("bit_rate_type")
    @Excel(name = "Bit Rate Type", width = 15)
    @Schema(description = "Bit rate type (CBR/VBR)")
    private String bitRateType;

    @TableField("bit_rate")
    @Excel(name = "Bit Rate", width = 12)
    @Schema(description = "Bit rate")
    private Integer bitRate;

    @TableField("profile")
    @Excel(name = "Profile", width = 12)
    @Schema(description = "Profile level")
    private String profile;

    @TableField("gop")
    @Excel(name = "GOP", width = 10)
    @Schema(description = "GOP length")
    private Integer gop;

    @TableField("audio_enabled")
    @Excel(name = "Audio Enabled", width = 12)
    @Schema(description = "Whether audio is available")
    private Boolean audioEnabled;

    @TableField("audio_codec")
    @Excel(name = "Audio Codec", width = 15)
    @Schema(description = "Audio codec")
    private String audioCodec;

    @TableField("audio_sample_rate")
    @Excel(name = "Sample Rate", width = 12)
    @Schema(description = "Audio sample rate")
    private Integer audioSampleRate;

    @TableField("audio_channels")
    @Excel(name = "Audio Channels", width = 12)
    @Schema(description = "Audio channels")
    private Integer audioChannels;

    @TableField("audio_bit_rate")
    @Excel(name = "Audio Bit Rate", width = 15)
    @Schema(description = "Audio bit rate")
    private Integer audioBitRate;
}
