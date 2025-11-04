package org.jeecg.modules.pos.mapstruct;

import org.jeecg.modules.pos.entity.PosConsumptionDetail;
import org.jeecg.modules.pos.vo.PosConsumptionDetailVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 消费记录明细Mapstruct
 */
@Mapper
public interface PosConsumptionDetailMapstruct {
    
    PosConsumptionDetailMapstruct INSTANCE = Mappers.getMapper(PosConsumptionDetailMapstruct.class);
    
    /**
     * 实体转VO
     */
    PosConsumptionDetailVO toVO(PosConsumptionDetail entity);
    
    /**
     * VO转实体
     */
    PosConsumptionDetail toEntity(PosConsumptionDetailVO vo);
    
    /**
     * 实体列表转VO列表
     */
    List<PosConsumptionDetailVO> toVOList(List<PosConsumptionDetail> entityList);
    
    /**
     * VO列表转实体列表
     */
    List<PosConsumptionDetail> toEntityList(List<PosConsumptionDetailVO> voList);
}