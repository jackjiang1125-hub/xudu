package org.jeecg.modules.acc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 读头管理
 * @Author: jeecg-boot
 * @Date: 2025-01-26
 * @Version: V1.0
 */
@Data
@TableName("acc_reader")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
@Schema(description = "读头管理")
public class AccReader extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 读头名称
     */
    @Excel(name = "读头名称", width = 15)
    @Schema(description = "读头名称")
    @TableField("name")
    private String name;

    /**
     * 门名称
     */
    @Excel(name = "门名称", width = 15)
    @Schema(description = "门名称")
    @TableField("door_name")
    private String doorName;
    

    /**
     * 编号
     */
    @Excel(name = "编号", width = 15)
    @Schema(description = "编号")
    @TableField("num")
    private String num;

    /**
     * 出入类型
     */
    @Excel(name = "出入类型", width = 15)
    @Schema(description = "出入类型")
    @TableField("type")
    private String type;

    /**
     * 设备sn
     */
    @Schema(description = "设备sn")
    @TableField("device_sn")
    private String deviceSn;
}