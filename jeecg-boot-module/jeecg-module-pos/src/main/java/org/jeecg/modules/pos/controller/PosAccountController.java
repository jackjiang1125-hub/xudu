package org.jeecg.modules.pos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.modules.pos.service.IPosAccountService;
import org.jeecg.modules.pos.service.IPosAccountTransactionService;
import org.jeecg.modules.pos.vo.PosAccountDetailVO;
import org.jeecg.modules.pos.vo.PosAccountListVO;
import org.jeecg.modules.pos.vo.PosAccountTodaySummaryVO;
import org.jeecg.modules.pos.vo.PosAccountTransactionVO;
import org.jeecg.modules.pos.request.PosAccountQuery;
import org.jeecg.modules.pos.request.PosAccountProfileUpdateRequest;
import org.jeecg.modules.pos.request.PosAccountRechargeRequest;
import org.jeecg.modules.pos.request.PosAccountRefundRequest;
import org.jeecg.modules.pos.request.PosAccountSimpleOperateRequest;
import org.jeecg.modules.pos.request.PosAccountStatusChangeRequest;
import org.jeecg.modules.pos.request.PosAccountTransactionPageQuery;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;

/**
 * POS账户管理接口
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/pos/account")
@Tag(name = "POS-账户管理")
public class PosAccountController {

    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();

    @Autowired
    private IPosAccountService posAccountService;

    @Autowired
    private IPosAccountTransactionService transactionService;

    @AutoLog("POS账户-分页列表查询")
    @GetMapping("/list")
    @Operation(summary = "分页查询POS账户")
    public Result<PageResult<PosAccountListVO>> list(@Valid PosAccountQuery query,
                                                     PageRequest pageRequest,
                                                     HttpServletRequest req) {
        return Result.OK(posAccountService.pageList(query, pageRequest, req.getParameterMap()));
    }

    @AutoLog("POS账户-详情查询")
    @GetMapping("/detail")
    @Operation(summary = "查询账户详情")
    public Result<PosAccountDetailVO> detail(@RequestParam("accountId") @NotBlank String accountId) {
        return Result.OK(posAccountService.getDetail(accountId));
    }

    @AutoLog("POS账户-交易流水查询")
    @GetMapping("/transactions")
    @Operation(summary = "分页查询账户交易流水")
    public Result<PageResult<PosAccountTransactionVO>> transactions(@Valid PosAccountTransactionPageQuery query,
                                                                    PageRequest pageRequest,
                                                                    HttpServletRequest req) {
        if (Boolean.FALSE.equals(query.getShowAll())) {
            applyTodayRange(query);
        }
        return Result.OK(transactionService.pageTransactions(query, pageRequest, req.getParameterMap()));
    }

    @AutoLog("POS账户-今日汇总")
    @GetMapping("/todaySummary")
    @Operation(summary = "查询账户今日收支概览")
    public Result<PosAccountTodaySummaryVO> todaySummary(@RequestParam("accountId") @NotBlank String accountId,
                                                         @RequestParam(value = "date", required = false)
                                                         @DateTimeFormat(pattern = "yyyy-MM-dd")
                                                         Date targetDate) {
        Date summaryDate = targetDate == null ? new Date() : targetDate;
        return Result.OK(transactionService.summarize(accountId, summaryDate));
    }

    @AutoLog("POS账户-充值")
    @PostMapping("/recharge")
    @Operation(summary = "账户充值")
    public Result<?> recharge(@Valid @RequestBody PosAccountRechargeRequest request) {
        posAccountService.recharge(request);
        return Result.OK("充值成功");
    }

    @AutoLog("POS账户-退款/取款")
    @PostMapping("/refund")
    @Operation(summary = "账户退款/取款")
    public Result<?> refund(@Valid @RequestBody PosAccountRefundRequest request) {
        posAccountService.refund(request);
        return Result.OK("退款成功");
    }

    @AutoLog("POS账户-资料更新")
    @PutMapping("/profile")
    @Operation(summary = "更新账户资料")
    public Result<?> updateProfile(@Valid @RequestBody PosAccountProfileUpdateRequest request) {
        posAccountService.updateProfile(request);
        return Result.OK("资料更新成功");
    }

    @AutoLog("POS账户-状态更新")
    @PostMapping("/status")
    @Operation(summary = "修改账户状态")
    public Result<?> changeStatus(@Valid @RequestBody PosAccountStatusChangeRequest request) {
        posAccountService.changeStatus(request);
        return Result.OK("状态更新成功");
    }

    @AutoLog("POS账户-挂失")
    @PostMapping("/loss")
    @Operation(summary = "账户挂失")
    public Result<?> reportLoss(@Valid @RequestBody PosAccountSimpleOperateRequest request) {
        posAccountService.reportLoss(request);
        return Result.OK("挂失成功");
    }

    @AutoLog("POS账户-重置支付密码")
    @PostMapping("/payPassword/reset")
    @Operation(summary = "重置账户支付密码")
    public Result<?> resetPayPassword(@Valid @RequestBody PosAccountSimpleOperateRequest request) {
        posAccountService.resetPayPassword(request);
        return Result.OK("支付密码重置成功");
    }

    @AutoLog("POS账户-补卡")
    @PostMapping("/card/reissue")
    @Operation(summary = "账户补卡")
    public Result<?> reissueCard(@Valid @RequestBody PosAccountSimpleOperateRequest request) {
        posAccountService.reissueCard(request);
        return Result.OK("补卡申请成功");
    }

    private void applyTodayRange(PosAccountTransactionPageQuery query) {
        LocalDate today = LocalDate.now(DEFAULT_ZONE);
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        if (query.getOccurTimeStart() == null) {
            query.setOccurTimeStart(Date.from(start.atZone(DEFAULT_ZONE).toInstant()));
        }
        if (query.getOccurTimeEnd() == null) {
            query.setOccurTimeEnd(Date.from(end.atZone(DEFAULT_ZONE).toInstant()));
        }
    }
}
