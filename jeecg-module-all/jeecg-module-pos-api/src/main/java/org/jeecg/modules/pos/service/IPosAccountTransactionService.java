package org.jeecg.modules.pos.service;

import org.jeecg.modules.pos.vo.PosAccountTodaySummaryVO;
import org.jeecg.modules.pos.vo.PosAccountTransactionVO;
import org.jeecg.modules.pos.request.PosAccountTransactionPageQuery;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;

import java.util.Date;
import java.util.Map;

/**
 * 账户交易服务
 */
public interface IPosAccountTransactionService {

    /**
     * 分页查询流水
     */
    PageResult<PosAccountTransactionVO> pageTransactions(PosAccountTransactionPageQuery query, PageRequest pageRequest, Map<String, String[]> queryParam);

    /**
     * 记录交易
     */
    void record(PosAccountTransactionVO transaction);

    /**
     * 汇总指定日期的收支
     */
    PosAccountTodaySummaryVO summarize(String accountId, Date targetDate);
}
