package org.jeecg.modules.iot.device.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.modules.iot.device.entity.IotDeviceCommand;
import org.jeecg.modules.iot.device.service.IotDeviceCommandService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

/**
 * REST endpoints for inspecting device command queues.
 */
@Tag(name = "IOT-ACC:命令管理")
@RestController
@RequestMapping("/iot/acc/command")
@RequiredArgsConstructor
public class IotDeviceCommandController extends JeecgController<IotDeviceCommand, IotDeviceCommandService> {

    private final IotDeviceCommandService iotDeviceCommandService;

    @GetMapping("/list")
    @Operation(summary = "分页查询设备命令")
    public Result<IPage<IotDeviceCommand>> list(IotDeviceCommand command,
                                                @RequestParam(name = "pageNo", defaultValue = "1") Integer pageNo,
                                                @RequestParam(name = "pageSize", defaultValue = "10") Integer pageSize,
                                                HttpServletRequest req) {
        QueryWrapper<IotDeviceCommand> queryWrapper = QueryGenerator.initQueryWrapper(command, req.getParameterMap());
        queryWrapper.orderByDesc("create_time");
        Page<IotDeviceCommand> page = new Page<>(pageNo, pageSize);
        IPage<IotDeviceCommand> pageList = iotDeviceCommandService.page(page, queryWrapper);
        return Result.OK(pageList);
    }
}
