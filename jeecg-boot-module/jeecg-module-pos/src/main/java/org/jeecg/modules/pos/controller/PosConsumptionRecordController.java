package org.jeecg.modules.pos.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.api.vo.Result;
import org.jeecg.modules.pos.service.IPosConsumptionRecordService;
import org.jeecg.modules.pos.vo.PosConsumptionRecordVO;
import org.jeecg.modules.pos.request.PosConsumptionRecordQuery;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * 消费记录管理控制器
 */
@Tag(name = "POS-消费记录管理")
@RestController
@RequestMapping("/pos/consumptionRecord")
@Slf4j
public class PosConsumptionRecordController {

    @Autowired
    private IPosConsumptionRecordService consumptionRecordService;

    /**
     * 分页查询消费记录
     */
    @AutoLog(value = "消费记录-分页列表查询")
    @GetMapping("/list")
    @Operation(summary = "分页查询消费记录")
    public Result<PageResult<PosConsumptionRecordVO>> list(PosConsumptionRecordQuery query,
                                                           PageRequest pageRequest,
                                                           HttpServletRequest req) {
        try {
            PageResult<PosConsumptionRecordVO> page = consumptionRecordService.list(query, pageRequest, req.getParameterMap());
            return Result.OK(page);
        } catch (Exception e) {
            log.error("查询消费记录列表失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID查询详情
     */
    @AutoLog(value = "消费记录-查询详情")
    @GetMapping("/detail")
    @Operation(summary = "查询消费记录详情")
    public Result<PosConsumptionRecordVO> detail(@RequestParam String id) {
        try {
            PosConsumptionRecordVO vo = consumptionRecordService.getDetailById(id);
            if (vo == null) {
                return Result.error("消费记录不存在");
            }
            return Result.OK(vo);
        } catch (Exception e) {
            log.error("查询消费记录详情失败", e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除消费记录
     */
    @AutoLog(value = "消费记录-批量删除")
    @DeleteMapping("/deleteBatch")
    @Operation(summary = "批量删除消费记录")
    public Result<?> deleteBatch(@RequestParam String ids) {
        try {
            if (ids == null || ids.isEmpty()) {
                return Result.error("参数错误");
            }
            String[] idArray = ids.split(",");
            boolean success = consumptionRecordService.deleteBatchByIds(idArray);
            if (success) {
                return Result.OK("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除消费记录失败", e);
            return Result.error("删除失败: " + e.getMessage());
        }
    }
}
