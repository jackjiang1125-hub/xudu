// dto/NormalizeResult.java
package com.xudu.center.zlm.dto;
import lombok.Builder; import lombok.Data;

@Data
@Builder
public class NormalizeResult {
  /** 是否无需转码（直接用源流） */
  private boolean browserFriendly;
  /** 实际给应用层的 app/stream（可能=源，也可能=转码后的） */
  private String app;
  private String stream;
  /** 如果发生了转码：选用的 ffmpeg 模板 key 与 ZLM 返回的任务 key */
  private String ffmpegCmdKey;
  private String ffmpegTaskKey;
  /** 播放 URL（应用层直接用） */
  private PlayUrls urls;
  /** 源的编解码（便于前端展示） */
  private String videoCodec;
  private String audioCodec;
}
