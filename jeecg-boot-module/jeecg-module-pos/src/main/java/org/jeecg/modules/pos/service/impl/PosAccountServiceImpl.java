package org.jeecg.modules.pos.service.impl;

import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.pos.entity.PosAccount;
import org.jeecg.modules.pos.entity.PosAccountTransaction;
import org.jeecg.modules.pos.enums.PosAccountStatusEnum;
import org.jeecg.modules.pos.enums.PosTransactionDirectionEnum;
import org.jeecg.modules.pos.enums.PosTransactionTypeEnum;
import org.jeecg.modules.pos.enums.PosWalletType;
import org.jeecg.modules.pos.mapper.PosAccountMapper;
import org.jeecg.modules.pos.mapstruct.PosAccountMapstruct;
import org.jeecg.modules.pos.mapstruct.PosAccountQueryMapstruct;
import org.jeecg.modules.pos.mapstruct.PosAccountTransactionMapstruct;
import org.jeecg.modules.pos.service.IPosAccountService;
import org.jeecg.modules.pos.service.IPosAccountTransactionService;
import org.jeecg.modules.pos.vo.PosAccountDetailVO;
import org.jeecg.modules.pos.vo.PosAccountLimitVO;
import org.jeecg.modules.pos.vo.PosAccountListVO;
import org.jeecg.modules.pos.vo.PosAccountTodaySummaryVO;
import org.jeecg.modules.pos.vo.PosAccountTransactionVO;
import org.jeecg.modules.pos.vo.PosAccountWalletVO;
import org.jeecg.modules.pos.request.PosAccountProfileUpdateRequest;
import org.jeecg.modules.pos.request.PosAccountQuery;
import org.jeecg.modules.pos.request.PosAccountRechargeRequest;
import org.jeecg.modules.pos.request.PosAccountRefundRequest;
import org.jeecg.modules.pos.request.PosAccountSimpleOperateRequest;
import org.jeecg.modules.pos.request.PosAccountStatusChangeRequest;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * POS账户服务实现
 */
@Slf4j
@Service
public class PosAccountServiceImpl extends JeecgServiceImpl<PosAccountMapper, PosAccount> implements IPosAccountService {

    @Autowired
    private IPosAccountTransactionService transactionService;



    @Override
    public PageResult<PosAccountListVO> pageList(PosAccountQuery query, PageRequest pageRequest, Map<String, String[]> queryParam) {
        PosAccountQuery actualQuery = Optional.ofNullable(query).orElseGet(PosAccountQuery::new);
        long pageNo = pageRequest == null || pageRequest.getPageNo() == null ? 1L : pageRequest.getPageNo();
        long pageSize = pageRequest == null || pageRequest.getPageSize() == null ? 10L : pageRequest.getPageSize();
        Map<String, String[]> params = normalizeQueryParams(queryParam);
        return pageByQuery(
            actualQuery,
            pageNo,
            pageSize,
            params,
            PosAccountQueryMapstruct.INSTANCE::toEntity,
            PosAccountMapstruct.INSTANCE::toListVO,
            qw -> qw.orderByDesc("create_time")
        );
    }

