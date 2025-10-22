package com.xudu.center.video.camera.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xudu.center.video.camera.entity.Video;
import com.xudu.center.video.camera.vo.VideoQuery;
import com.xudu.center.video.camera.vo.VideoVO;
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
}
