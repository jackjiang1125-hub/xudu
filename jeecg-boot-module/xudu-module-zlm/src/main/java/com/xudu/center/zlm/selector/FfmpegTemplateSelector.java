package com.xudu.center.zlm.selector;

import com.xudu.center.zlm.config.ZlmProperties;
import com.xudu.center.zlm.dto.CodecInfo;
import com.xudu.center.zlm.params.Audio;
import com.xudu.center.zlm.params.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FfmpegTemplateSelector {

  private final ZlmProperties props;

  public String select(CodecInfo ci, boolean preferNvenc){
    boolean v264 = ci.videoKind() == Video.H264;
    boolean v265 = ci.videoKind() == Video.H265;
    boolean aac  = (ci.audioKind() == Audio.AAC || ci.audioKind() == Audio.NONE);

    var k = props.getFfmpegKeys();
    if (v264 && !aac) return nvl(k.getAacOnlyRtmp(),        "ffmpeg.cmd_aac_only_rtmp");
    if (v265 &&  aac) return preferNvenc
            ? nvl(k.getH264OnlyRtmpNvenc(), "ffmpeg.cmd_h264_only_rtmp_nvenc")
            : nvl(k.getH264OnlyRtmp(),      "ffmpeg.cmd_h264_only_rtmp");
    return preferNvenc
            ? nvl(k.getH264AacRtmpNvenc(),  "ffmpeg.cmd_h264_aac_rtmp_nvenc")
            : nvl(k.getH264AacRtmp(),       "ffmpeg.cmd_h264_aac_rtmp");
  }

  private static String nvl(String v, String def){ return (v==null || v.isBlank()) ? def : v; }
}
