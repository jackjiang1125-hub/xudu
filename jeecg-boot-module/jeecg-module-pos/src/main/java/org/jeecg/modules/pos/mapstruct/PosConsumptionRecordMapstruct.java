package org.jeecg.modules.pos.mapstruct;

import org.jeecg.modules.pos.entity.PosConsumptionRecord;
import org.jeecg.modules.pos.vo.PosConsumptionRecordVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 消费记录Mapstruct
 */
@Mapper
public interface PosConsumptionRecordMapstruct {
    
    PosConsumptionRecordMapstruct INSTANCE = Mappers.getMapper(PosConsumptionRecordMapstruct.class);
    
    /**
     * 实体转VO
     */
    PosConsumptionRecordVO toVO(PosConsumptionRecord entity);
    
    /**
     * VO转实体
     */
    PosConsumptionRecord toEntity(PosConsumptionRecordVO vo);
    
    /**
     * 实体列表转VO列表
     */
    List<PosConsumptionRecordVO> toVOList(List<PosConsumptionRecord> entityList);
    
    /**
     * VO列表转实体列表
     */
    List<PosConsumptionRecord> toEntityList(List<PosConsumptionRecordVO> voList);
}