// util/PlayUrlBuilder.java
package com.xudu.center.zlm.util;

import com.xudu.center.zlm.config.ZlmProperties;
import com.xudu.center.zlm.dto.PlayUrls;

public class PlayUrlBuilder {
  public static String httpBase(ZlmProperties p) {
    return (p.getPublicHttpBase()!=null && !p.getPublicHttpBase().isBlank())
      ? trimSlash(p.getPublicHttpBase()) : trimSlash(p.getBaseUrl());
  }
  public static String rtspHost(ZlmProperties p) {
    if (p.getPublicRtspHost()!=null && !p.getPublicRtspHost().isBlank()) return p.getPublicRtspHost();
    // 粗略从 baseUrl 提取 host
    String base = p.getBaseUrl();
    String host = base.replaceFirst("^https?://", "");
    int slash = host.indexOf('/');
    if (slash>=0) host = host.substring(0, slash);
    if (!host.contains(":")) host += ":554";
    return host;
  }
  private static String trimSlash(String s){ return s.endsWith("/")?s.substring(0,s.length()-1):s; }

  public static PlayUrls of(ZlmProperties p, String app, String stream){
    String http = httpBase(p);
    String rtsp = "rtsp://" + rtspHost(p) + "/" + app + "/" + stream;
    String hls  = http + "/" + app + "/" + stream + "/hls.m3u8";
    String flv  = http + "/" + app + "/" + stream + ".live.flv";
    String whep = http + "/index/api/whep?app=" + app + "&stream=" + stream;
    return PlayUrls.builder().hls(hls).flv(flv).whep(whep).rtsp(rtsp).build();
  }
}
