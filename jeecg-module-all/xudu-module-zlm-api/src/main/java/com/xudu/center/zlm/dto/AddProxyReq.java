package com.xudu.center.zlm.dto;

import lombok.Data;

@Data
public class AddProxyReq {
    private String schema; // rtsp/rtmp/hls...
    private String app;
    private String stream;
    private String url;    // 源 URL
    private Integer rtpType = 1;     // 1=UDP, 0=TCP; 海康建议 tcp: rtsp_transport 参数在 FFmpeg 里
    private Boolean closeWhenNoConsumer = true;
}

