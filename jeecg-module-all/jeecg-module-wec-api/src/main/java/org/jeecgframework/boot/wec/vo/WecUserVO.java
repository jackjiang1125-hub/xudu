package org.jeecgframework.boot.wec.vo;

import lombok.Data;
import lombok.experimental.Accessors;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Accessors(chain = true)
public class WecUserVO implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String userId;
    private String realName;
    private String workNo;
    private String cardNo;
    
    /**
     * 1:白名单 2:黑名单
     */
    private String userType;
    
    private BigDecimal balance;
    
    /**
     * 1:正常
     */
    private String status;
}