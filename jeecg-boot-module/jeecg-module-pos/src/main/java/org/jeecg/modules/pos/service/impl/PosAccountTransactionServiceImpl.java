package org.jeecg.modules.pos.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.pos.entity.PosAccountTransaction;
import org.jeecg.modules.pos.enums.PosTransactionTypeEnum;
import org.jeecg.modules.pos.mapper.PosAccountTransactionMapper;
import org.jeecg.modules.pos.mapstruct.PosAccountTransactionMapstruct;
import org.jeecg.modules.pos.service.IPosAccountTransactionService;
import org.jeecg.modules.pos.vo.PosAccountTodaySummaryVO;
import org.jeecg.modules.pos.vo.PosAccountTransactionVO;
import org.jeecg.modules.pos.request.PosAccountTransactionPageQuery;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 账户交易服务实现
 */
@Slf4j
@Service
public class PosAccountTransactionServiceImpl extends JeecgServiceImpl<PosAccountTransactionMapper, PosAccountTransaction>
    implements IPosAccountTransactionService {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    @Override
    public PageResult<PosAccountTransactionVO> pageTransactions(PosAccountTransactionPageQuery query,
                                                                PageRequest pageRequest,
                                                                Map<String, String[]> queryParam) {
        PosAccountTransactionPageQuery actual = Optional.ofNullable(query).orElseGet(PosAccountTransactionPageQuery::new);
        long pageNo = pageRequest == null || pageRequest.getPageNo() == null ? actual.getPageNo() : pageRequest.getPageNo();
        long pageSize = pageRequest == null || pageRequest.getPageSize() == null ? actual.getPageSize() : pageRequest.getPageSize();
        Map<String, String[]> params = queryParam == null ? Map.of() : queryParam;

        return pageByQuery(
            actual,
            pageNo,
            pageSize,
            params,
            q -> {
                PosAccountTransaction entity = new PosAccountTransaction();
                entity.setAccountId(q.getAccountId());
                entity.setBizType(q.getBizType());
                entity.setDirection(q.getDirection());
                return entity;
            },
            PosAccountTransactionMapstruct.INSTANCE::toVO,
            qw -> {
                if (actual.getOccurTimeStart() != null) {
                    qw.ge("occur_time", actual.getOccurTimeStart());
                }
                if (actual.getOccurTimeEnd() != null) {
                    qw.le("occur_time", actual.getOccurTimeEnd());
                }
                if (StringUtils.isNotBlank(actual.getKeyword())) {
                    String keyword = actual.getKeyword();
                    qw.and(w -> w.like("transaction_no", keyword)
                        .or().like("biz_no", keyword)
                        .or().like("channel", keyword)
                        .or().like("remark", keyword));
                }
                qw.orderByDesc("occur_time");
            }
        );
    }

    @Override
    public void record(PosAccountTransactionVO transaction) {
        if (transaction == null) {
            return;
        }
        PosAccountTransaction entity = new PosAccountTransaction();
        entity.setId(transaction.getId());
        entity.setAccountId(transaction.getAccountId());
        entity.setAccountNo(transaction.getAccountNo());
        entity.setTransactionNo(transaction.getTransactionNo());
        entity.setOccurTime(transaction.getOccurTime());
        entity.setBizType(transaction.getBizType());
        entity.setDirection(transaction.getDirection());
        entity.setWalletType(transaction.getWalletType());
        entity.setAmount(transaction.getAmount());
        entity.setBalanceAfter(transaction.getBalanceAfter());
        entity.setChannel(transaction.getChannel());
        entity.setBizNo(transaction.getBizNo());
        entity.setRemark(transaction.getRemark());

        if (entity.getOccurTime() == null) {
            entity.setOccurTime(new Date());
        }
        this.save(entity);
    }

    @Override
    public PosAccountTodaySummaryVO summarize(String accountId, Date targetDate) {
        if (StringUtils.isBlank(accountId)) {
            return PosAccountTodaySummaryVO.empty();
        }
        Date baseDate = targetDate == null ? new Date() : targetDate;
        LocalDate localDate = Instant.ofEpochMilli(baseDate.getTime()).atZone(DEFAULT_ZONE).toLocalDate();
        LocalDateTime start = localDate.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        Date startDate = Date.from(start.atZone(DEFAULT_ZONE).toInstant());
        Date endDate = Date.from(end.atZone(DEFAULT_ZONE).toInstant());

        LambdaQueryWrapper<PosAccountTransaction> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PosAccountTransaction::getAccountId, accountId)
            .ge(PosAccountTransaction::getOccurTime, startDate)
            .lt(PosAccountTransaction::getOccurTime, endDate);

        List<PosAccountTransaction> list = this.list(wrapper);
        PosAccountTodaySummaryVO summary = PosAccountTodaySummaryVO.empty();
        summary.setTotalCount(list.size());
        list.forEach(txn -> {
            String bizType = txn.getBizType();
            BigDecimal amount = safe(txn.getAmount());
            if (PosTransactionTypeEnum.RECHARGE.getCode().equalsIgnoreCase(bizType)) {
                summary.setRechargeAmount(summary.getRechargeAmount().add(amount));
                summary.setRechargeCount(summary.getRechargeCount() + 1);
            } else if (PosTransactionTypeEnum.REFUND.getCode().equalsIgnoreCase(bizType)) {
                summary.setRefundAmount(summary.getRefundAmount().add(amount));
                summary.setRefundCount(summary.getRefundCount() + 1);
            } else if (PosTransactionTypeEnum.CONSUMPTION.getCode().equalsIgnoreCase(bizType)) {
                summary.setExpenseAmount(summary.getExpenseAmount().add(amount));
                summary.setExpenseCount(summary.getExpenseCount() + 1);
            }
        });
        return summary;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
