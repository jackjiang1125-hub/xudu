package com.xudu.center.zlm.dto;

import lombok.Data;


@Data
public class PushToMtxReq {
    private String app;       // 源在 ZLM 的 app（如 src）
    private String stream;    // 源在 ZLM 的 stream（如 cam001）
    private String mtxHost;   // 192.168.51.51
    private int mtxRtmpPort = 1935;     // 1935
    private String mtxApp = "live";     // live
    /** 优先 RTMP；也可选择 "rtsp" */
    private String upstreamProto = "rtmp";
}
