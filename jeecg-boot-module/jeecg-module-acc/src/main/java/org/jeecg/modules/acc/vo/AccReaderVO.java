package org.jeecg.modules.acc.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 读头管理VO
 * @Author: jeecg-boot
 * @Date: 2025-01-26
 * @Version: V1.0
 */
@Data
@Schema(description = "读头管理VO")
public class AccReaderVO implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    @Schema(description = "主键")
    private String id;

    /**
     * 创建人
     */
    @Schema(description = "创建人")
    private String createBy;

    /**
     * 创建日期
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建日期")
    private Date createTime;

    /**
     * 更新人
     */
    @Schema(description = "更新人")
    private String updateBy;

    /**
     * 更新日期
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "更新日期")
    private Date updateTime;

    /**
     * 所属部门
     */
    @Schema(description = "所属部门")
    private String sysOrgCode;

    /**
     * 读头id
     */
    @Schema(description = "读头id")
    private String readerId;

    /**
     * 读头名称
     */
    @Schema(description = "读头名称")
    private String name;

    /**
     * 门名称
     */
    @Schema(description = "门名称")
    private String doorName;

    /**
     * 编号
     */
    @Schema(description = "编号")
    private String num;

    /**
     * 出入类型
     */
    @Schema(description = "出入类型")
    private String type;
}