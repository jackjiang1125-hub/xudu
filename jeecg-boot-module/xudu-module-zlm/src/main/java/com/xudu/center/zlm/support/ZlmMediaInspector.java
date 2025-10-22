package com.xudu.center.zlm.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xudu.center.zlm.client.ZlmClient;
import com.xudu.center.zlm.dto.CodecInfo;
import com.xudu.center.zlm.model.ZlmResponse;
import com.xudu.center.zlm.params.Audio;
import com.xudu.center.zlm.params.Video;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.xudu.center.zlm.util.CodecClassifier.*;

@Component
@RequiredArgsConstructor
public class ZlmMediaInspector {

  private final ZlmClient client;
  private final ObjectMapper om = new ObjectMapper();

  /** 从 ZLM 读取并解析这路流的 CodecInfo（健壮版） */
  public CodecInfo probeCodec(String app, String stream) {
    ZlmResponse<Object> resp = client.getMediaList(app, stream);
    if (!resp.ok() || resp.getData() == null) {
      throw new RuntimeException("getMediaList failed: " + resp.getMsg());
    }
    JsonNode root = om.valueToTree(resp.getData());
    JsonNode arr  = root.isArray() ? root : root.get("data");
    if (arr == null || !arr.isArray() || arr.size()==0) {
      throw new RuntimeException("stream not found: " + app + "/" + stream);
    }
    JsonNode item   = arr.get(0);
    JsonNode tracks = item.get("tracks");
    if (tracks == null || !tracks.isArray()) throw new RuntimeException("no tracks: " + app + "/" + stream);

    JsonNode v=null, a=null;
    for (JsonNode t : tracks) {
      if (v==null && isVideoTrack(t)) v=t;
      else if (a==null && isAudioTrack(t)) a=t;
    }

    String vRaw = v!=null ? asText(tField(v,"codec_id_name","codec","codec_name")) : null;
    String aRaw = a!=null ? asText(tField(a,"codec_id_name","codec","codec_name")) : null;

    Video vKind = classifyVideo(vRaw);
    Audio aKind = classifyAudio(aRaw);

    Integer width  = v!=null ? asInt(tField(v,"width"))  : null;
    Integer height = v!=null ? asInt(tField(v,"height")) : null;
    Double  fps    = v!=null ? asDouble(tField(v,"fps","frame_rate","r_frame_rate")) : null;

    Integer sample = a!=null ? asInt(tField(a,"sample_rate","samplerate")) : null;
    Integer ch     = a!=null ? asInt(tField(a,"channels","channel"))       : null;

    return new CodecInfo(vRaw, aRaw, vKind, aKind, width, height, fps, sample, ch);
  }

  public boolean streamExists(String app, String stream) {
    try { probeCodec(app, stream); return true; } catch (Exception e) { return false; }
  }
}
