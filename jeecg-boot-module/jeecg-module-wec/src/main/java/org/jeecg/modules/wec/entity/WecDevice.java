package org.jeecg.modules.wec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import org.jeecg.common.aspect.annotation.Dict;

/**
 * WecDevice Entity
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("wec_device")
public class WecDevice extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String sn;
    private String deviceName;
    private String ipAddress;
    
    @Dict(dicCode = "wec_device_type")
    private String deviceType;
    
    @Dict(dicCode = "device_status")
    private String status;
    
    private String remark;

    /**
     * 费率模板ID
     */
    @Dict(dictTable = "wec_rate_template", dicText = "template_name", dicCode = "id")
    private String rateTemplateId;

    /**
     * 最大时长(分钟)
     */
    private Integer maxTimeMinutes;

    /**
     * 最大计量(升/度)
     */
    private Integer maxVolumeLiters;

    /**
     * 二维码启用 (1:启用, 0:禁用)
     */
    private String qrEnabled;

    /**
     * 允许脱机 (1:是, 0:否)
     */
    private String allowOffline;

    /**
     * 名单模式 (0:白名单, 1:黑名单)
     */
    @Dict(dicCode = "namelist_mode")
    private Integer namelistMode;
    
    /**
     * 最近一次心跳时间（非数据库字段，由Redis填充）
     */
    @TableField(exist = false)
    private java.util.Date lastHeartbeatTime;

    /**
     * 总使用时间(秒)（非数据库字段，由Redis填充）
     */
    @TableField(exist = false)
    private Long totalUsageTime;

    /**
     * 总流量(ml)（非数据库字段，由Redis填充）
     */
    @TableField(exist = false)
    private Long totalUsageFlow;

    /**
     * 总金额(分)（非数据库字段，由Redis填充）
     */
    @TableField(exist = false)
    private Long totalUsageMoney;
}
