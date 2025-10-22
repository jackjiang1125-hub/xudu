// dto/PlayUrls.java
package com.xudu.center.zlm.dto;
import lombok.Builder; import lombok.Data;

@Data
@Builder
public class PlayUrls {
  private String hls;    // http(s)://host/app/stream/hls.m3u8
  private String flv;    // http(s)://host/app/stream.live.flv
  private String whep;   // http(s)://host/index/api/whep?app=app&stream=stream
  private String rtsp;   // rtsp://host:port/app/stream
}
