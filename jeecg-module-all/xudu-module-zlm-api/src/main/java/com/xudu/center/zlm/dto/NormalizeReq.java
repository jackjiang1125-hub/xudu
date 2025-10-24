package com.xudu.center.zlm.dto;

import lombok.Data;


// dto/NormalizeReq.java
@Data
public class NormalizeReq {

  private String app;

  private String stream;
  private String transApp = "trans";
  private String naming = "auto";
  private String customStream;
  private boolean preferNvenc = false;
  /** 播放模式：HLS_FLV（默认），或 WEBRTC（若你的前端走 WHEP/WHIP 可用） */
  private String playMode = "HLS_FLV";
}
