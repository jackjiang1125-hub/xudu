package org.jeecg.modules.iot.device.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * 设备照片信息 VO
 * @author system
 * @date 2025-01-03
 */
@Data
@Schema(description = "设备照片信息")
public class AccDevicePhotoVO {

    @Schema(description = "照片路径")
    private String photoPath;

    @Schema(description = "照片名称")
    private String photoName;

    @Schema(description = "文件大小")
    private Long fileSize;

    @Schema(description = "上传时间")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    private Date uploadedTime;
}