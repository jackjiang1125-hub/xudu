package com.xudu.center.video.camera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xudu.center.video.camera.entity.Video;
import com.xudu.center.video.camera.mapstruct.VideoConverter;
import com.xudu.center.video.camera.mapstruct.VideoQueryMapstruct;
import com.xudu.center.video.camera.mapper.VideoMapper;
import com.xudu.center.video.camera.service.IVideoService;
import com.xudu.center.video.camera.vo.VideoQuery;
import com.xudu.center.video.camera.vo.VideoVO;
import com.xudu.center.zlm.api.IZlmService;
import com.xudu.center.zlm.dto.NormalizeResult;
import com.xudu.center.zlm.dto.PlayUrls;
import com.xudu.center.zlm.dto.ProxyAndNormalizeReq;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class VideoServiceImpl extends JeecgServiceImpl<VideoMapper, Video> implements IVideoService {

    @Autowired
    private IZlmService zlmService;
    
    @Autowired
    private VideoQueryMapstruct videoQueryMapstruct;
    
    @Autowired
    private VideoConverter videoConverter;

    @Override
    public void addVideo(VideoVO videoVO) {

        // 直接调用zlm一键添加 生成代理流

        ProxyAndNormalizeReq proxyAndNormalizeReq = new ProxyAndNormalizeReq();
        proxyAndNormalizeReq.setApp(videoVO.getApp());
        proxyAndNormalizeReq.setNaming("suffix");
        proxyAndNormalizeReq.setUrl(videoVO.getRtspUrl());
        proxyAndNormalizeReq.setStream(videoVO.getStream());
        proxyAndNormalizeReq.setCloseWhenNoConsumer(true);

        NormalizeResult normalizeResult = zlmService.addProxyAndNormalize(proxyAndNormalizeReq);

        // 填充信息
        videoVO.setAudioCodec(normalizeResult.getAudioCodec());
        videoVO.setVideoCodec(normalizeResult.getVideoCodec());
        videoVO.setFfmpegCmdKey(normalizeResult.getFfmpegCmdKey());
        videoVO.setWebRtcUrl(normalizeResult.getUrls().getWhep());
        videoVO.setHlsUrl(normalizeResult.getUrls().getHls());
        
        // 保存到数据库
        Video video = videoConverter.voToEntity(videoVO);
        
        // 检查是否已存在相同的流
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("stream", videoVO.getStream())
                   .eq("app", videoVO.getApp());
        Video existingVideo = this.getOne(queryWrapper);
        
        if (existingVideo != null) {
            // 更新现有记录
            video.setId(existingVideo.getId());
            this.updateById(video);
        } else {
            // 插入新记录
            this.save(video);
        }
    }

    @Override
    public PageResult<VideoVO> list(VideoQuery videoQuery, PageRequest pageRequest, Map<String, String[]> parameterMap) {
        // 一行代码搞定 DTO->Entity 查询之后 entity->VO
        return pageByQuery(videoQuery,
                pageRequest.getPageNo(),
                pageRequest.getPageSize(),
                parameterMap,
                videoQueryMapstruct::toVideo,
                videoConverter::entityToVo
                // ,qw -> qw.orderByDesc("create_time") //带排序
        );
    }

    @Override
    public VideoVO getById(String id) {
        Video video = super.getById(id);
        return video != null ? videoConverter.entityToVo(video) : null;
    }

    @Override
    public void updateVideo(VideoVO videoVO) {
        if (StringUtils.isBlank(videoVO.getId())) {
            throw new IllegalArgumentException("视频流ID不能为空");
        }
        
        Video video = videoConverter.voToEntity(videoVO);
        this.updateById(video);
    }

    @Override
    public void deleteVideo(String id) {
        if (StringUtils.isBlank(id)) {
            throw new IllegalArgumentException("视频流ID不能为空");
        }
        this.removeById(id);
    }

    @Override
    public void deleteBatchVideo(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("视频流ID列表不能为空");
        }
        this.removeByIds(ids);
    }
}
