package com.xudu.center.zlm.util;

import java.util.Locale;

public final class SchemaUtil {
  private SchemaUtil(){}
  public static String inferFromUrl(String url) {
    if (url == null) return "rtsp";
    String u = url.trim().toLowerCase(Locale.ROOT);
    if (u.startsWith("rtsp://")) return "rtsp";
    if (u.startsWith("rtmp://")) return "rtmp";
    if (u.startsWith("http://") || u.startsWith("https://")) return "http";
    if (u.startsWith("srt://")) return "srt";
    return "rtsp";
  }
}
