package org.jeecg.modules.pos.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 商品分类实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pos_product_category")
public class PosProductCategory extends JeecgEntity {

    /** 分类编号 */
    @TableField("category_code")
    private String categoryCode;

    /** 分类名称 */
    @TableField("category_name")
    private String categoryName;

    /** 展示别名 */
    @TableField("alias")
    private String alias;

    /** 分类简介 */
    @TableField("description")
    private String description;

    /** 状态(enabled:启用,disabled:停用) */
    @TableField("status")
    private String status;

    /** 排序号 */
    @TableField("display_order")
    private Integer displayOrder;

    /** 备注信息 */
    @TableField("remark")
    private String remark;
}