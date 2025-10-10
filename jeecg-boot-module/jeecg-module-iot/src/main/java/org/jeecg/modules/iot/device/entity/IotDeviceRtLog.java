package org.jeecg.modules.iot.device.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * Real-time log reported by an access control device (table=rtlog).
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("iot_device_rtlog")
@Schema(description = "门禁设备实时记录")
public class IotDeviceRtLog extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @Excel(name = "设备序列号", width = 15)
    @Schema(description = "设备序列号")
    @TableField("sn")
    private String sn;

    @Excel(name = "记录时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "记录时间")
    @TableField("log_time")
    private LocalDateTime logTime;

    @Excel(name = "用户PIN", width = 10)
    @Schema(description = "用户PIN")
    @TableField("pin")
    private String pin;

    @Excel(name = "卡号", width = 15)
    @Schema(description = "卡号")
    @TableField("card_no")
    private String cardNo;

    @Excel(name = "事件地址", width = 10)
    @Schema(description = "事件地址")
    @TableField("event_addr")
    private Integer eventAddr;

    @Excel(name = "事件代码", width = 10)
    @Schema(description = "事件代码")
    @TableField("event_code")
    private Integer eventCode;

    @Excel(name = "进出状态", width = 10, replace = {"进_1", "出_0"})
    @Schema(description = "进出状态(0:出, 1:进)")
    @TableField("inout_status")
    private Integer inoutStatus;

    @Excel(name = "验证方式", width = 15, replace = {"密码_1", "卡片_2", "密码+卡片_3", "指纹_4", "指纹+密码_5", "指纹+卡片_6", "指纹+密码+卡片_7", "人脸_8", "人脸+密码_9", "人脸+卡片_10", "人脸+密码+卡片_11", "掌纹_15", "其他_200"})
    @Schema(description = "验证方式")
    @TableField("verify_type")
    private Integer verifyType;

    @Excel(name = "记录索引", width = 10)
    @Schema(description = "记录索引")
    @TableField("record_index")
    private Integer recordIndex;

    @Schema(description = "站点代码")
    @TableField("site_code")
    private Integer siteCode;

    @Schema(description = "链路ID")
    @TableField("link_id")
    private Integer linkId;

    @Schema(description = "口罩标识")
    @TableField("mask_flag")
    private Integer maskFlag;

    @Schema(description = "温度")
    @TableField("temperature")
    private Integer temperature;

    @Schema(description = "转换温度")
    @TableField("conv_temperature")
    private Integer convTemperature;

    @Schema(description = "原始载荷")
    @TableField("raw_payload")
    private String rawPayload;

    @Excel(name = "客户端IP", width = 15)
    @Schema(description = "客户端IP")
    @TableField("client_ip")
    private String clientIp;
}
