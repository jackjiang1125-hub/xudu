package org.jeecg.modules.pos.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 商品分类VO
 */
@Data
public class PosProductCategoryVO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Schema(description = "主键ID")
    private String id;

    @Schema(description = "分类编号")
    private String categoryCode;

    @Schema(description = "分类名称")
    private String categoryName;

    @Schema(description = "展示别名")
    private String alias;

    @Schema(description = "分类简介")
    private String description;

    @Schema(description = "状态(enabled:启用,disabled:停用)")
    private String status;

    @Schema(description = "排序号")
    private Integer displayOrder;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "创建时间")
    private Date createTime;

    @Schema(description = "更新时间")
    private Date updateTime;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "更新人")
    private String updateBy;
}