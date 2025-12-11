package org.jeecg.modules.pos.mapstruct;

import org.jeecg.modules.pos.entity.PosAccountTransaction;
import org.jeecg.modules.pos.vo.PosAccountTransactionVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 账户交易 Mapstruct
 */
@Mapper
public interface PosAccountTransactionMapstruct {

    PosAccountTransactionMapstruct INSTANCE = Mappers.getMapper(PosAccountTransactionMapstruct.class);

    PosAccountTransactionVO toVO(PosAccountTransaction entity);

    List<PosAccountTransactionVO> toVOList(List<PosAccountTransaction> entities);
}
