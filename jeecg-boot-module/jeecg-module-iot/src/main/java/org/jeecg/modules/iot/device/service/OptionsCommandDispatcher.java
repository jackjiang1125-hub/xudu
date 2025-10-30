package org.jeecg.modules.iot.device.service;

import lombok.RequiredArgsConstructor;
import org.jeecg.modules.iot.device.entity.IotDeviceCommand;
import org.jeecg.modules.iot.device.seq.CommandSeqService;
import org.jeecg.modules.iot.utils.zkteco.OptionsCommandFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import java.util.ArrayList;
import java.util.List;

/**
 * OptionsCommandDispatcher
 *
 * 目的：对外提供统一的设备控制指令下发入口，负责：
 *  - 生成命令序号（使用 CommandSeqService）
 *  - 构建 SET OPTIONS 命令行（使用 OptionsCommandFactory）
 *  - 调用 IotDeviceCommandService 入队并记录命令下发表
 */
@Service
@RequiredArgsConstructor
public class OptionsCommandDispatcher {

    private final IotDeviceCommandService iotDeviceCommandService;
    private final CommandSeqService commandSeqService;

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
     */
    public List<IotDeviceCommand> dispatchSetOptions(String sn, List<OptionsCommandFactory.OptionsCommand> commands, String operator) {
        if (commands == null || commands.isEmpty()) {
            return List.of();
        }
        int startCmdId = (int) commandSeqService.nextSeqRange(sn, commands.size());
        int id = startCmdId;
        List<String> lines = new ArrayList<>();
        for (OptionsCommandFactory.OptionsCommand c : commands) {
            lines.add(c.build(id++));
        }
        return iotDeviceCommandService.enqueueCommands(sn, lines, operator);
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