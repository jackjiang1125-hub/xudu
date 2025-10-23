package com.xudu.center.video.camera.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * Video query parameters
 */
@Data
@Schema(description = "视频流查询参数")
public class VideoQuery {
    
    @Schema(description = "视频流名称")
    private String name;
    
    @Schema(description = "设备IP地址")
    private String ip;
    
    @Schema(description = "设备端口")
    private String port;
    
    @Schema(description = "设备制造商")
    private String manufacturer;
    
    @Schema(description = "设备型号")
    private String model;
    
    @Schema(description = "设备类型(nvr/ipc)")
    private String type;
    
    @Schema(description = "在线状态")
    private String status;
    
    @Schema(description = "流名称")
    private String stream;
    
    @Schema(description = "ZLM应用名")
    private String app;
}
