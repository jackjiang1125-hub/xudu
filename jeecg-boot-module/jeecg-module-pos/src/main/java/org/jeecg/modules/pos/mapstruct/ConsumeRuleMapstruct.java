package org.jeecg.modules.pos.mapstruct;


import org.jeecg.modules.pos.dto.ConsumeRuleDTO;
import org.jeecg.modules.pos.entity.PosConsumeRule;
import org.jeecg.modules.pos.vo.ConsumeRuleVO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * 消费规则 MapStruct
 */
@Mapper(componentModel = "spring")
public interface ConsumeRuleMapstruct {

    PosConsumeRule toEntity(ConsumeRuleDTO dto);

    ConsumeRuleVO toVO(PosConsumeRule entity);

    List<ConsumeRuleVO> toVOList(List<PosConsumeRule> list);

    /**
     * 用 DTO 覆盖更新实体
     */
    void updateEntityFromDTO(ConsumeRuleDTO dto, @MappingTarget PosConsumeRule entity);
}
