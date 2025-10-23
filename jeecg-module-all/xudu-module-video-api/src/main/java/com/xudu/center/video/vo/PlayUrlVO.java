// PlayUrlVO.java  (给前端的播放地址封装)
package com.xudu.center.video.vo;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayUrlVO {
    private String app;            // live
    private String stream;         // cam001
    private String hls;            // http://host/live/cam001/hls.m3u8
    private String flv;            // http://host/live/cam001.live.flv
    private String webrtcApi;      // http://host/index/api/webrtc?app=live&stream=cam001&type=play
}
