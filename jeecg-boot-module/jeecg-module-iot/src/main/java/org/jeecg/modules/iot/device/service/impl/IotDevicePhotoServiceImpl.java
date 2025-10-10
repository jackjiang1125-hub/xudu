package org.jeecg.modules.iot.device.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.iot.device.entity.IotDevicePhoto;
import org.jeecg.modules.iot.device.mapper.IotDevicePhotoMapper;
import org.jeecg.modules.iot.device.service.IotDevicePhotoService;
import org.jeecg.modules.iot.device.vo.AccDevicePhotoVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class IotDevicePhotoServiceImpl extends JeecgServiceImpl<IotDevicePhotoMapper, IotDevicePhoto>
        implements IotDevicePhotoService {
    
    @Override
    public AccDevicePhotoVO findPhotoInfoBySnAndLogTime(String sn, String logTime) {
        try {
            // 将日志时间从 "2025-10-03 19:21:32" 格式转换为 "20251003192132" 格式
            String timeStamp = convertLogTimeToTimeStamp(logTime);
            log.debug("时间格式转换: {} -> {}", logTime, timeStamp);
            
            // 使用转换后的时间戳查询完整信息
            return baseMapper.findPhotoInfoBySnAndTimeStamp(sn, timeStamp);
            
        } catch (Exception e) {
            log.error("时间格式转换失败: logTime={}", logTime, e);
            throw new RuntimeException("时间格式转换失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将日志时间转换为时间戳格式
     * @param logTime 日志时间 (格式: 2025-10-03 19:21:32)
     * @return 时间戳 (格式: 20251003192132)
     */
    private String convertLogTimeToTimeStamp(String logTime) {
        // 输入格式: "2025-10-03 19:21:32"
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // 输出格式: "20251003192132"
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
        
        LocalDateTime dateTime = LocalDateTime.parse(logTime, inputFormatter);
        return dateTime.format(outputFormatter);
    }
}
