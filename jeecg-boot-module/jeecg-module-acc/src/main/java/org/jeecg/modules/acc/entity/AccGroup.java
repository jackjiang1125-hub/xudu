package org.jeecg.modules.acc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 门禁权限组实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("acc_group")
public class AccGroup extends JeecgEntity {

    /** 权限组名称 */
    @TableField("group_name")
    private String groupName;

    /** 关联的时间段ID */
    @TableField("period_id")
    private String periodId;

    /** 备注 */
    @TableField("remark")
    private String remark;
}