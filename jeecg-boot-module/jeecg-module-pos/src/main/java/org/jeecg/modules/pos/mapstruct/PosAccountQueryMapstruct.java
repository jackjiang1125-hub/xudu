package org.jeecg.modules.pos.mapstruct;

import org.jeecg.modules.pos.entity.PosAccount;
import org.jeecg.modules.pos.request.PosAccountQuery;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * POS账户查询 Mapstruct
 */
@Mapper
public interface PosAccountQueryMapstruct {

    PosAccountQueryMapstruct INSTANCE = Mappers.getMapper(PosAccountQueryMapstruct.class);

    /**
     * 查询DTO转实体
     */
    PosAccount toEntity(PosAccountQuery query);
}
