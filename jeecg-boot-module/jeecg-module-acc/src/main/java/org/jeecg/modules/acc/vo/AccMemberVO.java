package org.jeecg.modules.acc.vo;

import lombok.Data;

/**
 * 权限组成员VO
 */
@Data
public class AccMemberVO {
    private String id;
    private String memberName;
    private String memberCode;
    private String department;
    private String position;
    private String status;
    private String remark;
    // ===== 前端展示补充字段（与 accgroup.data.ts 对齐）=====
    private String name;   // 显示姓名（与 memberName 同步）
    private String dept;   // 显示部门（与 department 同步）
    private String phone;  // 联系方式
}