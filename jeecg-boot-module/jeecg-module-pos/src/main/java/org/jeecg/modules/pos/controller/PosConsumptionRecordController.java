package org.jeecg.modules.pos.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.pos.entity.PosConsumptionRecord;
import org.jeecg.modules.pos.service.IPosConsumptionRecordService;
import org.jeecg.modules.pos.vo.PosConsumptionRecordVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

/**
 * 消费记录管理控制器
 */
@Tag(name = "POS-消费记录管理")
@RestController
@RequestMapping("/pos/consumptionRecord")
@Slf4j
public class PosConsumptionRecordController extends JeecgController<PosConsumptionRecord, IPosConsumptionRecordService> {

    @Autowired
    private IPosConsumptionRecordService consumptionRecordService;

    /**
     * 分页查询消费记录
     */
    @AutoLog(value = "消费记录-分页列表查询")
    @GetMapping("/list")
    @Operation(summary = "分页查询消费记录")
    public Result<IPage<PosConsumptionRecordVO>> list(@RequestParam(name = "cardNo", required = false) String cardNo,
                                                   @RequestParam(name = "customerId", required = false) String customerId,
                                                   @RequestParam(name = "customerName", required = false) String customerName,
                                                   @RequestParam(name = "type", required = false) String type,
                                                   @RequestParam(name = "deviceCode", required = false) String deviceCode,
                                                   @RequestParam(name = "deviceName", required = false) String deviceName,
                                                   @RequestParam(name = "restaurantCode", required = false) String restaurantCode,
                                                   @RequestParam(name = "consumeTimeStart", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date consumeTimeStart,
                                                   @RequestParam(name = "consumeTimeEnd", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date consumeTimeEnd,
                                                   @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                   @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize) {
        try {
            IPage<PosConsumptionRecordVO> page = consumptionRecordService.pageList(cardNo, customerId, customerName, type,
                    deviceCode, deviceName, restaurantCode, consumeTimeStart, consumeTimeEnd, pageNo, pageSize);
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