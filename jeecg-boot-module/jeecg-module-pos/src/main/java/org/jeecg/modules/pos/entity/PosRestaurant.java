package org.jeecg.modules.pos.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * 餐厅信息实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("pos_restaurant")
@Schema(name = "PosRestaurant", description = "餐厅信息")
public class PosRestaurant extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 餐厅编码
     */
    @Excel(name = "餐厅编码", width = 15)
    @Schema(description = "餐厅编码")
    @TableField("restaurant_code")
    private String restaurantCode;

    /**
     * 餐厅名称
     */
    @Excel(name = "餐厅名称", width = 20)
    @Schema(description = "餐厅名称")
    @TableField("restaurant_name")
    private String restaurantName;

    /**
     * 经营模式 (meal_type: 餐别, product: 商品)
     */
    @Excel(name = "经营模式", width = 15, dicCode = "category")
    @Schema(description = "经营模式 (meal_type: 餐别, product: 商品)")
    @TableField("category")
    private String category;

    /**
     * 餐厅类型 (restaurant: 餐厅, supermarket: 超市)
     */
    @Excel(name = "餐厅类型", width = 15, dicCode = "dining_service_type")
    @Schema(description = "餐厅类型 (restaurant: 餐厅, supermarket: 超市)")
    @TableField("dining_service_type")
    private String diningServiceType;

    /**
     * 备注
     */
    @Excel(name = "备注", width = 30)
    @Schema(description = "备注")
    @TableField("remark")
    private String remark;
}