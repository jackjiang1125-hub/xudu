package org.jeecg.modules.acc.mapstruct;

import org.jeecg.modules.acc.entity.AccReader;
import org.jeecg.modules.acc.vo.AccReaderVO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * @Description: 读头管理 MapStruct转换器
 * @Author: jeecg-boot
 * @Date: 2025-01-26
 * @Version: V1.0
 */
@Mapper
public interface AccReaderMapstruct {

    AccReaderMapstruct INSTANCE = Mappers.getMapper(AccReaderMapstruct.class);

    /**
     * 实体转VO
     */
    AccReaderVO toVO(AccReader entity);

    /**
     * VO转实体
     */
    AccReader toEntity(AccReaderVO vo);

    /**
     * 实体列表转VO列表
     */
    List<AccReaderVO> toVOList(List<AccReader> entityList);

    /**
     * VO列表转实体列表
     */
    List<AccReader> toEntityList(List<AccReaderVO> voList);
}