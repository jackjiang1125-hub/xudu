package org.jeecg.modules.pos.convert;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.jeecg.modules.pos.entity.PosRestaurant;
import org.jeecg.modules.pos.vo.PosRestaurantVO;
import java.util.List;

/**
 * 餐厅信息对象转换工具
 */
@Mapper(componentModel = "spring")
public interface PosRestaurantConvert {

    PosRestaurantConvert INSTANCE = Mappers.getMapper(PosRestaurantConvert.class);

    /**
     * Entity转VO
     */
    PosRestaurantVO toVO(PosRestaurant entity);

    /**
     * VO转Entity
     */
    PosRestaurant toEntity(PosRestaurantVO vo);

    /**
     * Entity列表转VO列表
     */
    List<PosRestaurantVO> toVOList(List<PosRestaurant> entityList);

    /**
     * VO列表转Entity列表
     */
    List<PosRestaurant> toEntityList(List<PosRestaurantVO> voList);
}