package com.xudu.center.zlm.util;

import com.xudu.center.zlm.config.ZlmProperties;
import com.xudu.center.zlm.dto.PlayUrls;

public final class ZlmUrls {
  private ZlmUrls(){}

  private static String trim(String s){
    return (s==null) ? "" : (s.endsWith("/") ? s.substring(0, s.length()-1) : s);
  }

  /** 推回 ZLM 的 RTMP 发布地址：rtmp://host/app/stream */
  public static String internalRtmpPublish(ZlmProperties p, String app, String stream) {
    return trim(p.getInternal().getRtmpBase()) + "/" + app + "/" + stream;
  }

  /** 本机 ZLM 的 RTSP 读取地址：rtsp://host:port/app/stream */
  public static String internalRtspRead(ZlmProperties p, String app, String stream) {
    return trim(p.getInternal().getRtspBase()) + "/" + app + "/" + stream;
  }

  /** 对外播放 URL（HLS/FLV/WHEP/RTSP），你之前已有 */
  public static PlayUrls publicPlayUrls(ZlmProperties p, String app, String stream){
    return PlayUrlBuilder.of(p, app, stream);
  }
}
