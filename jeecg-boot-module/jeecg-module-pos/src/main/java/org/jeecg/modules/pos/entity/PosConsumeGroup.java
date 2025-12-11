package org.jeecg.modules.pos.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.util.List;

/**
 * 消费人群
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@TableName(value = "pos_consume_group", autoResultMap = true)
public class PosConsumeGroup extends JeecgEntity {

    private static final long serialVersionUID = 1L;

    /** 人群编码 */
    @TableField("group_code")
    private String groupCode;

    /** 人群名称 */
    @TableField("group_name")
    private String groupName;

    /** 人群类型：STUDENT/TEACHER/MIXED 等 */
    @TableField("group_type")
    private String groupType;

    /** 成员来源方式：PERSON/DEPT */
    @TableField("member_mode")
    private String memberMode;

    /** 状态：0停用 1启用 */
    @TableField("status")
    private Integer status;

    /** 人群说明 */
    @TableField("description")
    private String description;

    /** 标签列表 */
    @TableField(value = "tags", typeHandler = JacksonTypeHandler.class)
    private List<String> tags;

    /** 备注 */
    @TableField("remark")
    private String remark;
}
