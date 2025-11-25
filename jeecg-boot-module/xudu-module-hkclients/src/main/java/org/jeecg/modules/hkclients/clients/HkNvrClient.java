package org.jeecg.modules.hkclients.clients;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.hkclients.AbstractHkClient;
import org.jeecg.modules.hkclients.dto.HkConn;
import org.jeecg.modules.hkclients.dto.NvrDeviceOverview;
import org.jeecg.modules.hkclients.exception.HKClientException;
import org.jeecg.modules.hkclients.http.HikPooledClientManager;
import org.jeecg.modules.hkclients.model.content.InputProxyChannelList;
import org.jeecg.modules.hkclients.model.streaming.StreamingChannelList;
import org.jeecg.modules.hkclients.model.system.DeviceInfo;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class HkNvrClient extends AbstractHkClient {

    public HkNvrClient(HikPooledClientManager clientManager) {
        super(clientManager);
    }


    /** IPC Channel list */
    public InputProxyChannelList getInputProxyChannels(HkConn conn) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/ContentMgmt/InputProxy/channels");
        ResponseEntity<InputProxyChannelList> resp =
                tpl.exchange(URI.create(url), HttpMethod.GET, entityXml(null), InputProxyChannelList.class);
        return resp.getBody();
    }

    public boolean deleteInputProxyChannel(HkConn conn, int channelId) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/ContentMgmt/InputProxy/channels/" + channelId);
        ResponseEntity<String> resp =
                tpl.exchange(URI.create(url), HttpMethod.DELETE, entityXml(null), String.class);
        return resp.getStatusCode().is2xxSuccessful();
    }

    public boolean configureInputProxyChannels(HkConn conn, InputProxyChannelList body) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/ContentMgmt/InputProxy/channels");
        ResponseEntity<String> resp =
                tpl.exchange(URI.create(url), HttpMethod.PUT, entityXml(body), String.class);
        return resp.getStatusCode().is2xxSuccessful();
    }

    /** RTSP url from channel+streamType */
    public String buildRtspUrl(HkConn conn, int channelNo, int streamType) {
        int id = channelNo * 100 + streamType;
        return "rtsp://" + conn.getHost() + ":554/ISAPI/Streaming/channels/" + id;
    }

    /** 一次性取所有 StreamingChannel（避免逐条 GET 明细） */
    public List<StreamingChannelList.StreamingChannel> listStreamingChannels(HkConn conn) {
        RestTemplate tpl = getTemplate(conn);
        String url = buildUrl(conn, "/ISAPI/Streaming/channels");
        try {
            ResponseEntity<StreamingChannelList> resp =
                    tpl.exchange(URI.create(url), HttpMethod.GET, entityXml(null),
                            StreamingChannelList.class);
            StreamingChannelList body = resp.getBody();
            if (body == null || body.getChannels() == null) {
                return Collections.emptyList();
            }
            return body.getChannels();
        } catch (HKClientException e) {
            // 你之前这里是吞掉异常返回空列表，这里保持行为不变
            log.warn("listStreamingChannels error: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    public NvrDeviceOverview buildOverviewWithRtsp(HkConn conn) {
        // 设备信息 + 通道列表
        DeviceInfo di = getDeviceInfo(conn);
        InputProxyChannelList list = getInputProxyChannels(conn);

        // 一次性拉取所有 StreamingChannel
        List<StreamingChannelList.StreamingChannel> allSc = listStreamingChannels(conn);

        // 按通道号分组：trackId = chNo*100 + streamNo
        Map<Integer, List<StreamingChannelList.StreamingChannel>> byChannel =
                allSc.stream()
                        .filter(sc -> sc.getId() != null)
                        .collect(Collectors.groupingBy(sc -> {
                            try {
                                return Integer.parseInt(sc.getId()) / 100;
                            } catch (Exception e) {
                                return -1;
                            }
                        }));

        List<NvrDeviceOverview.NvrChannel> chs = new ArrayList<>();
        if (list != null && list.getChannels() != null) {
            for (InputProxyChannelList.InputProxyChannel c : list.getChannels()) {
                Integer chId = c.getId();
                if (chId == null || chId <= 0) continue;

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

                List<StreamingChannelList.StreamingChannel> scList =
                        byChannel.getOrDefault(chId, Collections.emptyList());
                scList.sort(Comparator.comparingInt(sc -> {
                    try {
                        return Integer.parseInt(sc.getId()) % 100;
                    } catch (Exception e) {
                        return 0;
                    }
                }));

                Map<Integer, String> rtspMap = new HashMap<>();

                for (StreamingChannelList.StreamingChannel sc : scList) {
                    int trackId;
                    try {
                        trackId = Integer.parseInt(sc.getId());
                    } catch (Exception e) {
                        continue;
                    }
                    int streamNo = trackId % 100;

                    String liveRtsp = buildRtspUrl(conn, chId, streamNo);
                    rtspMap.put(streamNo, liveRtsp);

                    var s = new NvrDeviceOverview.NvrChannel.StreamInfo();
                    s.setTrackId(trackId);
                    s.setRtsp(liveRtsp);

                    var v = sc.getVideo();
                    if (v != null) {
                        s.setVideoCodec(v.getVideoCodecType());
                        s.setWidth(v.getVideoResolutionWidth());
                        s.setHeight(v.getVideoResolutionHeight());
                        s.setFrameRate(v.getMaxFrameRate());
                        s.setBitRateType(v.getVideoQualityControlType());
                        Integer br = v.getConstantBitRate();
                        if (br != null) s.setBitRate(br);
                        s.setProfile(v.getProfile());
                        s.setGop(v.getGovLength());
                    }

                    var a = sc.getAudio();
                    if (a != null) {
                        s.setAudioEnabled(a.getEnabled() != null ? a.getEnabled() : Boolean.TRUE);
                        String audioCodec = a.getAudioCompressionType();
                        if ((audioCodec == null || audioCodec.isEmpty()) && a.getCodecType() != null) {
                            audioCodec = a.getCodecType();
                        }
                        s.setAudioCodec(audioCodec);
                    } else {
                        s.setAudioEnabled(Boolean.FALSE);
                    }

                    nc.getStreams().add(s);
                }

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
}
