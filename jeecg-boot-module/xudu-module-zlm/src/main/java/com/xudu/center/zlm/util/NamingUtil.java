package com.xudu.center.zlm.util;

import com.xudu.center.zlm.dto.CodecInfo;
import com.xudu.center.zlm.params.Audio;
import com.xudu.center.zlm.params.Video;

import java.util.Locale;

public final class NamingUtil {
  private NamingUtil(){}
  public static String chooseOutStreamName(String naming, String custom, String srcStream, CodecInfo ci) {
    String mode = (naming == null) ? "auto" : naming.toLowerCase(Locale.ROOT);
    if ("custom".equals(mode) && custom != null && !custom.isBlank()) return custom;
    String suffix = suffixOf(ci);
    return srcStream + suffix;
  }

  public static String suffixOf(CodecInfo ci) {
    boolean v264 = ci.videoKind() == Video.H264;
    boolean aac  = (ci.audioKind() == Audio.AAC || ci.audioKind() == Audio.NONE);
    if (!v264 && !aac) return "_h264_aac";
    if (!v264) return "_h264";
    if (!aac)  return "_aac";
    return "_norm";
  }
}
