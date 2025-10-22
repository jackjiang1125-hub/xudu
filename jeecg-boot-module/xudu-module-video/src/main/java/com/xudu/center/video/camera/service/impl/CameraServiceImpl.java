package com.xudu.center.video.camera.service.impl;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import com.xudu.center.video.camera.constant.CameraRtspConstants;
import com.xudu.center.video.camera.entity.CameraDevice;
import com.xudu.center.video.camera.mapper.CameraMapper;
import com.xudu.center.video.camera.onvif.OnvifDiscoveryClient;
import com.xudu.center.video.camera.onvif.dto.OnvifDiscoveryResponse;
import com.xudu.center.video.camera.service.CameraService;
import com.xudu.center.video.camera.service.IZlmService;
import com.xudu.center.video.camera.vo.CameraVO;
import com.xudu.center.video.camera.vo.PlayUrlVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class CameraServiceImpl extends JeecgServiceImpl<CameraMapper, CameraDevice> implements CameraService {

    private static final String DEFAULT_RTSP_PATH = "/h264";
    private static final String DEFAULT_ROLE = "camera";
    private static final String DEFAULT_ONVIF_PORT = "443";

    @Autowired
    private IZlmService zlmService;

    @Autowired
    private OnvifDiscoveryClient onvifDiscoveryClient;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public void addAll2ZLM() {
        List<CameraDevice> list = list();
        list.stream()
                .filter(device -> StringUtils.hasText(device.getStreamId()))
                .forEach(cameraDevice -> {
                    CameraVO cameraVO = new CameraVO();
                    BeanUtils.copyProperties(cameraDevice, cameraVO);
                    zlmService.addStreamProxy(cameraVO);
                });
    }

    @Override
    public CameraDevice create(CameraVO vo) {
        return create(vo, true);
    }

    @Override
    public CameraDevice create(CameraVO vo, boolean pushToZlm) {
        CameraDevice entity = new CameraDevice();
        if (vo != null && StringUtils.hasText(vo.getId())) {
            entity.setId(vo.getId());
        }
        if (pushToZlm && vo != null && !StringUtils.hasText(vo.getStreamId())) {
            vo.setStreamId(generateStreamId());
        }
        copyFromVo(entity, vo, true, pushToZlm);
        if (pushToZlm) {
            PlayUrlVO playUrlVO = zlmService.addStreamProxy(vo);
            entity.setHlsUrl(playUrlVO.getHls());
            entity.setFlvUrl(playUrlVO.getFlv());
            entity.setWebrtcApi(playUrlVO.getWebrtcApi());
        }
        save(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CameraDevice> register(CameraVO vo) {
        Assert.notNull(vo, "Register request cannot be null");
        Assert.hasText(vo.getIp(), "Device IP must not be empty");
        Assert.hasText(vo.getUsername(), "Username must not be empty");
        Assert.hasText(vo.getPassword(), "Password must not be empty");
        String onvifUsername = StringUtils.hasText(vo.getOnvifUsername()) ? vo.getOnvifUsername() : vo.getUsername();
        String onvifPassword = StringUtils.hasText(vo.getOnvifPassword()) ? vo.getOnvifPassword() : vo.getPassword();

        vo.setOnvifUsername(onvifUsername);
        vo.setOnvifPassword(onvifPassword);

        String port = StringUtils.hasText(vo.getPort()) ? vo.getPort() : DEFAULT_ONVIF_PORT;
        OnvifDiscoveryResponse discovery = onvifDiscoveryClient.discover(vo.getIp(), port, onvifUsername, onvifPassword);
        if (discovery == null) {
            throw new IllegalStateException("ONVIF discovery returned no data");
        }

        String capabilitiesJson = toJson(discovery.getCapabilities());
        String rawPayload = toJson(discovery);

        if (!"nvr".equalsIgnoreCase(discovery.getRole())) {
            CameraVO single = new CameraVO();
            BeanUtils.copyProperties(vo, single);
            single.setId(null);
            if (!StringUtils.hasText(single.getStreamId())) {
                single.setStreamId(generateStreamId());
            }
            enrichWithOnvif(single, discovery, capabilitiesJson, rawPayload, 1);
            CameraDevice device = create(single, true);
            return Collections.singletonList(device);
        }

        CameraVO parentVo = new CameraVO();
        BeanUtils.copyProperties(vo, parentVo);
        parentVo.setId(null);
        parentVo.setRole("nvr");
        parentVo.setParentId(null);
        parentVo.setMediaVersion(discovery.getMediaVersion());
        parentVo.setChannelCount(discovery.getChannelCount());
        parentVo.setStateHash(discovery.getStateHash());
        parentVo.setCapabilitiesJson(capabilitiesJson);
        parentVo.setRawOnvifPayload(rawPayload);
        if (discovery.getDevice() != null) {
            OnvifDiscoveryResponse.DeviceInfo deviceInfo = discovery.getDevice();
            if (!StringUtils.hasText(parentVo.getDeviceName()) && StringUtils.hasText(deviceInfo.getModel())) {
                parentVo.setDeviceName(deviceInfo.getModel());
            }
            parentVo.setDeviceManufacturer(deviceInfo.getManufacturer());
            parentVo.setDeviceModel(deviceInfo.getModel());
            parentVo.setDeviceFirmwareVersion(deviceInfo.getFirmwareVersion());
            parentVo.setDeviceSerialNumber(deviceInfo.getSerialNumber());
            parentVo.setDeviceHardwareId(deviceInfo.getHardwareId());
        }
        parentVo.setStreamId("no_zlm");
        parentVo.setRtspUrl(null);
        parentVo.setOnvifRtspUrl(null);
        CameraDevice parent = create(parentVo, false);

        List<CameraDevice> created = new ArrayList<>();
        created.add(parent);

        if (CollectionUtils.isEmpty(discovery.getChannels())) {
            return created;
        }

        String baseName = StringUtils.hasText(vo.getDeviceName())
                ? vo.getDeviceName()
                : (discovery.getDevice() != null && StringUtils.hasText(discovery.getDevice().getModel())
                ? discovery.getDevice().getModel()
                : "NVR");

        int index = 1;
        for (OnvifDiscoveryResponse.Channel channel : discovery.getChannels()) {
            if (channel == null) {
                continue;
            }
            index++;
            OnvifDiscoveryResponse.ChannelProfile profile = selectProfile(channel);
            if (profile == null || !StringUtils.hasText(profile.getRtsp())) {
                continue;
            }
            CameraVO childVo = new CameraVO();
            BeanUtils.copyProperties(vo, childVo);
            childVo.setId(null);
            childVo.setType("ipc");//nvr下面的一定是IPC
            String channelName = StringUtils.hasText(profile.getName()) ? profile.getName() : ("CH" + index);
            childVo.setDeviceName(baseName + "-" + channelName);
            childVo.setParentId(parent.getId());
            childVo.setRole("ipc");
            childVo.setChannelIndex(index);
            childVo.setStreamId(generateStreamId());
            childVo.setSourceToken(channel.getSourceToken());
            childVo.setProfileToken(profile.getToken());
            childVo.setProfileKind(profile.getKind());
            childVo.setProfileName(profile.getName());
            childVo.setOnvifRtspUrl(profile.getRtsp());
            childVo.setRtspUrl(profile.getRtsp());
            childVo.setMediaVersion(discovery.getMediaVersion());
            childVo.setChannelCount(discovery.getChannelCount());
            childVo.setStateHash(discovery.getStateHash());
            childVo.setCapabilitiesJson(capabilitiesJson);
            childVo.setRawOnvifPayload(rawPayload);
            childVo.setIp("--");
            CameraDevice child = create(childVo, true);
            created.add(child);
        }
        return created;
    }

    @Override
    public CameraDevice update(String id, CameraVO vo) {
        CameraDevice entity = getById(id);
        if (entity == null) {
            throw new IllegalArgumentException("Camera device not found: " + id);
        }
        copyFromVo(entity, vo, false, false);
        updateById(entity);
        return entity;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean delete(String id) {
        if (!StringUtils.hasText(id)) {
            return false;
        }
        CameraDevice root = getById(id);
        if (root == null) {
            return false;
        }

        List<CameraDevice> devicesToRemove = new ArrayList<>();
        Deque<CameraDevice> stack = new ArrayDeque<>();
        stack.push(root);
        while (!stack.isEmpty()) {
            CameraDevice current = stack.pop();
            if (current == null) {
                continue;
            }
            devicesToRemove.add(current);
            List<CameraDevice> children = list(new LambdaQueryWrapper<CameraDevice>()
                    .eq(CameraDevice::getParentId, current.getId()));
            if (!CollectionUtils.isEmpty(children)) {
                children.forEach(stack::push);
            }
        }

        devicesToRemove.stream()
                .map(CameraDevice::getStreamId)
                .filter(StringUtils::hasText)
                .distinct()
                .forEach(streamId -> {
                    try {
                        zlmService.removeStreamProxy(streamId);
                    } catch (Exception ex) {
                        log.warn("Failed to remove ZLM stream proxy for {}: {}", streamId, ex.getMessage());
                    }
                });

        List<String> ids = devicesToRemove.stream()
                .map(CameraDevice::getId)
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            return true;
        }
        return removeByIds(ids);
    }

    @Override
    public List<CameraDevice> listTopLevel() {
        LambdaQueryWrapper<CameraDevice> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(CameraDevice::getParentId)
                .or(w -> w.eq(CameraDevice::getParentId, ""));
        wrapper.orderByDesc(CameraDevice::getCreateTime);
        return list(wrapper);
    }

    @Override
    public List<CameraDevice> listChildren(String parentId) {
        if (!StringUtils.hasText(parentId)) {
            return Collections.emptyList();
        }
        return list(new LambdaQueryWrapper<CameraDevice>()
                .eq(CameraDevice::getParentId, parentId)
                .orderByAsc(CameraDevice::getChannelIndex)
                .orderByDesc(CameraDevice::getCreateTime));
    }

    @Override
    public List<CameraDevice> listTopLevelNvr() {
        LambdaQueryWrapper<CameraDevice> wrapper = new LambdaQueryWrapper<>();
        wrapper.isNull(CameraDevice::getParentId)
                .or(w -> w.eq(CameraDevice::getParentId, ""));
        wrapper.in(CameraDevice::getType, Arrays.asList("nvr", "NVR"));
        wrapper.orderByDesc(CameraDevice::getCreateTime);
        return list(wrapper);
    }

    @Override
    public CameraDevice getDetail(String id) {
        return getById(id);
    }

    private void copyFromVo(CameraDevice target, CameraVO source, boolean isCreate, boolean generateStreamId) {
        if (target == null || source == null) {
            return;
        }
        target.setDeviceName(source.getDeviceName());
        target.setType(source.getType());
        String vendor = resolveVendor(source);
        target.setVendor(vendor);
        target.setUsername(source.getUsername());
        target.setPassword(source.getPassword());
        target.setOnvifUsername(source.getOnvifUsername());
        target.setOnvifPassword(source.getOnvifPassword());
        target.setIp(source.getIp());
        String port = CameraRtspConstants.resolvePort(vendor, source.getPort());
        target.setPort(port);

        String streamId = source.getStreamId();
        if (!StringUtils.hasText(streamId) && isCreate && generateStreamId) {
            streamId = generateStreamId();
        }
        if (StringUtils.hasText(streamId)) {
            target.setStreamId(streamId);
            source.setStreamId(streamId);
        }

        String rtspTransport = source.getRtspTransport();
        target.setRtspTransport(StringUtils.hasText(rtspTransport) ? rtspTransport : "tcp");

        Boolean onDemand = source.getOnDemand();
        target.setOnDemand(onDemand != null ? onDemand : Boolean.TRUE);

        target.setRtspPath(resolveRtspPath(source));

        target.setParentId(source.getParentId());
        target.setRole(StringUtils.hasText(source.getRole()) ? source.getRole() : DEFAULT_ROLE);
        target.setChannelIndex(source.getChannelIndex());
        target.setSourceToken(source.getSourceToken());
        target.setProfileToken(source.getProfileToken());
        target.setProfileKind(source.getProfileKind());
        target.setProfileName(source.getProfileName());
        target.setOnvifRtspUrl(source.getOnvifRtspUrl());
        target.setMediaVersion(source.getMediaVersion());
        target.setChannelCount(source.getChannelCount());
        target.setStateHash(source.getStateHash());
        target.setDeviceManufacturer(source.getDeviceManufacturer());
        target.setDeviceModel(source.getDeviceModel());
        target.setDeviceFirmwareVersion(source.getDeviceFirmwareVersion());
        target.setDeviceSerialNumber(source.getDeviceSerialNumber());
        target.setDeviceHardwareId(source.getDeviceHardwareId());
        target.setCapabilitiesJson(source.getCapabilitiesJson());
        target.setRawOnvifPayload(source.getRawOnvifPayload());
    }

    private void enrichWithOnvif(CameraVO target, OnvifDiscoveryResponse discovery, String capabilitiesJson,
                                 String rawPayload, int defaultChannelIndex) {
        target.setRole(StringUtils.hasText(discovery.getRole()) ? discovery.getRole() : DEFAULT_ROLE);
        target.setMediaVersion(discovery.getMediaVersion());
        target.setChannelCount(discovery.getChannelCount());
        target.setStateHash(discovery.getStateHash());
        target.setCapabilitiesJson(capabilitiesJson);
        target.setRawOnvifPayload(rawPayload);
        if (discovery.getDevice() != null) {
            OnvifDiscoveryResponse.DeviceInfo deviceInfo = discovery.getDevice();
            if (!StringUtils.hasText(target.getDeviceName()) && StringUtils.hasText(deviceInfo.getModel())) {
                target.setDeviceName(deviceInfo.getModel());
            }
            target.setDeviceManufacturer(deviceInfo.getManufacturer());
            target.setDeviceModel(deviceInfo.getModel());
            target.setDeviceFirmwareVersion(deviceInfo.getFirmwareVersion());
            target.setDeviceSerialNumber(deviceInfo.getSerialNumber());
            target.setDeviceHardwareId(deviceInfo.getHardwareId());
        }
        if (!CollectionUtils.isEmpty(discovery.getChannels())) {
            OnvifDiscoveryResponse.Channel channel = discovery.getChannels().get(0);
            if (channel != null) {
                target.setChannelIndex(defaultChannelIndex);
                target.setSourceToken(channel.getSourceToken());
                OnvifDiscoveryResponse.ChannelProfile profile = selectProfile(channel);
                if (profile != null) {
                    target.setProfileToken(profile.getToken());
                    target.setProfileKind(profile.getKind());
                    target.setProfileName(profile.getName());
                    if (StringUtils.hasText(profile.getRtsp())) {
                        target.setOnvifRtspUrl(profile.getRtsp());
                        target.setRtspUrl(profile.getRtsp());
                    }
                }
            }
        }
    }

    private OnvifDiscoveryResponse.ChannelProfile selectProfile(OnvifDiscoveryResponse.Channel channel) {
        if (channel == null || CollectionUtils.isEmpty(channel.getProfiles())) {
            return null;
        }
        for (OnvifDiscoveryResponse.ChannelProfile profile : channel.getProfiles()) {
            if ("sub".equalsIgnoreCase(profile.getKind())) {
                //拿子码流
                return profile;
            }
        }
        return channel.getProfiles().get(0);
    }

    private String resolveRtspPath(CameraVO source) {
        if (source == null) {
            return DEFAULT_RTSP_PATH;
        }
        String url = source.getRtspUrl();
        if (!StringUtils.hasText(url)) {
            return DEFAULT_RTSP_PATH;
        }
        int idx = url.lastIndexOf('/');
        if (idx < 0 || idx == url.length() - 1) {
            return DEFAULT_RTSP_PATH;
        }
        return url.substring(idx);
    }

    private String generateStreamId() {
        return "camera_" + UUID.randomUUID().toString().replace("-", "");
    }

    private String resolveVendor(CameraVO source) {
        if (source == null) {
            return null;
        }
        if (StringUtils.hasText(source.getVendor())) {
            return source.getVendor();
        }
        return source.getType();
    }

    private String toJson(Object value) {
        if (value == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to serialize ONVIF payload", e);
        }
    }
}
