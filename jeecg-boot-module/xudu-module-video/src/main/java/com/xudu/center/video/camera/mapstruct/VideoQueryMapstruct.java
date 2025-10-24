package com.xudu.center.video.camera.mapstruct;

import com.xudu.center.video.camera.entity.Video;
import com.xudu.center.video.vo.VideoQuery;
import org.mapstruct.Mapper;

/**
 * VideoQuery to Video entity converter using MapStruct
 */
@Mapper(componentModel = "spring")
public interface VideoQueryMapstruct {
    
    /**
     * Convert VideoQuery to Video entity for query conditions
     * @param videoQuery VideoQuery object
     * @return Video entity
     */
    Video toVideo(VideoQuery videoQuery);
}
