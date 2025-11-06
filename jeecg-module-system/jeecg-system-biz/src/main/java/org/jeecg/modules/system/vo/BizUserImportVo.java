package org.jeecg.modules.system.vo;

import lombok.Data;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * 业务用户导入模板 VO
 * 仅用于 Excel 模板下载与导入映射
 */
@Data
public class BizUserImportVo {

    /** 工号 */
    @Excel(name = "工号", width = 20)
    private String workNo;

    /** 真实姓名 */
    @Excel(name = "真实姓名", width = 20)
    private String realname;

    /** 电话 */
    @Excel(name = "电话", width = 20)
    private String phone;

    /** 电子邮件 */
    @Excel(name = "电子邮件", width = 25)
    private String email;

    /** 性别（1：男 2：女） */
    @Excel(name = "性别", width = 10, dicCode = "sex")
    private Integer sex;

    /** 卡号（门禁卡号） */
    @Excel(name = "卡号", width = 25)
    private String cardNumber;

    /** 管理员密码（设备管理员口令） */
    @Excel(name = "设备验证密码", width = 20)
    private String verifyPassword;

}