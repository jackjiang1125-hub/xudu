package org.jeecg.modules.iot.utils.zkteco;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * OptionsCommandFactory
 *
 * 目的：生成 BioTime/BioAccess 协议中的 SET OPTIONS（第12章）控制命令。
 * 格式示例：
 *  - C:<cmdId>:SET OPTIONS MachineTZ=+0800
 *  - C:<cmdId>:SET OPTIONS DateTime=829493474
 */
public final class OptionsCommandFactory {
    private static final String SP = " ";
    private static final String HT = "\t";

    private OptionsCommandFactory() {}

    /**
     * 构建 SET OPTIONS 通用命令。
     * @param cmdId 命令序号
     * @param params 参数键值，按协议要求传入
     */
    public static String buildSetOptions(int cmdId, Map<String, ?> params) {
        String tail = params.entrySet().stream()
                .map(e -> e.getKey() + "=" + (e.getValue() == null ? "" : e.getValue()))
                .collect(Collectors.joining(HT));
        return prefix(cmdId) + "SET" + SP + "OPTIONS" + SP + tail;
    }

    /**
     * 构建时区同步命令：MachineTZ。
     * @param cmdId 命令序号
     * @param tzOffset 形如 +0800/-0500 的时区偏移字符串
     */
    public static String buildSyncTimezone(int cmdId, String tzOffset) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("MachineTZ", tzOffset);
        return buildSetOptions(cmdId, params);
    }

    /**
     * 构建时间同步命令：DateTime（秒级时间戳）。
     * @param cmdId 命令序号
     * @param epochSeconds Unix 秒级时间戳
     */
    public static String buildSyncTime(int cmdId, long epochSeconds) {
        Map<String, Object> params = new LinkedHashMap<>();
        params.put("DateTime", toZkSeconds(epochSeconds));
        return buildSetOptions(cmdId, params);
    }

    /**
     * 将 Unix 秒级时间戳转换为 PUSH 协议附录5所要求的旧编码秒数：
     * tt = ((year-2000)*12*31 + ((mon-1)*31) + day-1) * 86400 + (hour*60+min)*60 + sec
     * 年、月、日、时、分、秒取自服务器当前时区（system default）。
     */
    private static long toZkSeconds(long epochSeconds) {
        ZonedDateTime dt = Instant.ofEpochSecond(epochSeconds).atZone(ZoneId.systemDefault());
        long days = ((dt.getYear() - 2000L) * 12L * 31L)
                + ((dt.getMonthValue() - 1L) * 31L)
                + (dt.getDayOfMonth() - 1L);
        long secondsOfDay = (dt.getHour() * 60L + dt.getMinute()) * 60L + dt.getSecond();
        return days * 24L * 60L * 60L + secondsOfDay;
    }

    private static String prefix(int cmdId) { return "C:" + cmdId + ":"; }

    /**
     * 便捷接口：批量构建器
     */
    public interface OptionsCommand {
        String build(int cmdId);
    }

    public static OptionsCommand setOptions(Map<String, ?> params) {
        return (cmdId) -> buildSetOptions(cmdId, params);
    }

    public static OptionsCommand syncTimezone(String tzOffset) {
        return (cmdId) -> buildSyncTimezone(cmdId, tzOffset);
    }

    public static OptionsCommand syncTime(long epochSeconds) {
        return (cmdId) -> buildSyncTime(cmdId, epochSeconds);
    }
}
