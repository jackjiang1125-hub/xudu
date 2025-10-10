package org.jeecg.modules.iot.device.mapstruct;


import org.jeecg.modules.events.acc.RegisterAccDeviceEvent;
import org.jeecg.modules.iot.device.entity.IotDevice;
import org.jeecgframework.boot.iot.vo.IotDeviceVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface IotDeviceMapstruct {

    @Mapping(target = "authorized", source = "authorized", qualifiedByName = "boolTo01Int")
    IotDeviceVO toIotDeviceVO(IotDevice iotDevice);

    List<IotDeviceVO> toVOList(List<IotDevice> iotDeviceList);


  //  @Mapping(target = "authorized", source = "authorized", qualifiedByName = "boolTo01Int")
    RegisterAccDeviceEvent  toRegisterAccDeviceEvent(IotDevice iotDevice);



    @Named("boolTo01Int")
    default Integer boolTo01Int(Boolean b) {
        return (b != null && b) ? 1 : 0;
    }
}
