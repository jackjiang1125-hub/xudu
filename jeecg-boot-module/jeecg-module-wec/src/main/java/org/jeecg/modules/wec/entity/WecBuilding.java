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
@TableName("wec_building")
public class WecBuilding extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @TableField("building_name")
    private String buildingName;

    @TableField("building_code")
    private String buildingCode;

    @TableField("area_id")
    private String areaId;
}
