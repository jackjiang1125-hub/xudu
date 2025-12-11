package org.jeecg.modules.pos.mapstruct;


import org.jeecg.modules.pos.dto.ConsumeGroupDTO;
import org.jeecg.modules.pos.entity.PosConsumeGroup;
import org.jeecg.modules.pos.vo.ConsumeGroupVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 消费人群 MapStruct
 */
@Mapper(componentModel = "spring")
public interface ConsumeGroupMapstruct {

    PosConsumeGroup toEntity(ConsumeGroupDTO dto);

    ConsumeGroupVO toVO(PosConsumeGroup entity);

    List<ConsumeGroupVO> toVOList(List<PosConsumeGroup> list);

    void updateEntityFromDTO(ConsumeGroupDTO dto, @MappingTarget PosConsumeGroup entity);
}
