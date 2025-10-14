package org.jeecg.modules.acc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 权限组-设备关联实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("acc_group_device")
public class AccGroupDevice extends JeecgEntity {

    /** 组ID */
    @TableField("group_id")
    private String groupId;

    /** 设备ID（本系统设备ID或外部设备ID） */
    @TableField("device_id")
    private String deviceId;
}