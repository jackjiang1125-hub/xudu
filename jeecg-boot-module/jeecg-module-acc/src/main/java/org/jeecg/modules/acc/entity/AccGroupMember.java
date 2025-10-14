package org.jeecg.modules.acc.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 权限组-人员关联实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName("acc_group_member")
public class AccGroupMember extends JeecgEntity {

    /** 组ID */
    @TableField("group_id")
    private String groupId;

    /** 人员ID（外部系统/人事系统ID或本系统用户ID） */
    @TableField("member_id")
    private String memberId;
}