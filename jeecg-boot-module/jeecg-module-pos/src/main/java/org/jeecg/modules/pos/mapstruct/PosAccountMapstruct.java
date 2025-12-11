package org.jeecg.modules.pos.mapstruct;

import org.jeecg.modules.pos.entity.PosAccount;
import org.jeecg.modules.pos.vo.PosAccountDetailVO;
import org.jeecg.modules.pos.vo.PosAccountListVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * POS账户 Mapstruct
 */
@Mapper
public interface PosAccountMapstruct {

    PosAccountMapstruct INSTANCE = Mappers.getMapper(PosAccountMapstruct.class);

    /**
     * 实体 -> 列表VO
     */
    @Mapping(target = "departmentId", source = "deptId")
    @Mapping(target = "departmentName", source = "deptName")
    PosAccountListVO toListVO(PosAccount entity);

    /**
     * 实体 -> 详情VO
     */
    @Mapping(target = "departmentId", source = "deptId")
    @Mapping(target = "departmentName", source = "deptName")
    @Mapping(target = "wallet", ignore = true)
    @Mapping(target = "limits", ignore = true)
    PosAccountDetailVO toDetailVO(PosAccount entity);

   // List<PosAccountListVO> toListVOList(List<PosAccount> entities);
}
