package org.jeecg.modules.pos.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.pos.entity.PosProductCategory;

/**
 * 商品分类Mapper
 */
@Mapper
public interface PosProductCategoryMapper extends BaseMapper<PosProductCategory> {
}