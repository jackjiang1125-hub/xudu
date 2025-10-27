package org.jeecg.modules.pos.mapstruct;

import org.jeecg.modules.pos.entity.PosRestaurant;
import org.jeecg.modules.pos.vo.PosRestaurantVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * 餐厅信息Mapstruct
 * @author system
 * @date 2025-10-27
 */
@Mapper
public interface PosRestaurantMapstruct {
    
    PosRestaurantMapstruct INSTANCE = Mappers.getMapper(PosRestaurantMapstruct.class);
    
    /**
     * 实体转VO
     */
    PosRestaurantVO toVO(PosRestaurant entity);
    
    /**
     * VO转实体
     */
    PosRestaurant toEntity(PosRestaurantVO vo);
    
    /**
     * 实体列表转VO列表
     */
    List<PosRestaurantVO> toVOList(List<PosRestaurant> entityList);
    
    /**
     * VO列表转实体列表
     */
    List<PosRestaurant> toEntityList(List<PosRestaurantVO> voList);
}