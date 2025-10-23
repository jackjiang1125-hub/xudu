// controller/ZlmHookController.java
package com.xudu.center.zlm.hook;

import com.xudu.center.zlm.config.ZlmPlayProperties;
import com.xudu.center.zlm.service.OnDemandPlayService;
import lombok.RequiredArgsConstructor;
import org.jeecg.config.shiro.IgnoreAuth;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/index/hook")
@RequiredArgsConstructor
public class ZlmHookController {

  private final OnDemandPlayService onDemand;
  private final ZlmPlayProperties playProps;

  /** 有人请求还不存在的流：触发按需拉流 */
  @IgnoreAuth
  @PostMapping("/on_stream_not_found")
  public Map<String,Object> onStreamNotFound(@RequestBody Map<String,Object> in){
    String app    = (String) in.getOrDefault("app", "");
    String stream = (String) in.getOrDefault("stream", "");
    // 异步触发准备，hook 不需阻塞
    new Thread(() -> {
      try {
          onDemand.prepareByAppStream(app, stream);
      } catch (Throwable ignore){
      }
    }, "prepare-"+app+"-"+stream).start();
    Map<String,Object> out = new HashMap<>();
    out.put("code", 0);  // 允许继续
    out.put("msg", "ok");
    return out;
  }

  /** 按流覆盖协议（可选）：例如移动端 app 统一走 HLS-fMP4 */
  @IgnoreAuth
  @PostMapping("/on_publish")
  public Map<String,Object> onPublish(@RequestBody Map<String,Object> in){
    String app = (String) in.getOrDefault("app","");
    Map<String,Object> out = new HashMap<>();
    out.put("code", 0);
    if ("mobile".equalsIgnoreCase(app) && playProps.isMobileUseHlsFmp4()) {
      out.put("enable_hls", true);
      out.put("enable_hls_fmp4", true);
      out.put("enable_rtmp", false);
    }
    return out;
  }

  /** 无人观看（可选）：若是转码流，可在这里停掉 FFmpeg 任务 */
  @IgnoreAuth
  @PostMapping("/on_stream_none_reader")
  public Map<String,Object> onNoneReader(@RequestBody Map<String,Object> in){
    String app    = (String) in.getOrDefault("app", "");
    String stream = (String) in.getOrDefault("stream", "");
    // 这里你可以查是否为转码流（比如 app=trans），然后调用 client.delFFmpegSource(key)
    // 示例仅返回默认行为：允许自动关闭
    Map<String,Object> out = new HashMap<>();
    out.put("code", 0);
    out.put("close", true); // 允许关闭（对代理拉流生效）
    return out;
  }
}
