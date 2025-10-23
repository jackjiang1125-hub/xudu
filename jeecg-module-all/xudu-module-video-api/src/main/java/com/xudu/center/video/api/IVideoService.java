package com.xudu.center.video.api;


import com.xudu.center.video.vo.VideoQuery;
import com.xudu.center.video.vo.VideoVO;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;

import java.util.List;

public interface IVideoService {
    
    /**
     * 添加视频流
     */
    void addVideo(VideoVO videoVO);
    
    /**
     * 分页查询视频流列表
     */
    PageResult<VideoVO> list(VideoQuery videoQuery, PageRequest pageRequest, java.util.Map<String, String[]> parameterMap);
    
    /**
     * 根据ID查询视频流
     */
    VideoVO getById(String id);
    
    /**
     * 更新视频流
     */
    void updateVideo(VideoVO videoVO);
    
    /**
     * 删除视频流
     */
    void deleteVideo(String id);
    
    /**
     * 批量删除视频流
     */
    void deleteBatchVideo(List<String> ids);

    VideoVO findByAppStream(String app, String stream);
}
