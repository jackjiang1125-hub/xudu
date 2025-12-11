package org.jeecg.modules.pos.vo;



import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

/**
 * POS账户列表VO
 */
@Data
public class PosAccountListVO {
    private String id;
    private String accountNo;
    private String userId;
    private String userName;
    private String realName;
    private String gender;
    private String phone;
    private String departmentId;
    private String departmentName;
    private String position;
    private String accountType;
    private String accountLevel;
    private String accountStatus;
    private String registerChannel;


    private Date registerTime;


    private Date lastActiveTime;
    private BigDecimal totalBalance;
    private BigDecimal arrearsAmount;
    private Boolean autoRechargeEnabled;
    private BigDecimal autoRechargeThreshold;
    private BigDecimal autoRechargeAmount;
    private Boolean allowMealOrder;
    private Boolean allowDelivery;
    private Boolean allowSelfPickup;
    private List<String> tags;
    private String remark;
}
