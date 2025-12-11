package org.jeecg.modules.pos.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 消费人群成员
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "pos_consume_group_member", autoResultMap = true)
public class PosConsumeGroupMember extends JeecgEntity {

    private static final long serialVersionUID = 1L;

    /** 消费人群ID */
    @TableField("group_id")
    private String groupId;

    /** 成员类型：ACCOUNT/DEPT */
    @TableField("member_type")
    private String memberType;

    /** 成员ID：账户ID 或 部门ID */
    @TableField("member_id")
    private String memberId;

    /** 成员名称快照 */
    @TableField("member_name")
    private String memberName;

    /** 所属部门ID */
    @TableField("dept_id")
    private String deptId;

    /** 所属部门名称 */
    @TableField("dept_name")
    private String deptName;

    /** 额外快照JSON */
    @TableField(value = "ext_json", typeHandler = JacksonTypeHandler.class)
    private Object extJson;
}
