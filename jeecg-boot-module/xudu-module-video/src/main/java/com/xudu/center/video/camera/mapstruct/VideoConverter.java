package com.xudu.center.video.camera.mapstruct;

import com.xudu.center.video.camera.entity.Video;
import com.xudu.center.video.camera.vo.VideoVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Video VO to Entity converter using MapStruct
 */

@Mapper(componentModel = "spring")
public interface VideoConverter {
    
    VideoConverter INSTANCE = Mappers.getMapper(VideoConverter.class);
    
    /**
     * Convert VideoVO to Video entity
     * @param videoVO VideoVO object
     * @return Video entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createBy", ignore = true)
    @Mapping(target = "createTime", ignore = true)
    @Mapping(target = "updateBy", ignore = true)
    @Mapping(target = "updateTime", ignore = true)
    Video voToEntity(VideoVO videoVO);
    
    /**
     * Convert Video entity to VideoVO
     * @param video Video entity
     * @return VideoVO object
     */
    VideoVO entityToVo(Video video);
}
