package com.xudu.center.zlm.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.xudu.center.zlm.params.Audio;
import com.xudu.center.zlm.params.Video;

public final class CodecClassifier {
  private CodecClassifier(){}

  public static String norm(String s){
    if (s == null) return "";
    String t = s.trim().toUpperCase(java.util.Locale.ROOT);
    return t.replaceAll("[\\s._\\-\\/]", "");
  }

  public static Video classifyVideo(String raw){
    if (raw == null) return Video.UNKNOWN;
    String s = norm(raw);
    if (s.contains("H264") || s.contains("AVC") || s.contains("AVC1")) return Video.H264;
    if (s.contains("H265") || s.contains("HEVC") || s.contains("HEV1") || s.contains("HVC1")) return Video.H265;
    if (s.contains("VP8"))  return Video.VP8;
    if (s.contains("VP9"))  return Video.VP9;
    if (s.contains("AV1") || s.contains("AV01")) return Video.AV1;
    if (s.contains("MPEG4") || s.contains("MP4V") || s.contains("MP4VES")) return Video.MPEG4;
    if (s.contains("MJPEG") || s.equals("JPEG") || s.contains("MOTIONJPEG")) return Video.MJPEG;
    if (s.contains("H263")) return Video.H263;
    if (s.contains("MPEG2")) return Video.MPEG2;
    return Video.UNKNOWN;
  }

  public static Audio classifyAudio(String raw){
    if (raw == null || raw.isBlank()) return Audio.NONE;
    String s = norm(raw);
    if (s.contains("AAC") || s.contains("MPEG4GENERIC") || s.contains("MP4A") || s.contains("LATM")) return Audio.AAC;
    if (s.contains("OPUS")) return Audio.OPUS;
    if (s.contains("PCMA") || s.contains("ALAW") || s.contains("PCMU") || s.contains("ULAW") || s.contains("G711")) return Audio.G711;
    if (s.contains("G726")) return Audio.G726;
    if (s.contains("AMR"))  return Audio.AMR;
    if (s.equals("MP3") || s.contains("MPEGLAYER3") || s.contains("L3")) return Audio.MP3;
    if (s.startsWith("PCM") || s.equals("L16") || s.equals("L8") || s.contains("S16LE") || s.contains("S16BE")) return Audio.PCM;
    if (s.contains("SPEEX")) return Audio.SPEEX;
    return Audio.UNKNOWN;
  }

  // ---------- track 判定与 JSON 取值 ----------
  public static boolean isVideoTrack(JsonNode t) {
    int ct = t.path("codec_type").asInt(-1);
    if (ct == 0) return true;
    if (ct == 1) return false;
    if (t.has("width") || t.has("height") || t.has("fps") || t.has("gop_size")) return true;
    String n = norm(asText(tField(t,"codec_id_name","codec","codec_name")));
    return n.contains("H264")||n.contains("AVC")||n.contains("HEVC")||n.contains("H265")
        ||n.contains("VP8")||n.contains("VP9")||n.contains("AV1")
        ||n.contains("MJPEG")||n.contains("MPEG4")||n.contains("H263");
  }

  public static boolean isAudioTrack(JsonNode t) {
    int ct = t.path("codec_type").asInt(-1);
    if (ct == 1) return true;
    if (ct == 0) return false;
    if (t.has("sample_rate") || t.has("channels") || t.has("sample_bit")) return true;
    String n = norm(asText(tField(t,"codec_id_name","codec","codec_name")));
    return n.contains("AAC")||n.contains("MPEG4GENERIC")||n.contains("MP4A")||n.contains("LATM")
        ||n.contains("OPUS")||n.contains("G711")||n.contains("PCMA")||n.contains("PCMU")
        ||n.contains("G726")||n.contains("AMR")||n.equals("MP3")||n.startsWith("PCM")||n.contains("SPEEX");
  }

  public static JsonNode tField(JsonNode node, String... names) {
    for (String n : names) if (node.has(n)) return node.get(n);
    return node.path(names[0]);
  }
  public static String asText(JsonNode n){ return (n==null||n.isMissingNode())? null : n.asText(null); }
  public static Integer asInt(JsonNode n){ return (n==null||n.isMissingNode()||!n.isNumber())? null : n.asInt(); }
  public static Double asDouble(JsonNode n){
    if (n==null || n.isMissingNode()) return null;
    if (n.isNumber()) return n.asDouble();
    String s = n.asText(null); if (s==null) return null;
    if (s.contains("/")) { try { String[] p=s.split("/"); return Double.parseDouble(p[0])/Double.parseDouble(p[1]); } catch(Exception ignored){} }
    try { return Double.parseDouble(s); } catch(Exception e){ return null; }
  }
}
