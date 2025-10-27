package org.jeecg.modules.pos.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 餐厅信息VO
 */
@Data
@Schema(name = "PosRestaurantVO", description = "餐厅信息")
public class PosRestaurantVO {
    
    @Schema(description = "主键ID")
    private String id;
    
    @Schema(description = "餐厅编码")
    private String restaurantCode;
    
    @Schema(description = "餐厅名称")
    private String restaurantName;
    
    @Schema(description = "经营模式")
    private String category;
    
    @Schema(description = "餐厅类型")
    private String diningServiceType;
    
    @Schema(description = "备注")
    private String remark;
    
    // 创建人信息
    @Schema(description = "创建人")
    private String createBy;
    
    @Schema(description = "创建时间")
    private String createTime;
    
    @Schema(description = "更新人")
    private String updateBy;
    
    @Schema(description = "更新时间")
    private String updateTime;
    
    @Schema(description = "组织编码")
    private String sysOrgCode;
}