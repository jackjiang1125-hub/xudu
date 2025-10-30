package org.jeecg.modules.iot.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.iot.device.cache.AccDeviceRedisCache;
import org.jeecg.modules.iot.device.entity.IotDeviceCommand;
import org.jeecg.modules.iot.device.enums.IotDeviceCommandStatus;
import org.jeecg.modules.iot.device.mapper.IotDeviceCommandMapper;
import org.jeecg.modules.iot.device.service.IotDeviceCommandService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of {@link IotDeviceCommandService}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IotDeviceCommandServiceImpl extends JeecgServiceImpl<IotDeviceCommandMapper, IotDeviceCommand>
        implements IotDeviceCommandService {

    private static final Pattern COMMAND_CODE_PATTERN = Pattern.compile("^C:([^:]+):.*", Pattern.CASE_INSENSITIVE);

    private final AccDeviceRedisCache redisCache;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<IotDeviceCommand> enqueueCommands(String sn, List<String> commands, String operator) {
        if (StringUtils.isBlank(sn) || commands == null || commands.isEmpty()) {
            return List.of();
        }
        LocalDateTime now = LocalDateTime.now();
        Date nowDate = new Date();
        List<IotDeviceCommand> entities = new ArrayList<>();
        for (String command : commands) {
            if (StringUtils.isBlank(command)) {
                continue;
            }
            IotDeviceCommand entity = new IotDeviceCommand();
            entity.setSn(sn);
            entity.setCommandContent(command.trim());
            entity.setCommandCode(extractCommandCode(command));
            entity.setStatus(IotDeviceCommandStatus.PENDING);
            entity.setEnqueueTime(now);
            entity.setCreateBy(operator);
            entity.setCreateTime(nowDate);
            if (entity.getCommandCode() == null) {
                log.warn("Enqueue command without code: sn={}, sample={}", sn, StringUtils.abbreviate(entity.getCommandContent(), 120));
            }
            entities.add(entity);
        }
        if (entities.isEmpty()) {
            return List.of();
        }
        saveBatch(entities);
        for (IotDeviceCommand command : entities) {
            redisCache.enqueueCommand(sn, command.getId(), command.getCommandContent());
            log.debug("Pushed to redis queue: sn={}, dbId={}, code={}, len={}", sn, command.getId(), command.getCommandCode(), command.getCommandContent() == null ? 0 : command.getCommandContent().length());
        }
        return entities;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markCommandsSent(List<String> commandIds, LocalDateTime sentTime) {
        if (commandIds == null || commandIds.isEmpty() || sentTime == null) {
            return;
        }
        Date now = new Date();
        List<IotDeviceCommand> records = listByIds(commandIds);
        if (records.isEmpty()) {
            return;
        }
        records.forEach(command -> {
            if (command.getStatus() == IotDeviceCommandStatus.PENDING) {
                command.setStatus(IotDeviceCommandStatus.SENT);
            }
            command.setSentTime(sentTime);
            command.setUpdateTime(now);
        });
        updateBatchById(records);
        log.info("Marked commands sent: count={}, time={}, ids={}", records.size(), sentTime, commandIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Optional<IotDeviceCommand> handleCommandReport(String sn, String commandCode, String resultCode,
                                                          String resultMessage, String rawPayload, String clientIp) {

        log.info("Command ack report: sn={}, code={}, resultCode={}, clientIp={}", sn, commandCode, resultCode, clientIp);
        if (StringUtils.isAnyBlank(sn, commandCode)) {
            return Optional.empty();
        }
        IotDeviceCommand record = getOne(new LambdaQueryWrapper<IotDeviceCommand>()
                .eq(IotDeviceCommand::getSn, sn)
                .eq(IotDeviceCommand::getCommandCode, commandCode)
                .orderByDesc(IotDeviceCommand::getCreateTime)
                .last("limit 1"), false);
        if (record == null) {
            log.warn("Unknown command for ack: sn={}, code={}, resultCode={}", sn, commandCode, resultCode);
            return Optional.empty();
        }
        LocalDateTime now = LocalDateTime.now();
        Date nowDate = new Date();
        IotDeviceCommandStatus prevStatus = record.getStatus();
        boolean prevSentWasNull = (record.getSentTime() == null);
        record.setAckTime(now);
        // 补记 sent_time：ACK 到达但此前未标记为 SENT 的场景
        if (prevSentWasNull) {
            record.setSentTime(now);
        }
        record.setResultCode(resultCode);
//        record.setResultMessage(resultMessage);
        record.setLastReportPayload(StringUtils.defaultIfBlank(rawPayload, record.getLastReportPayload()));
        record.setLastReportIp(StringUtils.defaultIfBlank(clientIp, record.getLastReportIp()));
        record.setUpdateTime(nowDate);
        if (StringUtils.isBlank(resultCode) || StringUtils.equalsIgnoreCase(resultCode.trim(), "0")) {
            record.setStatus(IotDeviceCommandStatus.ACKED);
        } else {
            record.setStatus(IotDeviceCommandStatus.FAILED);
        }
        updateById(record);
        return Optional.of(record);
    }

    private String extractCommandCode(String command) {
        if (StringUtils.isBlank(command)) {
            return null;
        }
        Matcher matcher = COMMAND_CODE_PATTERN.matcher(command.trim());
        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }
}
