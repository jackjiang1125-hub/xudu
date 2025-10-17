package org.jeecg.modules.acc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.vo.LoginUser;
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
            // 优先从 IoT 设备表按 SN 获取完整详情，并与传入数据进行合并（传入值优先，空缺由IoT补全）
            if (deviceVO != null && StringUtils.isNotBlank(deviceVO.getSn())) {
                IotDeviceVO iot = iotDeviceService.getBySn(deviceVO.getSn());
                if (iot != null) {
                    if (StringUtils.isBlank(deviceVO.getDeviceName())) deviceVO.setDeviceName(iot.getDeviceName());
                    if (StringUtils.isBlank(deviceVO.getDeviceType())) deviceVO.setDeviceType(iot.getDeviceType());
                    if (StringUtils.isBlank(deviceVO.getIpAddress())) deviceVO.setIpAddress(iot.getIpAddress());
                    if (StringUtils.isBlank(deviceVO.getFirmwareVersion())) deviceVO.setFirmwareVersion(iot.getFirmwareVersion());
                    if (StringUtils.isBlank(deviceVO.getPushVersion())) deviceVO.setPushVersion(iot.getPushVersion());
                    if (deviceVO.getMachineType() == null) deviceVO.setMachineType(iot.getMachineType());
                    if (StringUtils.isBlank(deviceVO.getGatewayIp())) deviceVO.setGatewayIp(iot.getGatewayIp());
                    if (StringUtils.isBlank(deviceVO.getNetMask())) deviceVO.setNetMask(iot.getNetMask());
                    if (deviceVO.getLastRegistryTime() == null) deviceVO.setLastRegistryTime(iot.getLastRegistryTime());
                    if (deviceVO.getAuthorized() == null) deviceVO.setAuthorized(iot.getAuthorized());
                }
            }
            AccDeviceVO result = accDeviceService.save(deviceVO);
            return Result.OK(result);
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
            boolean success = accDeviceService.deleteById(id);
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
            boolean success = accDeviceService.deleteBatch(idArray);
            if (success) {
                return Result.OK("批量删除成功");
            } else {
                return Result.error("批量删除失败");
            }
        } catch (Exception e) {
            log.error("批量删除设备失败", e);
            return Result.error("批量删除设备失败: " + e.getMessage());
        }
    }

    /**
     * 授权设备
     */
    @PostMapping("/authorize")
    @Operation(summary = "授权门禁设备")
    public Result<AccDeviceVO> authorize(@RequestBody AuthorizeRequest request) {
        if (request == null || StringUtils.isBlank(request.sn)) {
            return Result.error("设备SN不能为空");
        }
        // 查找设备，如果不存在则直接返回错误提示
        AccDeviceVO exist = accDeviceService.getBySn(request.sn);
        if (exist == null) {
            return Result.error("设备不存在，请先添加");
        }
        // 更新授权信息
        exist.setAuthorized(1);
        exist.setRegistryCode(request.registryCode);
        exist.setRemark(request.remark);
        exist.setLastRegistryTime(java.time.LocalDateTime.now());

        // 从 IoT 设备表补充细节信息（尽量不覆盖已有非空字段）
        IotDeviceVO iot = iotDeviceService.getBySn(request.sn);
        if (iot != null) {
            if (StringUtils.isBlank(exist.getDeviceName())) exist.setDeviceName(iot.getDeviceName());
            if (StringUtils.isBlank(exist.getDeviceType())) exist.setDeviceType(iot.getDeviceType());
            if (StringUtils.isBlank(exist.getIpAddress())) exist.setIpAddress(iot.getIpAddress());
            if (StringUtils.isBlank(exist.getFirmwareVersion())) exist.setFirmwareVersion(iot.getFirmwareVersion());
            if (StringUtils.isBlank(exist.getPushVersion())) exist.setPushVersion(iot.getPushVersion());
            if (exist.getMachineType() == null) exist.setMachineType(iot.getMachineType());
            if (StringUtils.isBlank(exist.getGatewayIp())) exist.setGatewayIp(iot.getGatewayIp());
            if (StringUtils.isBlank(exist.getNetMask())) exist.setNetMask(iot.getNetMask());
            if (exist.getLastRegistryTime() == null && iot.getLastRegistryTime() != null) exist.setLastRegistryTime(iot.getLastRegistryTime());
            if (exist.getAuthorized() == null && iot.getAuthorized() != null) exist.setAuthorized(iot.getAuthorized());
        }

        AccDeviceVO updated = accDeviceService.update(exist);
        return Result.OK(updated);
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
}
