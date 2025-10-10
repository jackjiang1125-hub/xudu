package org.jeecg.modules.iot.device.service.impl;

import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.iot.device.entity.IotDeviceState;
import org.jeecg.modules.iot.device.mapper.IotDeviceStateMapper;
import org.jeecg.modules.iot.device.service.IotDeviceStateService;
import org.springframework.stereotype.Service;

@Service
public class IotDeviceStateServiceImpl extends JeecgServiceImpl<IotDeviceStateMapper, IotDeviceState>
        implements IotDeviceStateService {
}
