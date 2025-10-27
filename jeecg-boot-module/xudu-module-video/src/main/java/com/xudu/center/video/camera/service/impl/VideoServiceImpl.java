package com.xudu.center.video.camera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xudu.center.video.api.IVideoService;
import com.xudu.center.video.camera.entity.Video;
import com.xudu.center.video.camera.entity.VideoStream;
import com.xudu.center.video.camera.mapstruct.VideoConverter;
import com.xudu.center.video.camera.mapstruct.VideoQueryMapstruct;
import com.xudu.center.video.camera.mapper.VideoMapper;
import com.xudu.center.video.camera.mapper.VideoStreamMapper;
import com.xudu.center.video.vo.StreamVO;
import com.xudu.center.video.vo.VideoQuery;
import com.xudu.center.video.vo.VideoVO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.hkclients.HKClients;
import org.jeecg.modules.hkclients.dto.HkConn;
import org.jeecg.modules.hkclients.dto.NvrDeviceOverview;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class VideoServiceImpl extends JeecgServiceImpl<VideoMapper, Video> implements IVideoService {

    private static final String MANUFACTURER_HK = "xudu_manufacturer_HK";
    private static final String TYPE_NVR = "xudu_video_nvr";
    private static final String TYPE_IPC = "ipc";
    private static final int DEFAULT_HK_HTTP_PORT = 80;
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_READ_TIMEOUT = 10000;

    @Autowired
    private VideoQueryMapstruct videoQueryMapstruct;

    @Autowired
    private VideoConverter videoConverter;

    @Autowired
    private VideoStreamMapper videoStreamMapper;

    @Autowired
    private HKClients hkClients;

    @Override
    public void addVideo(VideoVO videoVO) {
        if (isHikvisionNvr(videoVO)) {
            addHikvisionNvr(videoVO);
            return;
        }
        saveVideo(videoVO, true);
    }

    private void addHikvisionNvr(VideoVO request) {
        validateHikvisionRequest(request);

        HkConn conn = buildHkConn(request);
        NvrDeviceOverview overview = hkClients.buildOverviewWithRtsp(conn);
        if (overview == null) {
            throw new JeecgBootException("未获取到海康NVR信息");
        }

        VideoVO nvrVO = new VideoVO();
        nvrVO.setName(StringUtils.defaultIfBlank(request.getName(), overview.getDeviceName()));
        nvrVO.setUsername(request.getUsername());
        nvrVO.setPassword(request.getPassword());
        nvrVO.setIp(StringUtils.defaultIfBlank(overview.getIpv4Address(), request.getIp()));
        nvrVO.setPort(request.getPort());
        nvrVO.setManufacturer(request.getManufacturer());
        nvrVO.setModel(StringUtils.defaultIfBlank(overview.getModel(), request.getModel()));
        nvrVO.setType(TYPE_NVR);
        nvrVO.setApp(request.getApp());
        nvrVO.setStatus(request.getStatus());
        nvrVO.setStream(request.getStream());
        nvrVO.setFfmpegCmdKey(request.getFfmpegCmdKey());
        nvrVO.setHlsUrl(request.getHlsUrl());
        nvrVO.setWebRtcUrl(request.getWebRtcUrl());
        nvrVO.setDeviceId(overview.getDeviceId());
        nvrVO.setFirmwareVersion(overview.getFirmwareVersion());
        nvrVO.setMacAddress(overview.getMacAddress());
        nvrVO.setSerialNumber(overview.getSerialNumber());

        Video nvr = saveVideo(nvrVO, false);

        List<NvrDeviceOverview.NvrChannel> channels = Optional.ofNullable(overview.getChannels())
                .orElse(Collections.emptyList());
        for (NvrDeviceOverview.NvrChannel channelOverview : channels) {
            Video channel = saveHikvisionChannel(request, nvr, channelOverview);
            persistChannelStreams(request, channel, channelOverview);
        }
    }

    private Video saveHikvisionChannel(VideoVO request, Video nvr, NvrDeviceOverview.NvrChannel channelOverview) {
        VideoVO channelVO = new VideoVO();
        channelVO.setName(StringUtils.defaultIfBlank(channelOverview.getName(),
                "通道-" + Optional.ofNullable(channelOverview.getId()).orElse(0)));
        channelVO.setUsername(StringUtils.defaultIfBlank(channelOverview.getUserName(), request.getUsername()));
        channelVO.setPassword(request.getPassword());
        channelVO.setIp(StringUtils.defaultIfBlank(channelOverview.getIpAddress(), request.getIp()));
        channelVO.setPort("554");
        channelVO.setManufacturer(StringUtils.defaultIfBlank(channelOverview.getManufacturer(), request.getManufacturer()));
        channelVO.setModel(StringUtils.defaultIfBlank(channelOverview.getModel(), "IPC"));
        channelVO.setType(TYPE_IPC);
        channelVO.setApp(request.getApp());
        channelVO.setStatus(Boolean.TRUE.equals(channelOverview.getOnline()) ? "online" : "offline");
        channelVO.setParentId(nvr.getId());
        channelVO.setChannelNo(channelOverview.getId());
        channelVO.setSerialNumber(channelOverview.getSerialNumber());

        List<NvrDeviceOverview.NvrChannel.StreamInfo> streams =
                Optional.ofNullable(channelOverview.getStreams()).orElse(Collections.emptyList());
        NvrDeviceOverview.NvrChannel.StreamInfo primaryStream = pickPrimaryStream(streams);

        String primaryRtsp = primaryStream != null ? primaryStream.getRtsp() : channelOverview.getRtspMain();
        if (StringUtils.isBlank(primaryRtsp) && channelOverview.getId() != null) {
            int trackId = channelOverview.getId() * 100 + 1;
            primaryRtsp = "rtsp://" + request.getIp() + ":554/ISAPI/Streaming/channels/" + trackId;
        }
        primaryRtsp = withCredential(primaryRtsp, request.getUsername(), request.getPassword());
        channelVO.setRtspUrl(primaryRtsp);

        if (primaryStream != null) {
            channelVO.setVideoCodec(primaryStream.getVideoCodec());
            channelVO.setAudioCodec(primaryStream.getAudioCodec());
        }

        return saveVideo(channelVO, false);
    }

    private void persistChannelStreams(VideoVO request, Video channel, NvrDeviceOverview.NvrChannel channelOverview) {
        List<NvrDeviceOverview.NvrChannel.StreamInfo> streams =
                Optional.ofNullable(channelOverview.getStreams()).orElse(Collections.emptyList());
        for (NvrDeviceOverview.NvrChannel.StreamInfo streamInfo : streams) {
            VideoStream streamEntity = new VideoStream();
            streamEntity.setVideoId(channel.getId());
            streamEntity.setTrackId(streamInfo.getTrackId());
            Integer streamNo = extractStreamNo(streamInfo.getTrackId());
            streamEntity.setStreamNo(streamNo);
            streamEntity.setStreamName(resolveStreamName(streamNo));
            String streamRtsp = streamInfo.getRtsp();
            if (StringUtils.isBlank(streamRtsp) && streamInfo.getTrackId() != null) {
                streamRtsp = "rtsp://" + request.getIp() + ":554/ISAPI/Streaming/channels/" + streamInfo.getTrackId();
            }
            streamEntity.setRtspUrl(withCredential(streamRtsp, request.getUsername(), request.getPassword()));
            streamEntity.setVideoCodec(streamInfo.getVideoCodec());
            streamEntity.setWidth(streamInfo.getWidth());
            streamEntity.setHeight(streamInfo.getHeight());
            streamEntity.setFrameRate(streamInfo.getFrameRate());
            streamEntity.setBitRateType(streamInfo.getBitRateType());
            streamEntity.setBitRate(streamInfo.getBitRate());
            streamEntity.setProfile(streamInfo.getProfile());
            streamEntity.setGop(streamInfo.getGop());
            streamEntity.setAudioEnabled(streamInfo.getAudioEnabled());
            streamEntity.setAudioCodec(streamInfo.getAudioCodec());
            streamEntity.setAudioSampleRate(streamInfo.getAudioSampleRate());
            streamEntity.setAudioChannels(streamInfo.getAudioChannels());
            streamEntity.setAudioBitRate(streamInfo.getAudioBitRate());
            videoStreamMapper.insert(streamEntity);
        }
    }

    private boolean isHikvisionNvr(VideoVO videoVO) {
        return videoVO != null
                && TYPE_NVR.equalsIgnoreCase(StringUtils.trimToEmpty(videoVO.getType()))
                && MANUFACTURER_HK.equalsIgnoreCase(StringUtils.trimToEmpty(videoVO.getManufacturer()));
    }

    private void validateHikvisionRequest(VideoVO request) {
        if (StringUtils.isAnyBlank(request.getIp(), request.getUsername(), request.getPassword())) {
            throw new JeecgBootException("添加海康NVR需要提供IP、用户名和密码");
        }
    }

    private HkConn buildHkConn(VideoVO request) {
        int port = NumberUtils.toInt(request.getPort(), DEFAULT_HK_HTTP_PORT);
        return HkConn.builder()
                .host(request.getIp())
                .port(port)
                .username(request.getUsername())
                .password(request.getPassword())
                .connectTimeoutMs(DEFAULT_CONNECT_TIMEOUT)
                .readTimeoutMs(DEFAULT_READ_TIMEOUT)
                .build();
    }

    private Video saveVideo(VideoVO videoVO, boolean autoGenerateRtsp) {
        ensureStream(videoVO, null);
        if (autoGenerateRtsp && StringUtils.isBlank(videoVO.getRtspUrl())) {
            videoVO.setRtspUrl(generateRtspUrl(videoVO));
        }

        Video video = videoConverter.voToEntity(videoVO);

        if (StringUtils.isNotBlank(videoVO.getRtspUrl())) {
            QueryWrapper<Video> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("rtsp_url", videoVO.getRtspUrl());
            Video existingVideo = this.getOne(queryWrapper);
            if (existingVideo != null) {
                throw new JeecgBootException("已经存在该相同的RTSP地址");
            }
        }

        this.save(video);
        return video;
    }

    private void ensureStream(VideoVO videoVO, String prefixOverride) {
        if (videoVO == null || StringUtils.isNotBlank(videoVO.getStream())) {
            return;
        }
        String prefix = StringUtils.defaultIfBlank(prefixOverride,
                StringUtils.defaultIfBlank(videoVO.getManufacturer(), "stream"));
        int idx = prefix.lastIndexOf('_');
        if (idx >= 0 && idx < prefix.length() - 1) {
            prefix = prefix.substring(idx + 1);
        }
        prefix = prefix.replaceAll("[^A-Za-z0-9]", "");
        if (StringUtils.isBlank(prefix)) {
            prefix = "stream";
        }
        String random = UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        videoVO.setStream(prefix + "_" + random);
    }

    private NvrDeviceOverview.NvrChannel.StreamInfo pickPrimaryStream(List<NvrDeviceOverview.NvrChannel.StreamInfo> streams) {
        for (NvrDeviceOverview.NvrChannel.StreamInfo stream : streams) {
            Integer streamNo = extractStreamNo(stream.getTrackId());
            if (streamNo != null && streamNo == 1) {
                return stream;
            }
        }
        return streams.isEmpty() ? null : streams.get(0);
    }

    private Integer extractStreamNo(Integer trackId) {
        if (trackId == null) {
            return null;
        }
        int positive = Math.abs(trackId);
        return positive % 100;
    }

    private String resolveStreamName(Integer streamNo) {
        if (streamNo == null) {
            return null;
        }
        switch (streamNo) {
            case 1:
                return "main";
            case 2:
                return "sub";
            case 3:
                return "third";
            default:
                return "stream" + streamNo;
        }
    }

    private String withCredential(String rtspUrl, String username, String password) {
        if (StringUtils.isBlank(rtspUrl) || StringUtils.isAnyBlank(username, password)) {
            return rtspUrl;
        }
        try {
            URI uri = URI.create(rtspUrl);
            if (StringUtils.isNotBlank(uri.getUserInfo())) {
                return rtspUrl;
            }
            URI rebuilt = new URI(
                    uri.getScheme(),
                    username + ":" + password,
                    uri.getHost(),
                    uri.getPort(),
                    uri.getPath(),
                    uri.getQuery(),
                    uri.getFragment()
            );
            return rebuilt.toString();
        } catch (Exception ex) {
            return rtspUrl;
        }
    }

    private List<String> collectVideoHierarchyIds(String rootId) {
        Video root = super.getById(rootId);
        if (root == null) {
            return Collections.emptyList();
        }
        List<String> ordered = new ArrayList<>();
        Set<String> visited = new HashSet<>();
        Deque<String> queue = new ArrayDeque<>();
        queue.add(rootId);
        visited.add(rootId);
        while (!queue.isEmpty()) {
            String current = queue.poll();
            ordered.add(current);
            QueryWrapper<Video> wrapper = new QueryWrapper<>();
            wrapper.eq("parent_id", current);
            List<Video> children = this.list(wrapper);
            for (Video child : children) {
                if (visited.add(child.getId())) {
                    queue.add(child.getId());
                }
            }
        }
        return ordered;
    }

    private void removeStreamsByVideoIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        QueryWrapper<VideoStream> wrapper = new QueryWrapper<>();
        wrapper.in("video_id", ids);
        videoStreamMapper.delete(wrapper);
    }

    private Map<String, List<StreamVO>> fetchStreamsByVideoIds(Collection<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyMap();
        }
        Set<String> distinct = ids.stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
        if (distinct.isEmpty()) {
            return Collections.emptyMap();
        }
        QueryWrapper<VideoStream> wrapper = new QueryWrapper<>();
        wrapper.in("video_id", distinct);
        List<VideoStream> streamEntities = videoStreamMapper.selectList(wrapper);
        if (streamEntities == null || streamEntities.isEmpty()) {
            return Collections.emptyMap();
        }
        return streamEntities.stream()
                .collect(Collectors.groupingBy(
                        VideoStream::getVideoId,
                        Collectors.mapping(this::toStreamVo, Collectors.toList())
                ));
    }

    private StreamVO toStreamVo(VideoStream stream) {
        StreamVO vo = new StreamVO();
        vo.setId(stream.getId());
        vo.setVideoId(stream.getVideoId());
        vo.setStreamNo(stream.getStreamNo());
        vo.setTrackId(stream.getTrackId());
        vo.setStreamName(stream.getStreamName());
        vo.setRtspUrl(stream.getRtspUrl());
        vo.setVideoCodec(stream.getVideoCodec());
        vo.setWidth(stream.getWidth());
        vo.setHeight(stream.getHeight());
        vo.setFrameRate(stream.getFrameRate());
        vo.setBitRateType(stream.getBitRateType());
        vo.setBitRate(stream.getBitRate());
        vo.setProfile(stream.getProfile());
        vo.setGop(stream.getGop());
        vo.setAudioEnabled(stream.getAudioEnabled());
        vo.setAudioCodec(stream.getAudioCodec());
        vo.setAudioSampleRate(stream.getAudioSampleRate());
        vo.setAudioChannels(stream.getAudioChannels());
        vo.setAudioBitRate(stream.getAudioBitRate());
        return vo;
    }

    private void assignStreams(List<VideoVO> videos, Map<String, List<StreamVO>> streamMap) {
        if (videos == null || videos.isEmpty()) {
            return;
        }
        for (VideoVO video : videos) {
            List<StreamVO> matched = streamMap.getOrDefault(video.getId(), Collections.emptyList());
            if (TYPE_IPC.equalsIgnoreCase(StringUtils.trimToEmpty(video.getType()))) {
                video.setStreams(new ArrayList<>(matched));
            } else {
                video.setStreams(new ArrayList<>());
            }
        }
    }

    /**
     * 根据厂商类型生成RTSP地址
     *
     * @param videoVO 视频流信息
     * @return RTSP地址
     */
    private String generateRtspUrl(VideoVO videoVO) {
        String manufacturer = videoVO.getManufacturer();
        String ip = videoVO.getIp();
        String port = StringUtils.isNotBlank(videoVO.getPort()) ? videoVO.getPort() : "554";
        String username = videoVO.getUsername();
        String password = videoVO.getPassword();

        if ("xudu_manufacturer_HK".equalsIgnoreCase(manufacturer) || "海康威视".equals(manufacturer)) {
            return String.format("rtsp://%s:%s@%s:%s/Streaming/Channels/101",
                    username, password, ip, port);
        } else if ("dahua".equalsIgnoreCase(manufacturer) || "大华".equals(manufacturer)) {
            return String.format("rtsp://%s:%s@%s:%s/cam/realmonitor?channel=1&subtype=0",
                    username, password, ip, port);
        } else if ("axis".equalsIgnoreCase(manufacturer) || "安讯士".equals(manufacturer)) {
            return String.format("rtsp://%s:%s@%s:%s/axis-media/media.amp",
                    username, password, ip, port);
        } else if ("xudu_manufacturer_ZK".equalsIgnoreCase(manufacturer) || "熵基".equals(manufacturer)) {
            return String.format("rtsp://%s:%s@%s:%s/ch01.264",
                    username, password, ip, port);
        } else if ("xudu_manufacturer_ZS".equalsIgnoreCase(manufacturer) || "臻识".equals(manufacturer)) {
            return String.format("rtsp://%s:%s@%s:%s/h264",
                    username, password, ip, port);
        } else if ("xudu_manufacturer_HW".equalsIgnoreCase(manufacturer) || "华为".equals(manufacturer)) {
            return String.format("rtsp://%s:%s@%s:%s/LiveMedia/ch1/Media1",
                    username, password, ip, port);
        } else {
            return String.format("rtsp://%s:%s@%s:%s/live",
                    username, password, ip, port);
        }
    }

    @Override
    public PageResult<VideoVO> list(VideoQuery videoQuery, PageRequest pageRequest, Map<String, String[]> parameterMap) {
        PageResult<VideoVO> page = pageByQuery(videoQuery,
                pageRequest.getPageNo(),
                pageRequest.getPageSize(),
                parameterMap,
                videoQueryMapstruct::toVideo,
                videoConverter::entityToVo,
                qw -> qw.isNull("parent_id"));

        List<VideoVO> roots = page.getRecords();
        if (roots == null || roots.isEmpty()) {
            return page;
        }

        Map<String, VideoVO> rootMap = new HashMap<>();
        List<String> allVideoIds = new ArrayList<>();
        for (VideoVO root : roots) {
            root.setChildren(new ArrayList<>());
            root.setStreams(new ArrayList<>());
            if (StringUtils.isNotBlank(root.getId())) {
                rootMap.put(root.getId(), root);
                allVideoIds.add(root.getId());
            }
        }

        if (!rootMap.isEmpty()) {
            QueryWrapper<Video> childWrapper = new QueryWrapper<>();
            childWrapper.in("parent_id", rootMap.keySet());
            List<Video> childEntities = this.list(childWrapper);
            if (childEntities != null && !childEntities.isEmpty()) {
                Map<String, List<VideoVO>> childrenGrouped = new HashMap<>();
                for (Video childEntity : childEntities) {
                    VideoVO childVo = videoConverter.entityToVo(childEntity);
                    childVo.setChildren(new ArrayList<>());
                    childVo.setStreams(new ArrayList<>());
                    if (StringUtils.isNotBlank(childVo.getId())) {
                        allVideoIds.add(childVo.getId());
                    }
                    String parentId = childVo.getParentId();
                    childrenGrouped.computeIfAbsent(parentId, k -> new ArrayList<>()).add(childVo);
                }
                for (Map.Entry<String, List<VideoVO>> entry : childrenGrouped.entrySet()) {
                    VideoVO parent = rootMap.get(entry.getKey());
                    if (parent != null) {
                        parent.getChildren().addAll(entry.getValue());
                    }
                }
                Map<String, List<StreamVO>> streamMap = fetchStreamsByVideoIds(allVideoIds);
                assignStreams(roots, streamMap);
                for (List<VideoVO> childList : childrenGrouped.values()) {
                    assignStreams(childList, streamMap);
                }
            } else {
                Map<String, List<StreamVO>> streamMap = fetchStreamsByVideoIds(allVideoIds);
                assignStreams(roots, streamMap);
            }
        }
        return page;
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
        List<String> hierarchyIds = collectVideoHierarchyIds(id);
        if (hierarchyIds.isEmpty()) {
            return;
        }
        removeStreamsByVideoIds(hierarchyIds);
        this.removeByIds(hierarchyIds);
    }

    @Override
    public void deleteBatchVideo(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new IllegalArgumentException("视频流ID列表不能为空");
        }
        Set<String> allIds = new HashSet<>();
        for (String id : ids) {
            allIds.addAll(collectVideoHierarchyIds(id));
        }
        if (allIds.isEmpty()) {
            return;
        }
        removeStreamsByVideoIds(allIds);
        this.removeByIds(allIds);
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
