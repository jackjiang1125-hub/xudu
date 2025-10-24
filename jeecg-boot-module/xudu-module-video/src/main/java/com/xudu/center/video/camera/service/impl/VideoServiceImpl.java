package com.xudu.center.video.camera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xudu.center.video.api.IVideoService;
import com.xudu.center.video.camera.entity.Video;
import com.xudu.center.video.camera.mapstruct.VideoConverter;
import com.xudu.center.video.camera.mapstruct.VideoQueryMapstruct;
import com.xudu.center.video.camera.mapper.VideoMapper;
import com.xudu.center.video.vo.VideoQuery;
import com.xudu.center.video.vo.VideoVO;
import com.xudu.center.zlm.api.IZlmService;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

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
        
        // 自动生成stream（不唯一，可读性较好）
        if (StringUtils.isBlank(videoVO.getStream())) {
            String shortUuid = UUID.randomUUID().toString();
            videoVO.setStream(videoVO.getManufacturer().substring(videoVO.getManufacturer().lastIndexOf("_")+1) + "_" + shortUuid);
        }
        
        // 根据厂商类型自动生成RTSP地址
        if (StringUtils.isBlank(videoVO.getRtspUrl())) {
            videoVO.setRtspUrl(generateRtspUrl(videoVO));
        }

        // 直接调用zlm一键添加 生成代理流
//        ProxyAndNormalizeReq proxyAndNormalizeReq = new ProxyAndNormalizeReq();
//        proxyAndNormalizeReq.setApp(videoVO.getApp());
//        proxyAndNormalizeReq.setNaming("suffix");
//        proxyAndNormalizeReq.setUrl(videoVO.getRtspUrl());
//        proxyAndNormalizeReq.setStream(videoVO.getStream());
//        proxyAndNormalizeReq.setCloseWhenNoConsumer(true);
//
//        NormalizeResult normalizeResult = zlmService.addProxyAndNormalize(proxyAndNormalizeReq);
//
//        // 填充信息
//        videoVO.setAudioCodec(normalizeResult.getAudioCodec());
//        videoVO.setVideoCodec(normalizeResult.getVideoCodec());
//        videoVO.setFfmpegCmdKey(normalizeResult.getFfmpegCmdKey());
//        videoVO.setWebRtcUrl(normalizeResult.getUrls().getWhep());
//        videoVO.setHlsUrl(normalizeResult.getUrls().getHls());
        
        // 保存到数据库
        Video video = videoConverter.voToEntity(videoVO);
        
        // 检查是否已存在相同的流
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("rtsp_url", videoVO.getRtspUrl());
        Video existingVideo = this.getOne(queryWrapper);
        if (existingVideo != null) {
            // 更新现有记录
            throw new JeecgBootException("已经存在该相机");
        } else {
            // 插入新记录
            this.save(video);
        }
    }
    
    /**
     * 根据厂商类型生成RTSP地址
     * @param videoVO 视频流信息
     * @return RTSP地址
     */
    private String generateRtspUrl(VideoVO videoVO) {
        String manufacturer = videoVO.getManufacturer();
        String ip = videoVO.getIp();
        String port = StringUtils.isNotBlank(videoVO.getPort()) ? videoVO.getPort() : "554";
        String username = videoVO.getUsername();
        String password = videoVO.getPassword();
        
        // 根据厂商类型生成不同的RTSP地址格式
        if ("xudu_manufacturer_HK".equalsIgnoreCase(manufacturer) || "海康威视".equals(manufacturer)) {
            // 海康威视格式: rtsp://username:password@ip/Streaming/Channels/101
            return String.format("rtsp://%s:%s@%s:%s/Streaming/Channels/101", 
                username, password, ip, port);
        } else if ("dahua".equalsIgnoreCase(manufacturer) || "大华".equals(manufacturer)) {
            // 大华格式: rtsp://username:password@ip:port/cam/realmonitor?channel=1&subtype=0
            return String.format("rtsp://%s:%s@%s:%s/cam/realmonitor?channel=1&subtype=0", 
                username, password, ip, port);
        } else if ("axis".equalsIgnoreCase(manufacturer) || "安讯士".equals(manufacturer)) {
            // 安讯士格式: rtsp://username:password@ip:port/axis-media/media.amp
            return String.format("rtsp://%s:%s@%s:%s/axis-media/media.amp", 
                username, password, ip, port);
        } else if ("xudu_manufacturer_ZK".equalsIgnoreCase(manufacturer) || "熵基".equals(manufacturer)) {
            // 熵基格式: rtsp://username:password@ip:port/ch01
            return String.format("rtsp://%s:%s@%s:%s/ch01", 
                username, password, ip, port);
        } else if ("xudu_manufacturer_ZS".equalsIgnoreCase(manufacturer) || "臻识".equals(manufacturer)) {
            // 臻识格式: rtsp://username:password@ip:port/h264
            return String.format("rtsp://%s:%s@%s:%s/h264", 
                username, password, ip, port);
        } else {
            // 默认格式: rtsp://username:password@ip:port/live
            return String.format("rtsp://%s:%s@%s:%s/live", 
                username, password, ip, port);
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

    @Override
    public VideoVO findByAppStream(String app, String stream) {
        if (StringUtils.isBlank(app) || StringUtils.isBlank(stream)) {
            return null;
        }
        
        QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("app", app);
        queryWrapper.eq("stream", stream);
        
        Video video = this.getOne(queryWrapper);
        return video != null ? videoConverter.entityToVo(video) : null;
    }
}
