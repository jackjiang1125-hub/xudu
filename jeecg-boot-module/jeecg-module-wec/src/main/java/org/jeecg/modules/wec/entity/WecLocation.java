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
@TableName("wec_location")
public class WecLocation extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @TableField("location_name")
    private String locationName;

    @TableField("remark")
    private String remark;
}
