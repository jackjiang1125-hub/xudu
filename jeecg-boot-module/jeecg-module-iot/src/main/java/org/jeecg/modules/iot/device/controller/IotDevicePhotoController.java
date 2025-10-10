package org.jeecg.modules.iot.device.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.controller.JeecgController;
import org.jeecg.modules.iot.device.entity.IotDevicePhoto;
import org.jeecg.modules.iot.device.service.IotDevicePhotoService;
import org.jeecg.modules.iot.device.vo.AccDevicePhotoVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoints for managing access control device photos.
 */
@Tag(name = "IOT-ACC:设备照片管理")
@RestController
@RequestMapping("/iot/accDevicePhoto")
@RequiredArgsConstructor
@Slf4j
public class IotDevicePhotoController extends JeecgController<IotDevicePhoto, IotDevicePhotoService> {

    private final IotDevicePhotoService iotDevicePhotoService;

    @GetMapping("/findByLogTime")
    @Operation(summary = "根据设备SN和日志时间查询照片完整信息")
    public Result<AccDevicePhotoVO> findByLogTime(
            @Parameter(description = "设备序列号", required = true)
            @RequestParam String sn,
            @Parameter(description = "日志时间，格式: 2025-10-03 19:21:32", required = true)
            @RequestParam String logTime) {
        
        try {
            // 参数验证
            if (StringUtils.isBlank(sn)) {
                return Result.error("设备序列号不能为空");
            }
            if (StringUtils.isBlank(logTime)) {
                return Result.error("日志时间不能为空");
            }
            
            log.info("查询设备照片: sn={}, logTime={}", sn, logTime);
            
            // 查询照片完整信息
            AccDevicePhotoVO photoInfo = iotDevicePhotoService.findPhotoInfoBySnAndLogTime(sn, logTime);
            
            if (photoInfo == null) {
                log.warn("未找到匹配的照片: sn={}, logTime={}", sn, logTime);
                return Result.error("未找到匹配的照片记录");
            }
            
            log.info("找到照片信息: photoPath={}, photoName={}, fileSize={}, uploadedTime={}", 
                    photoInfo.getPhotoPath(), photoInfo.getPhotoName(), 
                    photoInfo.getFileSize(), photoInfo.getUploadedTime());
            return Result.OK(photoInfo);
            
        } catch (Exception e) {
            log.error("查询设备照片失败: sn={}, logTime={}", sn, logTime, e);
            return Result.error("查询失败: " + e.getMessage());
        }
    }
}
