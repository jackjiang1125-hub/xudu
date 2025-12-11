package org.jeecg.modules.pos.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

/**
 * 消费规则绑定
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "pos_consume_rule_binding", autoResultMap = true)
public class PosConsumeRuleBinding extends JeecgEntity {

    private static final long serialVersionUID = 1L;

    /** 规则ID */
    @TableField("rule_id")
    private String ruleId;

    /** 绑定类型：PERSON/DEPT/GROUP */
    @TableField("bind_type")
    private String bindType;

    /** 绑定对象ID：账户ID/部门ID/人群ID */
    @TableField("target_id")
    private String targetId;

    /** 绑定对象名称快照 */
    @TableField("target_name")
    private String targetName;

    /** 额外快照JSON */
    @TableField(value = "target_extra", typeHandler = JacksonTypeHandler.class)
    private Object targetExtra;

    /** 备注 */
    @TableField("remark")
    private String remark;
}
