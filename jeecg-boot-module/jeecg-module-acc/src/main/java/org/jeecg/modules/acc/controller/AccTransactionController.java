package org.jeecg.modules.acc.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.acc.entity.AccTransaction;
import org.jeecg.modules.acc.service.IAccTransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/acc/transaction")
@Slf4j
public class AccTransactionController {

    @Autowired
    private IAccTransactionService accTransactionService;

    @AutoLog("门禁交易记录-分页列表查询")
    @Operation(summary = "门禁交易记录-分页列表查询")
    @GetMapping("/list")
    public Result<IPage<AccTransaction>> list(AccTransaction accTransaction,
                                          @RequestParam(defaultValue = "1") Integer pageNo,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          HttpServletRequest req) {
        QueryWrapper<AccTransaction> qw = QueryGenerator.initQueryWrapper(accTransaction, req.getParameterMap());
        Page<AccTransaction> page = new Page<>(pageNo, pageSize);
        return Result.OK(accTransactionService.page(page, qw));
    }

    @AutoLog("门禁交易记录-添加")
    @Operation(summary = "门禁交易记录-添加")
    @PostMapping("/add")
    public Result<String> add(@RequestBody AccTransaction entity) {
        accTransactionService.save(entity);
        return Result.OK("添加成功");
    }

    @AutoLog("门禁交易记录-编辑")
    @Operation(summary = "门禁交易记录-编辑")
    @PutMapping("/edit")
    public Result<String> edit(@RequestBody AccTransaction entity) {
        return accTransactionService.updateById(entity) ? Result.OK("编辑成功") : Result.error("记录不存在");
    }

    @AutoLog("门禁交易记录-通过id删除")
    @Operation(summary = "门禁交易记录-通过id删除")
    @DeleteMapping("/delete")
    public Result<?> delete(@RequestParam String id) {
        accTransactionService.removeById(id);
        return Result.OK("删除成功");
    }

    @AutoLog("门禁交易记录-批量删除")
    @Operation(summary = "门禁交易记录-批量删除")
    @DeleteMapping("/deleteBatch")
    public Result<?> deleteBatch(@RequestParam String ids) {
        accTransactionService.removeByIds(java.util.Arrays.asList(ids.split(",")));
        return Result.OK("批量删除成功");
    }
}