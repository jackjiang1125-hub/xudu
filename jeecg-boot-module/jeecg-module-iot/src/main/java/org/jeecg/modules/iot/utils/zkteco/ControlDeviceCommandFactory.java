package org.jeecg.modules.iot.utils.zkteco;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * ControlDeviceCommandFactory
 *
 * 目的：生成 BioTime/BioAccess 协议（第12章 CONTROL DEVICE 控制类命令）所需的控制指令串。
 * 与现有 AccessCommandFactory 保持一致的前缀与分隔符格式：
 *  - 前缀：C:<cmdId>:
 *  - 分隔：参数键值对使用制表符连接（\t），命令段使用空格连接。
 *
 * 示例：
 *  C:501:CONTROL DEVICE Door=1\tRelay=1\tPulse=3
 *
 * 说明：
 *  - 具体键名与取值以设备固件的协议文档为准；本工厂类提供通用 Map 组装能力，并内置 openDoor 的便捷构造。
 *  - 设备上报回执时 CMD 字段会为 "CONTROL DEVICE"，ID 会匹配 cmdId；入库与回执处理与现有流程一致。
 */
public final class ControlDeviceCommandFactory {
    private static final String SP = " ";
    private static final String HT = "\t"; // Horizontal Tab

    private ControlDeviceCommandFactory() {}

    /**
     * 构建远程开门控制命令。
     * @param cmdId 命令序号（用于设备回执中的 ID 字段）
     * @param doorId 门编号（从 1 开始）
     * @param pulseSeconds 保持继电器动作的秒数（可空，设备默认值生效）
     */
    public static String buildOpenDoor(int cmdId, int doorId, Integer pulseSeconds) {
        String payload = encodePayload(doorId, true, pulseSeconds);
        return prefix(cmdId) + "CONTROL" + SP + "DEVICE" + SP + payload;
    }

