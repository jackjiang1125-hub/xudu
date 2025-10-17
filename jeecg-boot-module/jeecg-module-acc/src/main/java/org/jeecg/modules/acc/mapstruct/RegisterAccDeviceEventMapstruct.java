package org.jeecg.modules.acc.mapstruct;

import org.jeecg.modules.acc.entity.AccDevice;
import org.jeecg.modules.events.acc.RegisterAccDeviceEvent;
import org.jeecgframework.boot.acc.vo.AccDeviceVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RegisterAccDeviceEventMapstruct {

    @Mapping(target = "deviceType", constant = "acc")
    AccDevice toAccDevice(RegisterAccDeviceEvent registerAccDeviceEvent);
}
