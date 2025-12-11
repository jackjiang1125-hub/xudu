package org.jeecg.modules.pos.mapstruct;


import org.jeecg.modules.pos.entity.PosConsumeRuleBinding;
import org.jeecg.modules.pos.vo.ConsumeRuleBindingVO;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * 规则绑定 MapStruct
 */
@Mapper(componentModel = "spring")
public interface ConsumeRuleBindingMapstruct {

    ConsumeRuleBindingVO toVO(PosConsumeRuleBinding entity);

    List<ConsumeRuleBindingVO> toVOList(List<PosConsumeRuleBinding> list);
}
