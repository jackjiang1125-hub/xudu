package org.jeecg.modules.iot.utils.zkteco;

import java.util.LinkedHashMap;
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
        Map<String, Object> params = new LinkedHashMap<>();
        // 约定键名与设备状态上报中的字段保持一致（Door/Relay），便于后续对齐与扩展
        params.put("Door", doorId);
        params.put("Relay", 1); // 1 表示触发开锁继电器
        if (pulseSeconds != null) {
            params.put("Pulse", pulseSeconds);
        }
        return buildControlDevice(cmdId, params);
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
}
