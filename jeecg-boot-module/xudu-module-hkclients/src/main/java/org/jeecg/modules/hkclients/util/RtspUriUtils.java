package org.jeecg.modules.hkclients.util;

import org.jeecg.modules.hkclients.dto.HkConn;

public class RtspUriUtils {
// ========= 1A) 以 LocalDateTime 构造“可播放”的 RTSP 回放地址 =========
    /**
     * 构造可直接用于 VLC/ZLM 的 RTSP 回放地址：
     * - 使用 /ISAPI/Streaming/tracks/{trackId}
     * - 端口统一为 554（可用 rtspPortOverride 覆盖）
     * - 自动拼接用户名密码 rtsp://user:pass@host:port/...
     * - 时间按 YYYYMMDD'T'HHmmssZ 字面格式（多数机型按设备本地时间解析）
     */
    public static String buildPlaybackRtsp(HkConn conn, int trackId,
                                    java.time.LocalDateTime startLocal,
                                    java.time.LocalDateTime endLocal) {
        return buildPlaybackRtsp(conn, trackId, startLocal, endLocal, null);
    }

    public static String buildPlaybackRtsp(HkConn conn, int trackId,
                                    java.time.LocalDateTime startLocal,
                                    java.time.LocalDateTime endLocal,
                                    Integer rtspPortOverride) {
        java.time.format.DateTimeFormatter F = java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        String start = startLocal.format(F);
        String end   = endLocal.format(F);
        int rtspPort = (rtspPortOverride != null ? rtspPortOverride : 554);
        // 直接产出“可播放”URL（含账号密码 + /ISAPI 路径）
        return String.format(
                "rtsp://%s:%s@%s:%d/ISAPI/Streaming/tracks/%d?starttime=%s&endtime=%s",
                conn.getUsername(), conn.getPassword(),
                conn.getHost(), rtspPort, trackId, start, end
        );
    }

// ========= 1B) 以 "yyyyMMdd'T'HHmmssZ" 字符串构造“可播放”的 RTSP 回放地址 =========
    /** 字符串版（时间如 20251025T213215Z），端口默认 554 */
    public static String buildPlaybackRtsp(HkConn conn, int trackId,
                                    String startUtcZ, String endUtcZ) {
        return buildPlaybackRtsp(conn, trackId, startUtcZ, endUtcZ, null);
    }

    /** 字符串版（时间如 20251025T213215Z），可指定 RTSP 端口 */
    public static String buildPlaybackRtsp(HkConn conn, int trackId,
                                    String startUtcZ, String endUtcZ,
                                    Integer rtspPortOverride) {
        if (conn == null) throw new IllegalArgumentException("conn is null");
        if (startUtcZ == null || endUtcZ == null)
            throw new IllegalArgumentException("startUtcZ/endUtcZ is null");

        String s = startUtcZ.trim();
        String e = endUtcZ.trim();
        if (!s.endsWith("Z")) s = s + "Z";
        if (!e.endsWith("Z")) e = e + "Z";


        int rtspPort = (rtspPortOverride != null ? rtspPortOverride : 554);
        return String.format(
                "rtsp://%s:%s@%s:%d/ISAPI/Streaming/tracks/%d?starttime=%s&endtime=%s",
                conn.getUsername(), conn.getPassword(),
                conn.getHost(), rtspPort, trackId, s, e
        );
    }

// ========= 1C) 可选重载：把 /ISAPI/ContentMgmt/search 返回的 playbackURI 直接“构造成可播放” =========
    /**
     * 传入设备返回的 playbackURI（可能是 rtsp://host:80/Streaming/tracks/.../?...）
     * 统一成：rtsp://user:pass@host:rtspPort/ISAPI/Streaming/tracks/...?... 便于直接播放。
     */
    public String buildPlaybackRtsp(HkConn conn, String playbackUriFromSearch, Integer rtspPortOverride) {
        if (playbackUriFromSearch == null || !playbackUriFromSearch.startsWith("rtsp://"))
            throw new IllegalArgumentException("playbackUriFromSearch must start with rtsp://");

        String fixed = playbackUriFromSearch;

        // 统一路径为 /ISAPI/Streaming/tracks
        if (fixed.contains("/Streaming/tracks/") && !fixed.contains("/ISAPI/Streaming/tracks/")) {
            fixed = fixed.replace("/Streaming/tracks/", "/ISAPI/Streaming/tracks/");
        }

        // 修正常见的 :80 -> RTSP 端口
        int rtspPort = (rtspPortOverride != null ? rtspPortOverride : 554);
        fixed = fixed.replace(":80/", ":" + rtspPort + "/");

        // 补鉴权 rtsp://user:pass@...
        String after = fixed.substring("rtsp://".length());
        if (!after.contains("@")) {
            fixed = "rtsp://" + conn.getUsername() + ":" + conn.getPassword() + "@" + after;
        }

        // 规整 "/?" -> "?"
        int i = fixed.indexOf("/?");
        if (i > 0) fixed = fixed.substring(0, i) + "?" + fixed.substring(i + 2);

        return fixed;
    }

}
