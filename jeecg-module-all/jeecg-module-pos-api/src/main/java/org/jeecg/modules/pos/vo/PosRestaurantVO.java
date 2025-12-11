package org.jeecg.modules.pos.vo;


import lombok.Data;

/**
 * 餐厅信息VO
 */
@Data
public class PosRestaurantVO {
    private String id;
    private String restaurantCode;
    private String restaurantName;
    private String category;
    private String diningServiceType;
    private String remark;
    
    // 创建人信息
    private String createBy;
    private String createTime;
    private String updateBy;
    private String updateTime;
    private String sysOrgCode;
}