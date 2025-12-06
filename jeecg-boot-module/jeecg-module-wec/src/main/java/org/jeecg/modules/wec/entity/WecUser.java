package org.jeecg.modules.wec.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecg.common.aspect.annotation.Dict;
import java.math.BigDecimal;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("wec_user")
public class WecUser extends JeecgEntity {
    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    private String userId;
    private String realName;
    private String workNo;
    private String cardNo;
    
    @Dict(dicCode = "wec_user_type")
    private String userType; // 1:白名单 2:黑名单
    
    private BigDecimal balance;
    
    @Dict(dicCode = "valid_status")
    private String status;
    
    private String remark;
}
