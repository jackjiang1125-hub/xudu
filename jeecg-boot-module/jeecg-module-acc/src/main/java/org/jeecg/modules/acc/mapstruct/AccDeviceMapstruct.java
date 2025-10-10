package org.jeecg.modules.acc.mapstruct;

import org.jeecg.modules.acc.entity.AccDevice;
import org.jeecgframework.boot.acc.vo.AccDeviceVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

/**
 * 门禁设备转换器
 * @author system
 * @date 2025-01-03
 */
@Mapper(componentModel = "spring")
public interface AccDeviceMapstruct {

    /**
     * 实体转VO
     */
    @Mapping(target = "authorized", source = "authorized", qualifiedByName = "boolTo01Int")
    AccDeviceVO toAccDeviceVO(AccDevice accDevice);

    /**
     * 实体列表转VO列表
     */
    List<AccDeviceVO> toVOList(List<AccDevice> accDeviceList);

    /**
     * VO转实体
     */
    @Mapping(target = "authorized", source = "authorized", qualifiedByName = "intToBool")
    AccDevice toAccDevice(AccDeviceVO accDeviceVO);

    /**
     * Boolean转Integer (1/0)
     */
    @Named("boolTo01Int")
    default Integer boolTo01Int(Boolean b) {
        return (b != null && b) ? 1 : 0;
    }

    /**
     * Integer转Boolean
     */
    @Named("intToBool")
    default Boolean intToBool(Integer i) {
        return i != null && i == 1;
    }
}
