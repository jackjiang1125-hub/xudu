package org.jeecg.modules.iot.device.service;

import lombok.RequiredArgsConstructor;
import org.jeecg.modules.iot.device.entity.IotDeviceCommand;
import org.jeecg.modules.iot.device.seq.CommandSeqService;
import org.jeecg.modules.iot.utils.zkteco.ControlDeviceCommandFactory;
import org.jeecg.modules.iot.utils.zkteco.OptionsCommandFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * ControlDeviceCommandDispatcher
 *
 * 目的：对外提供统一的设备控制指令下发入口，负责：
 *  - 生成命令序号（使用 CommandSeqService）
 *  - 构建 CONTROL DEVICE 命令行（使用 ControlDeviceCommandFactory）
 *  - 调用 IotDeviceCommandService 入队并记录命令下发表
 */
@Service
@RequiredArgsConstructor
public class ControlDeviceCommandDispatcher {

    private final IotDeviceCommandService iotDeviceCommandService;
    private final CommandSeqService commandSeqService;

    /**
     * 远程开门（单条命令）。
     * @param sn 设备序列号
     * @param doorId 门编号，从 1 开始
     * @param pulseSeconds 继电器保持秒数，可空
     * @param operator 操作人用户名
     */
    public List<IotDeviceCommand> openDoor(String sn, int doorId, Integer pulseSeconds, String operator) {
        int startCmdId = (int) commandSeqService.nextSeq(sn);
        String cmd = ControlDeviceCommandFactory.buildOpenDoor(startCmdId, doorId, pulseSeconds);
        return iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), operator);
    }

    /**
     * 通用批量控制指令下发：可扩展为多条不同的 CONTROL DEVICE 命令。
     * @param sn 设备序列号
     * @param commands 控制命令构造器列表
     * @param operator 操作人用户名
     */
    public List<IotDeviceCommand> dispatch(String sn, List<ControlDeviceCommandFactory.ControlCommand> commands, String operator) {
        if (commands == null || commands.isEmpty()) {
            return List.of();
        }
        int startCmdId = (int) commandSeqService.nextSeqRange(sn, commands.size());
        int id = startCmdId;
        List<String> lines = new ArrayList<>();
        for (ControlDeviceCommandFactory.ControlCommand c : commands) {
            lines.add(c.build(id++));
        }
        return iotDeviceCommandService.enqueueCommands(sn, lines, operator);
    }

    /**
     * 同步设备时区：生成 SET OPTIONS MachineTZ=+0800
     */
    public List<IotDeviceCommand> syncTimezone(String sn, String tzOffset, String operator) {
        int cmdId = (int) commandSeqService.nextSeq(sn);
        String offset = (tzOffset == null || tzOffset.isBlank()) ? currentTzOffsetFormatted() : tzOffset;
        String cmd = OptionsCommandFactory.buildSyncTimezone(cmdId, offset);
        return iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), operator);
    }

    /**
     * 同步设备时间：生成 SET OPTIONS DateTime=<epochSeconds>
     */
    public List<IotDeviceCommand> syncTime(String sn, Long epochSeconds, String operator) {
        int cmdId = (int) commandSeqService.nextSeq(sn);
        long seconds = (epochSeconds != null) ? epochSeconds : Instant.now().getEpochSecond();
        String cmd = OptionsCommandFactory.buildSyncTime(cmdId, seconds);
        return iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), operator);
    }

    /**
     * 批量下发 SET OPTIONS 命令。
     * 若一次性下发多组参数，则合并为一条命令，并用英文逗号分隔键值。
     * 示例：SET OPTIONS Door1CloseAndLock=0,WiegandIDIn=1,Door1Drivertime=5,...
     */
    public List<IotDeviceCommand> dispatchSetOptions(String sn, List<OptionsCommandFactory.OptionsCommand> commands, String operator) {
        if (commands == null || commands.isEmpty()) {
            return List.of();
        }
        int cmdId = (int) commandSeqService.nextSeq(sn);
        // 收集所有键值对，最终用逗号拼接
        List<String> kvAll = new ArrayList<>();
        for (OptionsCommandFactory.OptionsCommand c : commands) {
            String line = c.build(cmdId); // 形如：C:<cmdId>:SET OPTIONS k=v\t...
            int idx = line.indexOf("SET OPTIONS ");
            String tail = idx >= 0 ? line.substring(idx + "SET OPTIONS ".length()) : line;
            String[] parts = tail.split("\\t"); // 将制表符分隔的键值对拆分
            for (String p : parts) {
                if (p != null && !p.isBlank()) {
                    kvAll.add(p.trim());
                }
            }
        }
        String merged = "C:" + cmdId + ":SET OPTIONS " + String.join(",", kvAll);
        return iotDeviceCommandService.enqueueCommands(sn, List.of(merged), operator);
    }

    private String currentTzOffsetFormatted() {
        ZoneOffset offset = ZonedDateTime.now(ZoneId.systemDefault()).getOffset();
        int totalSeconds = offset.getTotalSeconds();
        int abs = Math.abs(totalSeconds);
        int hours = abs / 3600;
        int minutes = (abs % 3600) / 60;
        String sign = totalSeconds >= 0 ? "+" : "-";
        return sign + String.format("%02d%02d", hours, minutes);
    }
}