package org.jeecg.modules.pos.request;

import lombok.Data;

import java.util.Date;

/**
 * 餐厅查询条件
 */
@Data
public class PosRestaurantQuery {
    private String restaurantName;
    private String restaurantCode;
    private String category;
    private String diningServiceType;
    private Date createTimeStart;
    private Date createTimeEnd;
}
