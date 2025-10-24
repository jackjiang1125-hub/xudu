package org.jeecg.modules.pos.mapstruct;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import org.jeecg.modules.pos.entity.PosProductCategory;
import org.jeecg.modules.pos.vo.PosProductCategoryVO;

/**
 * 商品分类对象转换
 */
@Mapper
public interface PosProductCategoryMapstruct {
    PosProductCategoryMapstruct INSTANCE = Mappers.getMapper(PosProductCategoryMapstruct.class);

    /**
     * 实体转VO
     */
    PosProductCategoryVO toVO(PosProductCategory entity);

    /**
     * VO转实体
     */
    PosProductCategory toEntity(PosProductCategoryVO vo);
}