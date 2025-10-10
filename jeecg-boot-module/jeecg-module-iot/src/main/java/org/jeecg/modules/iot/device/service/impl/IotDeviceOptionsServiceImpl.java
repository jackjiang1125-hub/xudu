package org.jeecg.modules.iot.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.iot.device.entity.IotDeviceOptions;
import org.jeecg.modules.iot.device.mapper.IotDeviceOptionsMapper;
import org.jeecg.modules.iot.device.service.IotDeviceOptionsService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 设备参数表 Service 实现类
 */
@Slf4j
@Service
public class IotDeviceOptionsServiceImpl extends ServiceImpl<IotDeviceOptionsMapper, IotDeviceOptions>
        implements IotDeviceOptionsService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDeviceOptions(String sn, String deviceId, Map<String, String> options, 
                                 String rawPayload, String clientIp, LocalDateTime reportTime) {
        if (StringUtils.isBlank(sn) || options == null || options.isEmpty()) {
            log.warn("Invalid parameters for saving device options: sn={}, options={}", sn, options);
            return;
        }

        // 先删除该设备的所有旧参数
        if (StringUtils.isNotBlank(deviceId)) {
            deleteByDeviceId(deviceId);
        } else {
            deleteByDeviceSn(sn);
        }

        // 批量插入新参数
        List<IotDeviceOptions> optionsList = new ArrayList<>();
        for (Map.Entry<String, String> entry : options.entrySet()) {
            String paramName = entry.getKey();
            String paramValue = entry.getValue();
            
            if (StringUtils.isBlank(paramName)) {
                continue;
            }

            IotDeviceOptions option = new IotDeviceOptions();
            option.setDeviceId(deviceId);
            option.setSn(sn);
            option.setParamName(paramName);
            option.setParamValue(paramValue);
            option.setParamType(determineParamType(paramValue));
            option.setReportTime(reportTime);
            option.setRawPayload(rawPayload);
            option.setClientIp(clientIp);
            
            optionsList.add(option);
        }

        if (!optionsList.isEmpty()) {
            saveBatch(optionsList);
            log.info("Saved {} device options for device sn={}", optionsList.size(), sn);
        }
    }

    @Override
    public void deleteByDeviceSn(String sn) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        QueryWrapper<IotDeviceOptions> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sn", sn);
        remove(queryWrapper);
    }

    @Override
    public void deleteByDeviceId(String deviceId) {
        if (StringUtils.isBlank(deviceId)) {
            return;
        }
        QueryWrapper<IotDeviceOptions> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("device_id", deviceId);
        remove(queryWrapper);
    }

    /**
     * 根据参数值判断参数类型
     */
    private String determineParamType(String value) {
        if (StringUtils.isBlank(value)) {
            return "STRING";
        }
        
        // 判断是否为布尔值
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value) ||
            "1".equals(value) || "0".equals(value)) {
            return "BOOLEAN";
        }
        
        // 判断是否为整数
        try {
            Integer.parseInt(value);
            return "INTEGER";
        } catch (NumberFormatException e) {
            // 不是整数，继续判断
        }
        
        // 默认为字符串类型
        return "STRING";
    }
}
