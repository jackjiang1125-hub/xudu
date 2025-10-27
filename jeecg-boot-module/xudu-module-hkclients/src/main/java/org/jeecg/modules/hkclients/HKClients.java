package org.jeecg.modules.hkclients;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.hkclients.dto.HkConn;
import org.jeecg.modules.hkclients.dto.NvrDeviceOverview;
import org.jeecg.modules.hkclients.exception.HKClientException;
import org.jeecg.modules.hkclients.http.HikPooledClientManager;
import org.jeecg.modules.hkclients.model.content.InputProxyChannelList;
import org.jeecg.modules.hkclients.model.streaming.StreamingChannelList;
import org.jeecg.modules.hkclients.model.system.DeviceInfo;
import org.springframework.http.*;
import org.springframework.web.client.*;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class HKClients {

    private static final String XML_CT = "application/xml; charset=UTF-8";
    private final HikPooledClientManager clientManager;

    public HKClients(HikPooledClientManager clientManager) {
        this.clientManager = clientManager;
    }

    private RestTemplate getTemplate(HkConn conn) {
        return clientManager.getOrCreate(conn.getHost(), conn.getPort(), conn.getUsername(), conn.getPassword(),
                conn.getConnectTimeoutMs(), conn.getReadTimeoutMs());
    }
    private String buildUrl(HkConn conn, String path) {
        if (!path.startsWith("/")) path = "/" + path;
        return conn.baseUrl() + path;
    }
    private <T> HttpEntity<T> entityXml(T body) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Accept", XML_CT);
        headers.set("Content-Type", XML_CT);
        return body == null ? new HttpEntity<>(headers) : new HttpEntity<>(body, headers);
    }

    public String getRaw(HkConn conn, String path) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, path);
        ResponseEntity<String> resp = tpl.exchange(URI.create(url), HttpMethod.GET, entityXml(null), String.class);
        return resp.getBody();
    }

    /** Device Info */
    public DeviceInfo getDeviceInfo(HkConn conn) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/System/deviceInfo");
        try {
            ResponseEntity<DeviceInfo> resp = tpl.exchange(URI.create(url), HttpMethod.GET, entityXml(null), DeviceInfo.class);
            return resp.getBody();
        } catch (HttpClientErrorException e) {
            throw new HKClientException(e.getStatusCode().value(), "getDeviceInfo error: " + e.getResponseBodyAsString());
        }
    }



    /** IPC Channel list */
    public InputProxyChannelList getInputProxyChannels(HkConn conn) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/ContentMgmt/InputProxy/channels");
        try {
            ResponseEntity<InputProxyChannelList> resp = tpl.exchange(URI.create(url), HttpMethod.GET, entityXml(null), InputProxyChannelList.class);
            return resp.getBody();
        } catch (HttpClientErrorException e) {
            throw new HKClientException(e.getStatusCode().value(), "getInputProxyChannels error: " + e.getResponseBodyAsString());
        }
    }

    public boolean deleteInputProxyChannel(HkConn conn, int channelId) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/ContentMgmt/InputProxy/channels/" + channelId);
        try {
            ResponseEntity<String> resp = tpl.exchange(URI.create(url), HttpMethod.DELETE, entityXml(null), String.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException e) {
            throw new HKClientException(e.getStatusCode().value(),
                    "deleteInputProxyChannel error: " + e.getResponseBodyAsString());
        }
    }

    public boolean configureInputProxyChannels(HkConn conn, InputProxyChannelList body) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/ContentMgmt/InputProxy/channels");
        try {
            ResponseEntity<String> resp = tpl.exchange(URI.create(url), HttpMethod.PUT, entityXml(body), String.class);
            return resp.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException e) {
            throw new HKClientException(e.getStatusCode().value(),
                    "configureInputProxyChannels error: " + e.getResponseBodyAsString());
        }
    }






    /** RTSP url from channel+streamType */
    public String buildRtspUrl(HkConn conn, int channelNo, int streamType) {
        int id = channelNo * 100 + streamType;
        return "rtsp://" + conn.getHost() + ":554/ISAPI/Streaming/channels/" + id;
    }

    public NvrDeviceOverview buildOverviewWithRtsp(HkConn conn) {
        // 设备信息 + 通道列表
        DeviceInfo di = getDeviceInfo(conn);
        InputProxyChannelList list = getInputProxyChannels(conn);

        // 一次性拉取所有 StreamingChannel（避免逐条请求/避免猜 1、2、3 路）
        List<StreamingChannelList.StreamingChannel> allSc = listStreamingChannels(conn);

        // 按通道号分组：trackId = chNo*100 + streamNo
        Map<Integer, List<StreamingChannelList.StreamingChannel>> byChannel =
                allSc.stream()
                        .filter(sc -> sc.getId() != null)
                        .collect(Collectors.groupingBy(sc -> {
                            try { return Integer.parseInt(sc.getId()) / 100; } catch (Exception e) { return -1; }
                        }));

        List<NvrDeviceOverview.NvrChannel> chs = new ArrayList<>();
        if (list != null && list.getChannels() != null) {
            for (InputProxyChannelList.InputProxyChannel c : list.getChannels()) {
                Integer chId = c.getId();
                if (chId == null || chId <= 0) continue;

                // 更优雅的 null 处理
                var src = c.getSourceInputPortDescriptor();
                String ip    = Optional.ofNullable(src).map(InputProxyChannelList.SourceInputPortDescriptor::getIpAddress).orElse(null);
                String mfr   = Optional.ofNullable(src).map(InputProxyChannelList.SourceInputPortDescriptor::getManufacturer).orElse(null);
                String model = Optional.ofNullable(src).map(InputProxyChannelList.SourceInputPortDescriptor::getModel).orElse("未知");
                String userName = Optional.ofNullable(src).map(InputProxyChannelList.SourceInputPortDescriptor::getUserName).orElse(null);
                String serialNumber =  Optional.ofNullable(src).map(InputProxyChannelList.SourceInputPortDescriptor::getSerialNumber).orElse(null);

                NvrDeviceOverview.NvrChannel nc = NvrDeviceOverview.NvrChannel.builder()
                        .id(chId).name(c.getName()).online(c.getOnline())
                        .ipAddress(ip).manufacturer(mfr).model(model)
                        .userName(userName).serialNumber(serialNumber)
                        .streams(new ArrayList<>())
                        .build();

                // 本通道实际存在的所有 track
                List<StreamingChannelList.StreamingChannel> scList =
                        byChannel.getOrDefault(chId, Collections.emptyList());
                // 按 streamNo 升序（1/2/3…）方便前端展示
                scList.sort(Comparator.comparingInt(sc -> {
                    try { return Integer.parseInt(sc.getId()) % 100; } catch (Exception e) { return 0; }
                }));

                // 动态回填 main/sub/third（如果不存在则为 null）
                Map<Integer, String> rtspMap = new HashMap<>();

                for (StreamingChannelList.StreamingChannel sc : scList) {
                    int trackId;
                    try { trackId = Integer.parseInt(sc.getId()); } catch (Exception e) { continue; }
                    int streamNo = trackId % 100;

                    // 实时预览 RTSP（给 ZLM/前端直播）
                    String liveRtsp = buildRtspUrl(conn, chId, streamNo);
                    rtspMap.put(streamNo, liveRtsp);

                    // 组装 StreamInfo（只用 StreamingChannel 的字段）
                    var s = new NvrDeviceOverview.NvrChannel.StreamInfo();
                    s.setTrackId(trackId);
                    s.setRtsp(liveRtsp);

                    var v = sc.getVideo();
                    if (v != null) {
                        s.setVideoCodec(v.getVideoCodecType()); // 优先 videoCodecType，兼容 codecType
                        s.setWidth(v.getVideoResolutionWidth());
                        s.setHeight(v.getVideoResolutionHeight());
                        s.setFrameRate(v.getMaxFrameRate());
                        s.setBitRateType(v.getVideoQualityControlType());       // CBR/VBR
                        Integer br = v.getConstantBitRate();                 // constantBitRate / fixedBitRate
                        if (br != null) s.setBitRate(br);
                        s.setProfile(v.getProfile());
                        s.setGop(v.getGovLength());
                    }

                    var a = sc.getAudio();
                    if (a != null) {
                        // 有 Audio 节点：按设备给的 enabled/codec 等填
                        s.setAudioEnabled(a.getEnabled() != null ? a.getEnabled() : Boolean.TRUE);
                        String audioCodec = a.getAudioCompressionType();
                        if ((audioCodec == null || audioCodec.isEmpty()) && a.getCodecType() != null) {
                            audioCodec = a.getCodecType();
                        }
                        s.setAudioCodec(audioCodec);
                    } else {
                        // 无 Audio 节点：视为无音频
                        s.setAudioEnabled(Boolean.FALSE);
                    }

                    nc.getStreams().add(s);
                }

                // 回填常用字段（如不存在则保持 null）
                nc.setRtspMain(rtspMap.get(1));
                nc.setRtspSub(rtspMap.get(2));
                nc.setRtspThird(rtspMap.get(3));
                chs.add(nc);
            }
        }

        return NvrDeviceOverview.builder()
                .deviceName(Optional.ofNullable(di).map(DeviceInfo::getDeviceName).orElse(null))
                .deviceId(Optional.ofNullable(di).map(DeviceInfo::getDeviceID).orElse(null))
                .model(Optional.ofNullable(di).map(DeviceInfo::getModel).orElse(null))
                .firmwareVersion(Optional.ofNullable(di).map(DeviceInfo::getFirmwareVersion).orElse(null))
                .macAddress(Optional.ofNullable(di).map(DeviceInfo::getMacAddress).orElse(null))
                .serialNumber(Optional.ofNullable(di).map(DeviceInfo::getSerialNumber).orElse(null))
                .ipv4Address(Optional.ofNullable(di).map(DeviceInfo::getIpv4Address).orElse(null))
                .channelCount(chs.size())
                .channels(chs)
                .build();
    }



    /** 一次性取所有 StreamingChannel（避免逐条 GET 明细） */
    public java.util.List<StreamingChannelList.StreamingChannel> listStreamingChannels(HkConn conn) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/Streaming/channels");
        try {
            ResponseEntity<StreamingChannelList> resp =
                    tpl.exchange(URI.create(url), HttpMethod.GET, entityXml(null),
                            StreamingChannelList.class);
            StreamingChannelList body = resp.getBody();
            if (body == null || body.getChannels() == null) return java.util.Collections.emptyList();
            return body.getChannels();
        } catch (HttpClientErrorException e) {
            log.warn("listStreamingChannels error: {} {}", e.getStatusCode().value(), e.getResponseBodyAsString());
            return java.util.Collections.emptyList();
        }
    }

}
