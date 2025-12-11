package org.jeecg.modules.pos.request;


import lombok.Data;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 账户查询条件
 */
@Data
public class PosAccountQuery {
    private String accountNo;
    private String realName;
    private String phone;
    private String accountStatus;
    private String accountType;
    private String accountLevel;
    private String deptIds;

    private Date registerTimeStart;

    private Date registerTimeEnd;

}
