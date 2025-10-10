package org.jeecg.modules.iot.device.service.impl;

import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.iot.device.entity.IotDeviceCommandReport;
import org.jeecg.modules.iot.device.mapper.IotDeviceCommandReportMapper;
import org.jeecg.modules.iot.device.service.IotDeviceCommandReportService;
import org.springframework.stereotype.Service;

@Service
public class IotDeviceCommandReportServiceImpl extends JeecgServiceImpl<IotDeviceCommandReportMapper, IotDeviceCommandReport>
        implements IotDeviceCommandReportService {
}
