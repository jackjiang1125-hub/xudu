package org.jeecg.modules.pos.service;

import org.jeecg.modules.pos.vo.PosAccountDetailVO;
import org.jeecg.modules.pos.vo.PosAccountListVO;
import org.jeecg.modules.pos.vo.PosAccountTodaySummaryVO;
import org.jeecg.modules.pos.request.PosAccountQuery;
import org.jeecg.modules.pos.request.PosAccountProfileUpdateRequest;
import org.jeecg.modules.pos.request.PosAccountRechargeRequest;
import org.jeecg.modules.pos.request.PosAccountRefundRequest;
import org.jeecg.modules.pos.request.PosAccountSimpleOperateRequest;
import org.jeecg.modules.pos.request.PosAccountStatusChangeRequest;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;

import java.util.Map;

/**
 * POS账户服务
 */
public interface IPosAccountService {

    /**
     * 分页查询账户
     */
    PageResult<PosAccountListVO> pageList(PosAccountQuery query, PageRequest pageRequest, Map<String, String[]> queryParam);

    /**
     * 查询账户详情
     */
    PosAccountDetailVO getDetail(String accountId);

    /**
     * 充值
     */
    void recharge(PosAccountRechargeRequest request);

    /**
     * 退款/取款
     */
    void refund(PosAccountRefundRequest request);

    /**
     * 更新资料
     */
    void updateProfile(PosAccountProfileUpdateRequest request);

    /**
     * 修改状态
     */
    void changeStatus(PosAccountStatusChangeRequest request);

    /**
     * 挂失
     */
    void reportLoss(PosAccountSimpleOperateRequest request);

    /**
     * 重置支付密码
     */
    void resetPayPassword(PosAccountSimpleOperateRequest request);

    /**
     * 补卡
     */
    void reissueCard(PosAccountSimpleOperateRequest request);

    /**
     * 今日汇总
     */
    PosAccountTodaySummaryVO summarizeToday(String accountId);
}
