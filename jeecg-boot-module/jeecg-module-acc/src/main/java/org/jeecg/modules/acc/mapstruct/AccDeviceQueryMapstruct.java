package org.jeecg.modules.acc.mapstruct;

import org.jeecg.modules.acc.entity.AccDevice;
import org.jeecgframework.boot.acc.query.AccDeviceQuery;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface AccDeviceQueryMapstruct {
    AccDevice toIotDevice(AccDeviceQuery accDeviceQuery);
}
