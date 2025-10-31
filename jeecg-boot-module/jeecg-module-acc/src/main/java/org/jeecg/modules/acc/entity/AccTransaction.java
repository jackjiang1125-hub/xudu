package org.jeecg.modules.acc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("acc_transaction")
@Schema(description = "门禁交易记录（由 rtlog 异步处理入库）")
public class AccTransaction extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @Excel(name = "设备序列号", width = 15)
    @Schema(description = "设备序列号")
    @TableField("sn")
    private String sn;

    @Excel(name = "设备名称", width = 20)
    @Schema(description = "设备名称")
    @TableField("device_name")
    private String deviceName;

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

    @Schema(description = "事件地址")
    @TableField("event_addr")
    private Integer eventAddr;

    @Schema(description = "事件代码")
    @TableField("event_code")
    private Integer eventCode;

    @Schema(description = "事件级别")
    @TableField("event_level")
    private Integer eventLevel;

    @Schema(description = "进出状态(0:出, 1:进)")
    @TableField("inout_status")
    private Integer inoutStatus;

    @Schema(description = "验证方式")
    @TableField("verify_type")
    private Integer verifyType;

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

    @Schema(description = "媒体文件（图片路径）")
    @TableField("media_file")
    private String mediaFile;

    @Schema(description = "读头名称")
    @TableField("reader_name")
    private String readerName;

    @Schema(description = "门名称")
    @TableField("door_name")
    private String doorName;
}