    /**
     * 通用构造：CONTROL DEVICE + 任意键值参数。
     * @param cmdId 命令序号
     * @param params 键值参数（按协议要求传入）
     */
    public static String buildControlDevice(int cmdId, Map<String, ?> params) {
        String tail = params.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(HT));
        return prefix(cmdId) + "CONTROL" + SP + "DEVICE" + SP + tail;
    }

    private static String prefix(int cmdId) { return "C:" + cmdId + ":"; }

    /**
     * 通用控制命令接口：适用于批量组装时按序生成命令行。
     */
    public interface ControlCommand {
        String build(int cmdId);
    }

    /**
     * 便捷封装：远程开门命令，供批量接口使用。
     */
    public static ControlCommand openDoor(int doorId, Integer pulseSeconds) {
        return (cmdId) -> buildOpenDoor(cmdId, doorId, pulseSeconds);
    }

    /**
     * 便捷封装：原始键值参数命令，供批量接口使用。
     */
    public static ControlCommand raw(Map<String, ?> params) {
        return (cmdId) -> buildControlDevice(cmdId, params);
    }
    
    /**
     * 构建远程关门控制命令（数值编码）。示例：C:<cmdId>:CONTROL DEVICE 01010100
     */
    public static String buildCloseDoor(int cmdId, int doorId) {
        String payload = encodePayload(doorId, false, 0);
        return prefix(cmdId) + "CONTROL" + SP + "DEVICE" + SP + payload;
    }

    /**
     * 便捷封装：远程关门命令
     */
    public static ControlCommand closeDoor(int doorId) {
        return (cmdId) -> buildCloseDoor(cmdId, doorId);
    }

    /**
     * 构建取消报警命令。示例：C:<cmdId>:CONTROL DEVICE 02010000
     */
    public static String buildCancelAlarm(int cmdId, int doorId) {
        // 按协议标准固定编码：02010000（取消报警）
        String payload = "02010000";
        return prefix(cmdId) + "CONTROL" + SP + "DEVICE" + SP + payload;
    }

    /**
     * 构建远程常开命令（保持开门，脉冲为 ff）。示例：C:<cmdId>:CONTROL DEVICE 010101ff
     */
    public static String buildHoldOpen(int cmdId, int doorId) {
        String payload = encodePayloadFlexible(doorId, "01", "ff");
        return prefix(cmdId) + "CONTROL" + SP + "DEVICE" + SP + payload;
    }

    /**
     * 构建远程锁定命令。示例：C:<cmdId>:CONTROL DEVICE 06010100
     */
    public static String buildLockDoor(int cmdId, int doorId) {
        // 按协议标准固定编码：06010100（远程锁定）
        String payload = "06010100";
        return prefix(cmdId) + "CONTROL" + SP + "DEVICE" + SP + payload;
    }

    /**
     * 构建远程解锁命令。示例：C:<cmdId>:CONTROL DEVICE 06010000
     */
    public static String buildUnlockDoor(int cmdId, int doorId) {
        // 按协议标准固定编码：06010000（远程解锁）
        String payload = "06010000";
        return prefix(cmdId) + "CONTROL" + SP + "DEVICE" + SP + payload;
    }

    /**
     * 便捷封装：取消报警命令
     */
    public static ControlCommand cancelAlarm(int doorId) {
        return (cmdId) -> buildCancelAlarm(cmdId, doorId);
    }

    /**
     * 便捷封装：远程常开命令
     */
    public static ControlCommand holdOpen(int doorId) {
        return (cmdId) -> buildHoldOpen(cmdId, doorId);
    }

    /**
     * 便捷封装：远程锁定命令
     */
    public static ControlCommand lockDoor(int doorId) {
        return (cmdId) -> buildLockDoor(cmdId, doorId);
    }

    /**
     * 便捷封装：远程解锁命令
     */
    public static ControlCommand unlockDoor(int doorId) {
        return (cmdId) -> buildUnlockDoor(cmdId, doorId);
    }

    public static String buildEnableTodayAlwaysOpen(int cmdId, int doorId) {
        String payload = "04010100";
        return prefix(cmdId) + "CONTROL" + SP + "DEVICE" + SP + payload;
    }

    public static String buildDisableTodayAlwaysOpen(int cmdId, int doorId) {
        String payload = "04010000";
        return prefix(cmdId) + "CONTROL" + SP + "DEVICE" + SP + payload;
    }

    public static ControlCommand enableTodayAlwaysOpen(int doorId) {
        return (cmdId) -> buildEnableTodayAlwaysOpen(cmdId, doorId);
    }

    public static ControlCommand disableTodayAlwaysOpen(int doorId) {
        return (cmdId) -> buildDisableTodayAlwaysOpen(cmdId, doorId);
    }

    /**
     * 编码载荷为两位数序列：[DoorId][RelayId][ActionCode][PulseSeconds]
     * DoorId>=1，RelayId固定01，ActionCode固定01，关门时Pulse=00。
     */
    private static String encodePayload(int doorId, boolean open, Integer pulseSeconds) {
        int d = doorId <= 0 ? 1 : Math.min(doorId, 99);
        int relay = 1;
        int action = 1;
        int pulse = open ? (pulseSeconds == null ? 5 : Math.max(0, Math.min(pulseSeconds, 99))) : 0;
        return String.format("%02d%02d%02d%02d", d, relay, action, pulse);
    }

    /**
     * 灵活编码载荷，允许指定动作码与脉冲为十六进制 "ff"。
     * @param doorId 门编号（1..99）
     * @param action 两位动作码（例如 "00" 解锁、"01" 开/锁定）
     * @param pulse 两位脉冲码（例如 "00"、"05"、或 "ff" 表示常开）
     */
    private static String encodePayloadFlexible(int doorId, String action, String pulse) {
        int d = doorId <= 0 ? 1 : Math.min(doorId, 99);
        String d2 = String.format("%02d", d);
        String relay2 = "01";
        String a2 = action == null ? "01" : action.toLowerCase();
        String p2;
        if (pulse == null) {
            p2 = "00";
        } else if ("ff".equalsIgnoreCase(pulse)) {
            p2 = "ff";
        } else {
            // 数值两位十进制
            int p = Math.max(0, Math.min(Integer.parseInt(pulse), 99));
            p2 = String.format("%02d", p);
        }
        return d2 + relay2 + a2 + p2;
    }
}