    @Override
    public PosAccountDetailVO getDetail(String accountId) {
        PosAccount account = getRequiredAccount(accountId);
        //fillDeptInfo(account);
        PosAccountDetailVO detailVO = PosAccountMapstruct.INSTANCE.toDetailVO(account);
        detailVO.setWallet(buildWallet(account));
        detailVO.setLimits(buildLimit(account));
        detailVO.setBindDevices(account.getBindDevices());
        detailVO.setAssociatedCards(account.getAssociatedCards());
        detailVO.setLossReported(Optional.ofNullable(account.getLossReported()).orElse(Boolean.FALSE));
        detailVO.setLastLossReportTime(account.getLastLossReportTime());
        detailVO.setLastPasswordResetTime(account.getLastPasswordResetTime());
        detailVO.setLastCardReissueTime(account.getLastCardReissueTime());
        return detailVO;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void recharge(PosAccountRechargeRequest request) {
        PosAccount account = getRequiredAccount(request.getAccountId());
        PosWalletType walletType = Optional.ofNullable(PosWalletType.fromCode(request.getWalletType()))
            .orElseThrow(() -> new JeecgBootException("不支持的钱包类型"));
        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new JeecgBootException("充值金额必须大于0");
        }
        adjustWallet(account, walletType, amount, true);
        recalcBalance(account);
        account.setLastActiveTime(new Date());
        this.updateById(account);

        PosAccountTransaction transaction = buildTransaction(account, walletType, amount,
            PosTransactionTypeEnum.RECHARGE.getCode(), PosTransactionDirectionEnum.INCOME.getCode());
        transaction.setChannel(request.getChannel());
        transaction.setBizNo(request.getReferenceNo());
        transaction.setRemark(request.getRemark());
        PosAccountTransactionVO txnVO = PosAccountTransactionMapstruct.INSTANCE.toVO(transaction);
        transactionService.record(txnVO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void refund(PosAccountRefundRequest request) {
        PosAccount account = getRequiredAccount(request.getAccountId());
        PosWalletType walletType = Optional.ofNullable(PosWalletType.fromCode(request.getWalletType()))
            .orElseThrow(() -> new JeecgBootException("不支持的钱包类型"));
        BigDecimal amount = request.getAmount();
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new JeecgBootException("退款金额必须大于0");
        }
        adjustWallet(account, walletType, amount, false);
        recalcBalance(account);
        account.setLastActiveTime(new Date());
        this.updateById(account);

        PosAccountTransaction transaction = buildTransaction(account, walletType, amount,
            PosTransactionTypeEnum.REFUND.getCode(), PosTransactionDirectionEnum.EXPENSE.getCode());
        transaction.setChannel(request.getChannel());
        transaction.setBizNo(request.getOriginalBizNo());
        transaction.setRemark(request.getRemark());
        PosAccountTransactionVO txnVO = PosAccountTransactionMapstruct.INSTANCE.toVO(transaction);
        transactionService.record(txnVO);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateProfile(PosAccountProfileUpdateRequest request) {
        PosAccount account = getRequiredAccount(request.getAccountId());
        account.setRealName(request.getRealName());
        account.setPhone(request.getPhone());
        account.setGender(request.getGender());
        account.setDeptId(request.getDeptId());
        account.setDeptName(request.getDepartmentName());
        account.setPosition(request.getPosition());
        account.setAccountStatus(StringUtils.defaultIfBlank(request.getAccountStatus(), account.getAccountStatus()));
        account.setRemark(request.getRemark());
        account.setTags(request.getTags());
        this.updateById(account);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void changeStatus(PosAccountStatusChangeRequest request) {
        PosAccount account = getRequiredAccount(request.getAccountId());
        PosAccountStatusEnum statusEnum = Optional.ofNullable(PosAccountStatusEnum.fromCode(request.getTargetStatus()))
            .orElseThrow(() -> new JeecgBootException("非法的账户状态"));
        account.setAccountStatus(statusEnum.getCode());
        this.updateById(account);
        log.info("账户[{}]状态更新为{}", account.getAccountNo(), statusEnum.getCode());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void reportLoss(PosAccountSimpleOperateRequest request) {
        PosAccount account = getRequiredAccount(request.getAccountId());
        account.setLossReported(Boolean.TRUE);
        account.setLastLossReportTime(new Date());
        account.setAccountStatus(PosAccountStatusEnum.SUSPENDED.getCode());
        account.setRemark(StringUtils.defaultIfBlank(request.getRemark(), account.getRemark()));
        this.updateById(account);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void resetPayPassword(PosAccountSimpleOperateRequest request) {
        PosAccount account = getRequiredAccount(request.getAccountId());
        account.setLastPasswordResetTime(new Date());
        account.setRemark(StringUtils.defaultIfBlank(request.getRemark(), account.getRemark()));
        this.updateById(account);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void reissueCard(PosAccountSimpleOperateRequest request) {
        PosAccount account = getRequiredAccount(request.getAccountId());
        account.setLastCardReissueTime(new Date());
        account.setRemark(StringUtils.defaultIfBlank(request.getRemark(), account.getRemark()));
        this.updateById(account);
    }

    @Override
    public PosAccountTodaySummaryVO summarizeToday(String accountId) {
        return transactionService.summarize(accountId, new Date());
    }

    private PosAccount getRequiredAccount(String accountId) {
        if (StringUtils.isBlank(accountId)) {
            throw new JeecgBootException("账户ID不能为空");
        }
        PosAccount account = this.getById(accountId);
        if (account == null) {
            throw new JeecgBootException("账户不存在");
        }
        return account;
    }

    private void adjustWallet(PosAccount account, PosWalletType walletType, BigDecimal amount, boolean increase) {
        BigDecimal delta = increase ? amount : amount.negate();
        switch (walletType) {
            case CASH:
                BigDecimal cash = safe(account.getCashWallet()).add(delta);
                if (cash.compareTo(BigDecimal.ZERO) < 0) {
                    throw new JeecgBootException("现金钱包余额不足");
                }
                account.setCashWallet(cash);
                break;
            case SUBSIDY:
                BigDecimal subsidy = safe(account.getSubsidyWallet()).add(delta);
                if (subsidy.compareTo(BigDecimal.ZERO) < 0) {
                    throw new JeecgBootException("补贴钱包余额不足");
                }
                account.setSubsidyWallet(subsidy);
                break;
            case GIFT:
                BigDecimal gift = safe(account.getGiftWallet()).add(delta);
                if (gift.compareTo(BigDecimal.ZERO) < 0) {
                    throw new JeecgBootException("礼品钱包余额不足");
                }
                account.setGiftWallet(gift);
                break;
            default:
                throw new JeecgBootException("未实现的钱包类型");
        }
    }

    private void recalcBalance(PosAccount account) {
        BigDecimal total = safe(account.getCashWallet())
            .add(safe(account.getSubsidyWallet()))
            .add(safe(account.getGiftWallet()))
            .subtract(safe(account.getArrearsAmount()));
        account.setTotalBalance(total);
    }

    private PosAccountWalletVO buildWallet(PosAccount account) {
        PosAccountWalletVO walletVO = new PosAccountWalletVO();
        walletVO.setCashWallet(safe(account.getCashWallet()));
        walletVO.setSubsidyWallet(safe(account.getSubsidyWallet()));
        walletVO.setGiftWallet(safe(account.getGiftWallet()));
        walletVO.setFrozenAmount(safe(account.getFrozenAmount()));
        walletVO.setCreditLimit(safe(account.getCreditLimit()));
        walletVO.setArrearsAmount(safe(account.getArrearsAmount()));
        walletVO.setTotalBalance(safe(account.getTotalBalance()));
        return walletVO;
    }

    private PosAccountLimitVO buildLimit(PosAccount account) {
        PosAccountLimitVO limitVO = new PosAccountLimitVO();
        limitVO.setDailyConsumptionLimit(safe(account.getDailyConsumptionLimit()));
        limitVO.setSingleConsumptionLimit(safe(account.getSingleConsumptionLimit()));
        limitVO.setDailyRechargeLimit(safe(account.getDailyRechargeLimit()));
        limitVO.setSingleRechargeLimit(safe(account.getSingleRechargeLimit()));
        return limitVO;
    }

    private PosAccountTransaction buildTransaction(PosAccount account, PosWalletType walletType,
                                                   BigDecimal amount, String bizType, String direction) {
        PosAccountTransaction transaction = new PosAccountTransaction();
        transaction.setId(IdWorker.getIdStr());
        transaction.setAccountId(account.getId());
        transaction.setAccountNo(account.getAccountNo());
        transaction.setTransactionNo(IdWorker.getIdStr());
        transaction.setOccurTime(new Date());
        transaction.setWalletType(walletType.getCode());
        transaction.setBizType(bizType);
        transaction.setDirection(direction);
        transaction.setAmount(amount);
        transaction.setBalanceAfter(safe(account.getTotalBalance()));
        return transaction;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private Map<String, String[]> normalizeQueryParams(Map<String, String[]> original) {
        if (original == null || original.isEmpty()) {
            return original;
        }
        Map<String, String[]> copy = new HashMap<>(original);
        renameParam(copy, "registerTimeStart", "registerTime_begin");
        renameParam(copy, "registerTimeEnd", "registerTime_end");
        renameParam(copy, "deptIds", "deptId_in");
        return copy;
    }

    private void renameParam(Map<String, String[]> container, String fromKey, String toKey) {
        if (container == null || fromKey == null || toKey == null || fromKey.equals(toKey)) {
            return;
        }
        String[] value = container.remove(fromKey);
        if (value != null) {
            container.put(toKey, value);
        }
    }
}
