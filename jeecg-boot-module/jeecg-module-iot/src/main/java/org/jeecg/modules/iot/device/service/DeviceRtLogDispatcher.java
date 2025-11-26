package org.jeecg.modules.iot.device.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.iot.device.cache.AccDeviceRedisCache;
import org.jeecg.modules.iot.device.entity.IotDeviceRtLog;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeviceRtLogDispatcher {

    private final IotDeviceRtLogService iotDeviceRtLogService;
    private final AccDeviceRedisCache redisCache;

    /**
     * 统一处理：批量入库 + 写入队列 + 发布订阅
     */
    public void dispatch(List<IotDeviceRtLog> logs) {
        if (CollectionUtils.isEmpty(logs)) {
            return;
        }
        iotDeviceRtLogService.saveBatch(logs);
        redisCache.enqueueRtLogs(logs);
        redisCache.publishRtLogMessages(logs);
        log.debug("Dispatched {} rtlogs", logs.size());
    }
}
