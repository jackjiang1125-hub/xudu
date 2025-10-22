package com.xudu.center.video.camera.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import org.jeecg.common.system.base.entity.JeecgEntity;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Data
@TableName("ipc_media_gateway")
@JsonIgnoreProperties(ignoreUnknown = true) // 未映射字段不报错
public class MediaGateway extends JeecgEntity implements Serializable {



    /** 可手动编辑的名字，非上报字段 */
    private String name;

    // mediaServerId: 同时接受 general.mediaServerId & mediaServerId
    @JsonAlias({"general.mediaServerId", "mediaServerId"})
    @TableField("media_server_id")
    private String mediaServerId;

    // ---------------- API ----------------
    @JsonProperty("api.apiDebug")
    @TableField("api_debug")
    private Integer apiApiDebug;

    @JsonProperty("api.secret")
    @TableField("api_secret")
    private String apiSecret;

    @JsonProperty("api.defaultSnap")
    @TableField("api_default_snap")
    private String apiDefaultSnap;

    @JsonProperty("api.snapRoot")
    @TableField("api_snap_root")
    private String apiSnapRoot;

    // -------------- GENERAL 常用 ----------
    @JsonProperty("general.enableVhost")
    @TableField("general_enable_vhost")
    private Integer generalEnableVhost;

    @JsonProperty("general.flowThreshold")
    @TableField("general_flow_threshold")
    private Integer generalFlowThreshold;

    @JsonProperty("general.maxStreamWaitMS")
    @TableField("general_max_stream_wait_ms")
    private Integer generalMaxStreamWaitMs;

    // ---------------- FFmpeg -------------
    @JsonProperty("ffmpeg.bin")
    @TableField("ffmpeg_bin")
    private String ffmpegBin;

    @JsonProperty("ffmpeg.cmd")
    @TableField("ffmpeg_cmd")
    private String ffmpegCmd;

    @JsonProperty("ffmpeg.log")
    @TableField("ffmpeg_log")
    private String ffmpegLog;

    @JsonProperty("ffmpeg.restart_sec")
    @TableField("ffmpeg_restart_sec")
    private Integer ffmpegRestartSec;

    @JsonProperty("ffmpeg.snap")
    @TableField("ffmpeg_snap")
    private String ffmpegSnap;

    // ------------- 端口类 -----------------
    @JsonProperty("http.port")
    @TableField("http_port")
    private Integer httpPort;

    @JsonProperty("http.sslport")
    @TableField("http_sslport")
    private Integer httpSslport;

    @JsonProperty("rtmp.port")
    @TableField("rtmp_port")
    private Integer rtmpPort;

    @JsonProperty("rtsp.port")
    @TableField("rtsp_port")
    private Integer rtspPort;

    @JsonProperty("rtc.port")
    @TableField("rtc_port")
    private Integer rtcPort;

    @JsonProperty("rtc.tcpPort")
    @TableField("rtc_tcp_port")
    private Integer rtcTcpPort;

    @JsonProperty("rtc.signalingPort")
    @TableField("rtc_signaling_port")
    private Integer rtcSignalingPort;

    @JsonProperty("rtc.signalingSslPort")
    @TableField("rtc_signaling_ssl_port")
    private Integer rtcSignalingSslPort;

    @JsonProperty("rtc.externIP")
    @TableField("rtc_extern_ip")
    private String rtcExternIP;

    // 端口区间以字符串保存，前端更直观
    @JsonAlias({"rtc.portRange","rtc.port_range"})
    @TableField("rtc_port_range")
    private String rtcPortRange;

    @JsonProperty("rtc.timeoutSec")
    @TableField("rtc_timeout_sec")
    private Integer rtcTimeoutSec;

    // ---------------- Hook 关键项 ----------
    @JsonProperty("hook.enable")
    @TableField("hook_enable")
    private Integer hookEnable;

    @JsonProperty("hook.alive_interval")
    @TableField("hook_alive_interval")
    private java.math.BigDecimal hookAliveInterval;

    @JsonProperty("hook.on_server_started")
    @TableField("hook_on_server_started")
    private String hookOnServerStarted;

    @JsonProperty("hook.retry")
    @TableField("hook_retry")
    private Integer hookRetry;

    @JsonProperty("hook.retry_delay")
    @TableField("hook_retry_delay")
    private java.math.BigDecimal hookRetryDelay;

    // 注意：rtc.timeoutSec 与 hook.timeoutSec 是不同的，分开存
    @JsonProperty("hook.timeoutSec")
    @TableField("hook_timeout_sec")
    private Integer hookTimeoutSec;

    // -------------- Protocol/HLS ----------
    @JsonProperty("protocol.enable_hls")
    @TableField("enable_hls")
    private Integer enableHls;

    @JsonProperty("protocol.enable_fmp4")
    @TableField("enable_fmp4")
    private Integer enableFmp4;

    @JsonProperty("protocol.enable_rtmp")
    @TableField("enable_rtmp")
    private Integer enableRtmp;

    @JsonProperty("protocol.enable_rtsp")
    @TableField("enable_rtsp")
    private Integer enableRtsp;

    @JsonProperty("protocol.enable_ts")
    @TableField("enable_ts")
    private Integer enableTs;

    @JsonProperty("protocol.hls_demand")
    @TableField("hls_demand")
    private Integer hlsDemand;

    @JsonProperty("protocol.fmp4_demand")
    @TableField("fmp4_demand")
    private Integer fmp4Demand;

    @JsonProperty("protocol.rtmp_demand")
    @TableField("rtmp_demand")
    private Integer rtmpDemand;

    @JsonProperty("protocol.rtsp_demand")
    @TableField("rtsp_demand")
    private Integer rtspDemand;

    @JsonProperty("protocol.ts_demand")
    @TableField("ts_demand")
    private Integer tsDemand;

    @JsonProperty("protocol.hls_save_path")
    @TableField("hls_save_path")
    private String hlsSavePath;

    @JsonProperty("hls.segDur")
    @TableField("hls_seg_dur")
    private Integer hlsSegDur;

    @JsonProperty("hls.segNum")
    @TableField("hls_seg_num")
    private Integer hlsSegNum;

    @JsonProperty("hls.deleteDelaySec")
    @TableField("hls_delete_delay_sec")
    private Integer hlsDeleteDelaySec;

    // -------------- 录制常用 --------------
    @JsonProperty("record.appName")
    @TableField("record_app_name")
    private String recordAppName;

    @JsonProperty("record.enableFmp4")
    @TableField("record_enable_fmp4")
    private Integer recordEnableFmp4;

    // -------- 其余未映射项聚合到 extraJson --------
    @TableField("extra_json")
    private String extraJson;

    // 接住任意未知字段 -> 打进一个 Map，最后序列化到 extraJson
    @JsonIgnore
    @TableField(exist = false)
    private Map<String,Object> _extraCollector = new HashMap<>();

    @JsonAnySetter
    public void collectExtra(String key, Object value){
        // 仅收集“带点的、且未被以上字段消费”的条目会进来
        _extraCollector.put(key, value);
    }
}
