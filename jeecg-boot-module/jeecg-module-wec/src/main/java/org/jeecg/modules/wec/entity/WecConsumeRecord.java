package org.jeecg.modules.wec.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;
import org.jeecgframework.poi.excel.annotation.Excel;

/**
 * @Description: 消费记录
 * @Author: jeecg-boot
 * @Date:   2025-12-07
 * @Version: V1.0
 */
@Data
@TableName("wec_consume_record")
public class WecConsumeRecord implements Serializable {
    private static final long serialVersionUID = 1L;

	/**主键*/
	@TableId(type = IdType.ASSIGN_ID)
    private java.lang.String id;
	/**交易号*/
	@Excel(name = "交易号", width = 15)
    private java.lang.String tradeNo;
	/**卡号*/
	@Excel(name = "卡号", width = 15)
    private java.lang.String cardNo;
	/**用户ID*/
	@Excel(name = "用户ID", width = 15)
    private java.lang.String userId;
	/**用户姓名*/
	@Excel(name = "用户姓名", width = 15)
    private java.lang.String userName;
	/**设备ID*/
	@Excel(name = "设备ID", width = 15)
    private java.lang.String deviceId;
	/**设备名称*/
	@Excel(name = "设备名称", width = 15)
    private java.lang.String deviceName;
	/**金额*/
	@Excel(name = "金额", width = 15)
    private java.math.BigDecimal amount;
	/**余额*/
	@Excel(name = "余额", width = 15)
    private java.math.BigDecimal balance;
	/**类型(1:消费 2:充值 3:退款)*/
	@Excel(name = "类型(1:消费 2:充值 3:退款)", width = 15)
    private java.lang.String type;
	/**状态(1:成功 0:失败)*/
	@Excel(name = "状态(1:成功 0:失败)", width = 15)
    private java.lang.String status;
	/**交易时间*/
	@Excel(name = "交易时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private java.util.Date consumeTime;
	/**设备记录序号*/
	@Excel(name = "设备记录序号", width = 15)
    private java.lang.Integer recordNo;
	/**创建人*/
    private java.lang.String createBy;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private java.util.Date createTime;
	/**更新人*/
    private java.lang.String updateBy;
	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private java.util.Date updateTime;
	/**所属部门*/
    private java.lang.String sysOrgCode;
}
