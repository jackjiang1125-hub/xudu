package org.jeecg.modules.iot.device.mapstruct;

import org.jeecg.modules.iot.device.entity.IotDevice;
import org.jeecgframework.boot.iot.query.IotDeviceQuery;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IotDeviceQueryMapstruct {
    IotDevice toIotDevice(IotDeviceQuery iotDeviceQuery);
}
