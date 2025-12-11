package org.jeecg.modules.pos.request;


import lombok.Data;


import java.util.List;

/**
 * 账户资料更新请求
 */
@Data
public class PosAccountProfileUpdateRequest {


    private String accountId;


    private String realName;


    private String phone;
    private String gender;
    private String deptId;
    private String departmentName;
    private String position;
    private String accountStatus;
    private String remark;
    private List<String> tags;
}
