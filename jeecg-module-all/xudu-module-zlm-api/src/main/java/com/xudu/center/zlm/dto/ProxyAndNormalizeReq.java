// dto/ProxyAndNormalizeReq.java
package com.xudu.center.zlm.dto;

import lombok.Data;


@Data
public class ProxyAndNormalizeReq {
  /** 如果不填，自动从 url 推断（rtsp/rtmp/http…），默认 rtsp */
  private String schema;

  // 源拉到 ZLM 后的标识
  private String app;     // 例如 "src"
  private String stream;  // 例如 "cam001"

  /** 原始相机地址：rtsp://admin:pwd@ip:554/Streaming/Channels/101 等 */
  private String url;

  private String vhost = "__defaultVhost__";

  // addStreamProxy 相关可选项
  private Integer rtpType = 1;                // 1=UDP, 0=TCP（FFmpeg 里我们已强制 tcp，ZLM这里保留默认）
  private Boolean closeWhenNoConsumer = true; // 无观众时关闭

  // 规范化（转码）相关
  private String transApp = "trans";          // 转码流所在 app
  /** 命名策略：auto(默认)/suffix/custom；custom 时请传 customStream */
  private String naming = "auto";
  private String customStream;
  private boolean preferNvenc = false;        // 有 GPU 时优先 NVENC

  /** 播放模式：HLS_FLV（默认）或 WEBRTC；影响“是否需要转码”的判定 */
  private String playMode = "WEBRTC";

  /** 轮询等待源流就绪的最长时间与间隔（毫秒） */
  private int waitReadyMs = 7000;
  private int pollIntervalMs = 200;
}
