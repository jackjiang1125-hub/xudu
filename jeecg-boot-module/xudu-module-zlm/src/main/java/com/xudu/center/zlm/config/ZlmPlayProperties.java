// config/ZlmPlayProperties.java
package com.xudu.center.zlm.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "zlm.play") // 在 application-*.yml 里配置
public class ZlmPlayProperties {
  /** 友好时提升策略：reproxy | ffcopy | direct */
  private String promoteStrategy = "reproxy";
  /** 统一对外 app，默认 trans；如果 direct 则返回 src */
  private String publicApp = "trans";
  /** 内部源 app */
  private String srcApp = "src";
  /** 等待就绪最长毫秒数 */
  private int waitReadyMs = 7000;
  private int pollIntervalMs = 200;
  /** HLS：移动端是否偏好 fMP4 */
  private boolean mobileUseHlsFmp4 = true;
  /** FFmpeg copy 模板 key（-c copy -f flv） */
  private String ffmpegCopyKey = "ffmpeg.cmd_copy_rtmp";
}
