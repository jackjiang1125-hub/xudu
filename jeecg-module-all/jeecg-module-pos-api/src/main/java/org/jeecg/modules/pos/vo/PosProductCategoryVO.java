package org.jeecg.modules.pos.vo;
import lombok.Data;
import java.io.Serializable;
import java.util.Date;
/**
 * 商品分类VO
 */
@Data
public class PosProductCategoryVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String categoryCode;
    private String categoryName;
    private String alias;
    private String description;
    private String status;
    private Integer displayOrder;
    private String remark;
    private Date createTime;
    private Date updateTime;
    private String createBy;
    private String updateBy;
}
