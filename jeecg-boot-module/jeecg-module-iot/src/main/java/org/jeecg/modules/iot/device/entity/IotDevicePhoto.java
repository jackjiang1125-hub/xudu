package org.jeecg.modules.iot.device.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.time.LocalDateTime;

/**
 * Punch photos uploaded by devices (table=ATTPHOTO).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_device_photo")
public class IotDevicePhoto extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @TableField("sn")
    private String sn;

    @TableField("pin")
    private String pin;

    @TableField("photo_name")
    private String photoName;

    @TableField("file_size")
    private Integer fileSize;

    @TableField("photo_base64")
    private String photoBase64;

    @TableField("photo_path")
    private String photoPath;

    @TableField("uploaded_time")
    private LocalDateTime uploadedTime;

    @TableField("raw_payload")
    private String rawPayload;

    @TableField("client_ip")
    private String clientIp;
}
