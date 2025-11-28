package org.jeecg.modules.wec.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("wec_floor")
public class WecFloor extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @TableField("floor_name")
    private String floorName;

    @TableField("floor_code")
    private String floorCode;

    @TableField("building_id")
    private String buildingId;
}
