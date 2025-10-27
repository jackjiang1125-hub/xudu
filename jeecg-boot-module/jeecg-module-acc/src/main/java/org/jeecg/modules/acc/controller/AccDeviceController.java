package org.jeecg.modules.acc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecgframework.boot.acc.api.AccDeviceService;
import org.jeecgframework.boot.acc.query.AccDeviceQuery;
import org.jeecgframework.boot.acc.vo.AccDeviceVO;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.jeecgframework.boot.iot.api.IotDeviceService;
import org.jeecgframework.boot.iot.query.IotDeviceQuery;
import org.jeecgframework.boot.iot.vo.IotDeviceVO;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

/**
 * 门禁设备管理控制器
 * @author system
 * @date 2025-01-03
 */
@Tag(name = "ACC-门禁设备管理")
@RestController
@RequestMapping("/acc/device")

@Slf4j
public class AccDeviceController {


    @Autowired
    private AccDeviceService accDeviceService;

    @Autowired
    private IotDeviceService iotDeviceService;

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    /**
     * 分页查询门禁设备
     */
    @GetMapping("/list")
    @Operation(summary = "分页查询门禁设备")
    public Result<PageResult<AccDeviceVO>> list(AccDeviceQuery query,
                                                PageRequest pageRequest,
                                                HttpServletRequest req) {
        PageResult<AccDeviceVO> pageResult = accDeviceService.list(query,pageRequest,req.getParameterMap());
        return Result.OK(pageResult);
    }

    @GetMapping("/listAuthDevice")
    @Operation(summary = "分页查询待授权设备")
    public Result<PageResult<IotDeviceVO>> listAuthDevice(IotDeviceQuery query,
                                                          PageRequest pageRequest,
                                                          HttpServletRequest req) {
        query.setDeviceType("acc");//查询门禁设备
        PageResult<IotDeviceVO> pageResult = iotDeviceService.list(query,pageRequest,req.getParameterMap());
        return Result.OK(pageResult);
    }

    /**
     * 根据ID查询设备详情
     */
    @GetMapping("/detail")
    @Operation(summary = "根据ID查询设备详情")
    public Result<AccDeviceVO> getById(@RequestParam String id) {
        AccDeviceVO deviceVO = accDeviceService.getById(id);
        if (deviceVO == null) {
            return Result.error("设备不存在");
        }
        return Result.OK(deviceVO);
    }

    /**
     * 根据SN查询设备详情
     */
    @GetMapping("/getBySn")
    @Operation(summary = "根据SN查询设备详情")
    public Result<AccDeviceVO> getBySn(@RequestParam String sn) {
        AccDeviceVO deviceVO = accDeviceService.getBySn(sn);
        if (deviceVO == null) {
            return Result.error("设备不存在");
        }
        return Result.OK(deviceVO);
    }

    /**
     * 新增设备
     */
    @PostMapping("/add")
    @Operation(summary = "新增门禁设备")
    public Result<AccDeviceVO> add(@RequestBody AccDeviceVO deviceVO) {
        try {
            //不直接写入到acc_device，先写入到acc_device_temp。
            accDeviceService.saveTemp(deviceVO);

            return Result.OK();
        } catch (Exception e) {
            log.error("新增设备失败", e);
            return Result.error("新增设备失败: " + e.getMessage());
        }
    }

    /**
     * 更新设备
     */
    @PutMapping("/edit")
    @Operation(summary = "更新门禁设备")
    public Result<AccDeviceVO> edit(@RequestBody AccDeviceVO deviceVO) {
        try {
            AccDeviceVO result = accDeviceService.update(deviceVO);
            return Result.OK(result);
        } catch (Exception e) {
            log.error("更新设备失败", e);
            return Result.error("更新设备失败: " + e.getMessage());
        }
    }

    /**
     * 删除设备
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除门禁设备")
    public Result<String> delete(@RequestParam String id) {
        try {
            boolean success = accDeviceService.deleteByIdCustom(id);
            if (success) {
                
                return Result.OK("删除成功");
            } else {
                return Result.error("删除失败");
            }
        } catch (Exception e) {
            log.error("删除设备失败", e);
            return Result.error("删除设备失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除设备
     */
    @DeleteMapping("/deleteBatch")
    @Operation(summary = "批量删除门禁设备")
    public Result<String> deleteBatch(@RequestParam String ids) {
        try {
            String[] idArray = ids.split(",");
            for (String id : idArray) {
                accDeviceService.deleteByIdCustom(id);
            }
            return Result.OK("批量删除成功");
           
        } catch (Exception e) {
            log.error("批量删除设备失败", e);
            return Result.error("批量删除设备失败: " + e.getMessage());
        }
    }

    /**
     * 授权请求参数
     */
    public record AuthorizeRequest(String sn, String registryCode, String remark) {
    }

    /**
     * 通过设备SN添加门禁设备（使用最小字段或IoT信息）
     */
    @PostMapping("/addBySn")
    @Operation(summary = "按SN添加门禁设备")
    public Result<AccDeviceVO> addBySn(@RequestBody AddBySnRequest request) {
        if (request == null || StringUtils.isBlank(request.sn)) {
            return Result.error("设备SN不能为空");
        }

        // 已存在则直接返回
        AccDeviceVO exist = accDeviceService.getBySn(request.sn);
        if (exist != null) {
            return Result.OK(exist);
        }

        // 尝试从IoT分页接口按SN检索（若前端直接传递名称/IP，也同样支持）
        // 此处直接发布注册事件，交由服务监听并落库
        org.jeecg.modules.events.acc.RegisterAccDeviceEvent event = new org.jeecg.modules.events.acc.RegisterAccDeviceEvent();
        event.setSn(request.sn);
        event.setDeviceName(request.deviceName);
        event.setIpAddress(request.ipAddress);
        event.setAuthorized(Boolean.FALSE);
        applicationEventPublisher.publishEvent(event);

        // 事件监听保存为同步发布，发布后查询返回
        AccDeviceVO saved = accDeviceService.getBySn(request.sn);
        return Result.OK(saved);
    }

    /**
     * 按SN添加设备请求
     */
    public record AddBySnRequest(String sn, String deviceName, String ipAddress) {
    }

    /**
     * 批量同步设备时间
     */
    @PostMapping("/syncTime")
    @Operation(summary = "批量同步设备时间")
    public Result<Object> syncTime(@RequestBody SyncTimeRequest request) {
        if (request == null || request.sns() == null || request.sns().isEmpty()) {
            return Result.error("参数sns不能为空");
        }
        List<String> failed = new ArrayList<>();
        for (String sn : request.sns()) {
            try {
                Long ts = request.timestamp();
                iotDeviceService.syncTimezone(sn, "+0800");
                iotDeviceService.syncTime(sn, ts != null ? ts : System.currentTimeMillis() / 1000);
            } catch (Exception e) {
                log.warn("同步时间失败 sn={} err={}", sn, e.getMessage());
                failed.add(sn);
            }
        }
        int total = request.sns().size();
        int success = total - failed.size();
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("total", total);
        data.put("success", success);
        data.put("failed", failed);
        return Result.OK(data);
    }

    /**
     * 同步时间请求
     */
    public record SyncTimeRequest(List<String> sns, Long timestamp) {
    }
}
