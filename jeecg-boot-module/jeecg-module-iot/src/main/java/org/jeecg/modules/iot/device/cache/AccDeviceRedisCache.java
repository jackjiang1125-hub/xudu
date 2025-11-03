package org.jeecg.modules.iot.device.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.base.BaseMap;
import org.jeecg.common.constant.WebsocketConst;
import org.jeecg.common.modules.redis.client.JeecgRedisClient;
import org.jeecg.modules.iot.device.entity.IotDeviceRtLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis cache helper for storing device state, heartbeats and command queues.
 */
@Component
@RequiredArgsConstructor
public class AccDeviceRedisCache {

    private static final Logger log = LoggerFactory.getLogger(AccDeviceRedisCache.class);

    private static final Duration DEFAULT_TTL = Duration.ofHours(24);
    private static final int MAX_COMMANDS_PER_HEARTBEAT = 50;

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final JeecgRedisClient jeecgRedisClient;

    public void cacheInitializationSnapshot(String sn, Map<String, String> queryParams, String clientIp) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        Map<String, Object> snapshot = new HashMap<>();
        if (queryParams != null) {
            snapshot.putAll(queryParams);
        }
        snapshot.put("clientIp", clientIp);
        snapshot.put("timestamp", Instant.now().toString());
        writeJson(key("init", sn), snapshot, DEFAULT_TTL);
    }

    public void cacheRegistrySnapshot(String sn, Map<String, String> registryParams, String clientIp) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        Map<String, Object> snapshot = new HashMap<>();
        if (registryParams != null) {
            snapshot.putAll(registryParams);
        }
        snapshot.put("clientIp", clientIp);
        snapshot.put("timestamp", Instant.now().toString());
        writeJson(key("registry", sn), snapshot, DEFAULT_TTL);
    }

    public void recordHeartbeat(String sn, String clientIp) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        Map<String, Object> heartbeat = new HashMap<>();
        heartbeat.put("clientIp", clientIp);
        heartbeat.put("timestamp", Instant.now().toEpochMilli());
        writeJson(key("heartbeat", sn), heartbeat, DEFAULT_TTL);
    }




    public List<QueuedCommand> drainCommands(String sn) {
        List<QueuedCommand> commands = new ArrayList<>();

        if (StringUtils.isBlank(sn)) {
            return commands;
        }
        String redisKey = key("commands", sn);
        for (int i = 0; i < MAX_COMMANDS_PER_HEARTBEAT; i++) {

            String payload = redisTemplate.opsForList().leftPop(redisKey);
            if (payload == null) {
                break;
            }
            QueuedCommand command = QueuedCommand.fromPayload(payload);
            if (command != null) {
                commands.add(command);
            }
        }
        if (commands.isEmpty()) {
            redisTemplate.expire(redisKey, DEFAULT_TTL);
        }
        return commands;
    }


    public void enqueueCommand(String sn, String commandId, String commandContent) {
        if (StringUtils.isAnyBlank(sn, commandId, commandContent)) {
            return;
        }
        String redisKey = key("commands", sn);
        redisTemplate.opsForList().rightPush(redisKey, commandId + "|" + commandContent);
        redisTemplate.expire(redisKey, DEFAULT_TTL);
    }

    private void writeJson(String key, Map<String, Object> snapshot, Duration ttl) {
        try {
            String json = objectMapper.writeValueAsString(snapshot);
            redisTemplate.opsForValue().set(key, json, ttl);
        } catch (JsonProcessingException e) {
            log.warn("Failed to serialize redis snapshot for key {}", key, e);
        }
    }

    private String key(String category, String sn) {
        return "iot:acc:" + category + ":" + sn;
    }


    /**
     * 将门禁设备实时记录（rtlog）入队到 Redis 列表，供 ACC 模块异步按序消费。
     * 队列键：iot:acc:rtlog:queue
     */
    public void enqueueRtLogs(List<IotDeviceRtLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }
        String redisKey = "iot:acc:rtlog:queue";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (IotDeviceRtLog rtlog : logs) {
            try {
                java.util.Map<String, Object> payload = new java.util.HashMap<>();
                payload.put("sn", rtlog.getSn());
                String logTimeStr = rtlog.getLogTime() != null ? rtlog.getLogTime().format(formatter) : null;
                payload.put("logTime", logTimeStr);
                payload.put("pin", rtlog.getPin());
                payload.put("cardNo", rtlog.getCardNo());
                payload.put("eventAddr", rtlog.getEventAddr());
                payload.put("eventCode", rtlog.getEventCode());
                payload.put("inoutStatus", rtlog.getInoutStatus());
                payload.put("verifyType", rtlog.getVerifyType());
                payload.put("recordIndex", rtlog.getRecordIndex());
                payload.put("siteCode", rtlog.getSiteCode());
                payload.put("linkId", rtlog.getLinkId());
                payload.put("maskFlag", rtlog.getMaskFlag());
                payload.put("temperature", rtlog.getTemperature());
                payload.put("convTemperature", rtlog.getConvTemperature());
                payload.put("clientIp", rtlog.getClientIp());
                payload.put("rawPayload", rtlog.getRawPayload());
                // mediaFile: 与设备抓拍保存命名保持一致
                // 实际文件名示例：CKI2203760036_20251031125220-5190019.jpg
                // 规则：{sn}_{yyyyMMddHHmmss}-{pin}.jpg
                if (StringUtils.isNotBlank(rtlog.getSn())) {
                    String timeStamp = null;
                    if (rtlog.getLogTime() != null) {
                        // 日志时间格式转换：yyyy-MM-dd HH:mm:ss -> yyyyMMddHHmmss
                        DateTimeFormatter tsFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                        timeStamp = rtlog.getLogTime().format(tsFmt);
                    }

                    String fileName = "";
                    if (StringUtils.isNotBlank(timeStamp)) {
                        // 按实际保存规则拼接
                        fileName = rtlog.getSn() + "_" + timeStamp + "-" + rtlog.getPin() + ".jpg";
                    }
                    String mediaPath = "iot/device/photos/" + fileName;
                    payload.put("mediaFile", mediaPath);
                }
                String json = objectMapper.writeValueAsString(payload);
                // 记录按到达顺序写入队列，确保消费顺序
                redisTemplate.opsForList().rightPush(redisKey, json);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                log.warn("Failed to serialize rtlog for queue, sn={}", rtlog.getSn(), e);
            }
        }
    }

    /**
     * 通过 Redis 发布订阅机制，将 rtlog 实时推送到 JeecgBoot WebSocket 通道。
     * 通道名遵循系统约定：socketHandler（与 WebSocket.REDIS_TOPIC_NAME 一致）。
     */
    public void publishRtLogMessages(List<IotDeviceRtLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return;
        }
        final String topic = "socketHandler";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (IotDeviceRtLog rtlog : logs) {
            try {
                Map<String, Object> msg = new HashMap<>();
                String sn = rtlog.getSn();
                String logTimeStr = rtlog.getLogTime() != null ? rtlog.getLogTime().format(formatter) : null;
                // 标准 websocket 字段
                msg.put(WebsocketConst.MSG_CMD, "acc_rtlog");
                msg.put(WebsocketConst.MSG_ID, (sn != null ? sn : "") + "-" + (rtlog.getRecordIndex() != null ? rtlog.getRecordIndex() : logTimeStr));
                msg.put(WebsocketConst.MSG_TXT, "ACC实时事件");
                // 业务负载
                msg.put("sn", sn);
                msg.put("logTime", logTimeStr);
                msg.put("pin", rtlog.getPin());
                msg.put("cardNo", rtlog.getCardNo());
                msg.put("eventAddr", rtlog.getEventAddr());
                msg.put("eventCode", rtlog.getEventCode());
                msg.put("inoutStatus", rtlog.getInoutStatus());
                msg.put("verifyType", rtlog.getVerifyType());
                msg.put("recordIndex", rtlog.getRecordIndex());
                msg.put("siteCode", rtlog.getSiteCode());
                msg.put("linkId", rtlog.getLinkId());
                msg.put("maskFlag", rtlog.getMaskFlag());
                msg.put("temperature", rtlog.getTemperature());
                msg.put("convTemperature", rtlog.getConvTemperature());
                msg.put("clientIp", rtlog.getClientIp());
                // 复用队列中的 mediaFile 命名规则
                if (StringUtils.isNotBlank(rtlog.getSn())) {
                    String timeStamp = null;
                    if (rtlog.getLogTime() != null) {
                        DateTimeFormatter tsFmt = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                        timeStamp = rtlog.getLogTime().format(tsFmt);
                    }
                    String fileName = "";
                    if (StringUtils.isNotBlank(timeStamp)) {
                        fileName = rtlog.getSn() + "_" + timeStamp + "-" + rtlog.getPin() + ".jpg";
                    }
                    String mediaPath = "iot/device/photos/" + fileName;
                    msg.put("mediaFile", mediaPath);
                }

                String json = objectMapper.writeValueAsString(msg);
                BaseMap baseMap = new BaseMap();
                // 为空代表广播给所有在线用户
                baseMap.put("userId", "");
                baseMap.put("message", json);
                jeecgRedisClient.sendMessage(topic, baseMap);
            } catch (Exception e) {
                log.warn("Failed to publish rtlog websocket message, sn={}", rtlog.getSn(), e);
            }
        }
    }


    /**
     * Value object used for queued commands in Redis.
     */
    public record QueuedCommand(String id, String content) {

        private static QueuedCommand fromPayload(String payload) {
            if (StringUtils.isBlank(payload)) {
                return null;
            }
            int idx = payload.indexOf('|');
            if (idx <= 0 || idx >= payload.length() - 1) {
                return null;
            }
            String id = payload.substring(0, idx);
            String content = payload.substring(idx + 1);
            if (StringUtils.isAnyBlank(id, content)) {
                return null;
            }
            return new QueuedCommand(id, content);
        }
    }

}
