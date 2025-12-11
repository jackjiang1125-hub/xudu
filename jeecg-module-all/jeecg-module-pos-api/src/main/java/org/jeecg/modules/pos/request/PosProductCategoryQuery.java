package org.jeecg.modules.pos.request;

import lombok.Data;

import java.util.Date;

/**
 * 商品分类查询条件
 */
@Data
public class PosProductCategoryQuery {
    private String categoryName;
    private String categoryCode;
    private String status;
    private Date createTimeStart;
    private Date createTimeEnd;
}
