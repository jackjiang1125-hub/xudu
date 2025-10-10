package org.jeecg.modules.iot.device.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.iot.device.entity.IotDeviceRtLog;
import org.jeecg.modules.iot.device.service.IotDeviceRtLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import org.jeecg.common.system.base.controller.JeecgController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
/**
 * 门禁设备实时记录控制器
 */
@RestController
@RequestMapping("/iot/accDeviceRtLog")
@Tag(name = "门禁设备实时记录", description = "门禁设备实时记录管理")
@Slf4j
public class IotDeviceRtLogController extends JeecgController<IotDeviceRtLog, IotDeviceRtLogService> {

    @Autowired
    private IotDeviceRtLogService iotDeviceRtLogService;

    /**
     * 分页列表查询
     */
    @AutoLog(value = "门禁设备实时记录-分页列表查询")
    @Operation(summary = "门禁设备实时记录-分页列表查询", description = "门禁设备实时记录-分页列表查询")
    @GetMapping(value = "/list")
    //@RequiresPermissions("iot:accDeviceRtLog:list")
    public Result<IPage<IotDeviceRtLog>> queryPageList(IotDeviceRtLog iotDeviceRtLog,
                                                       @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                       @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                       HttpServletRequest req) {
        QueryWrapper<IotDeviceRtLog> queryWrapper = QueryGenerator.initQueryWrapper(iotDeviceRtLog, req.getParameterMap());
        // 按记录时间倒序排列，最新的记录在前面
        queryWrapper.orderByDesc("log_time");
        Page<IotDeviceRtLog> page = new Page<>(pageNo, pageSize);
        IPage<IotDeviceRtLog> pageList = iotDeviceRtLogService.page(page, queryWrapper);
        return Result.OK(pageList);
    }

    /**
     * 通过id查询
     */
    @AutoLog(value = "门禁设备实时记录-通过id查询")
    @Operation(summary = "门禁设备实时记录-通过id查询", description = "门禁设备实时记录-通过id查询")
    @GetMapping(value = "/queryById")
    //@RequiresPermissions("iot:accDeviceRtLog:list")
    public Result<IotDeviceRtLog> queryById(@RequestParam(name = "id", required = true) String id) {
        IotDeviceRtLog iotDeviceRtLog = iotDeviceRtLogService.getById(id);
        if (iotDeviceRtLog == null) {
            return Result.error("未找到对应数据");
        }
        return Result.OK(iotDeviceRtLog);
    }

    /**
     * 导出excel
     */
    @AutoLog(value = "门禁设备实时记录-导出")
    @Operation(summary = "门禁设备实时记录-导出", description = "门禁设备实时记录-导出")
    @RequestMapping(value = "/exportXls")
    @RequiresPermissions("iot:accDeviceRtLog:exportXls")
    public void exportXls(IotDeviceRtLog iotDeviceRtLog, HttpServletRequest req, HttpServletResponse response) {
        super.exportXls(req, iotDeviceRtLog, IotDeviceRtLog.class, "门禁设备实时记录");
    }
}
