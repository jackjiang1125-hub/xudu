package org.jeecg.modules.iot.device.service.impl;

import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.iot.device.entity.IotDeviceRtLog;
import org.jeecg.modules.iot.device.mapper.IotDeviceRtLogMapper;
import org.jeecg.modules.iot.device.service.IotDeviceRtLogService;
import org.springframework.stereotype.Service;

@Service
public class IotDeviceRtLogServiceImpl extends JeecgServiceImpl<IotDeviceRtLogMapper, IotDeviceRtLog>
        implements IotDeviceRtLogService {
}
