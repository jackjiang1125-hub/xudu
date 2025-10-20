package org.jeecg.modules.acc.mapstruct;

import org.jeecg.modules.acc.entity.AccDoor;
import org.jeecg.modules.acc.vo.AccDoorVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 门列表转换器
 */
@Mapper(componentModel = "spring")
public interface AccDoorMapstruct {
    AccDoorVO toVO(AccDoor entity);
    AccDoor toEntity(AccDoorVO vo);
    List<AccDoorVO> toVOList(List<AccDoor> entities);
}