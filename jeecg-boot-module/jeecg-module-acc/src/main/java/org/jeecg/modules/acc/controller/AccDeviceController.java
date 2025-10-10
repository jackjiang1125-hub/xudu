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
        
        return Result.OK();
    }

    /**
     * 授权请求参数
     */
    public record AuthorizeRequest(String sn, String registryCode, String remark) {
    }
}
