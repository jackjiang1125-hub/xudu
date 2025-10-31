package org.jeecg.modules.acc.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import javax.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.jeecg.modules.acc.entity.AccTransaction;
import org.jeecg.modules.acc.entity.AccDoor;
import org.jeecg.modules.acc.service.IAccTransactionService;
import org.jeecg.modules.acc.service.IAccDoorService;
import org.jeecgframework.boot.acc.api.AccDeviceService;
import org.jeecgframework.boot.acc.vo.AccDeviceVO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class AccRtLogConsumer implements Runnable {

    private static final String QUEUE_KEY = "iot:acc:rtlog:queue";
    private static final Duration DEDUP_TTL = Duration.ofDays(30);
    private static final Duration IDLE_SLEEP = Duration.ofSeconds(10);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final IAccTransactionService accTransactionService;
    private final AccDeviceService accDeviceService;
    private final IAccDoorService accDoorService;

    private Thread worker;
    private volatile boolean running = false;

    @PostConstruct
    public void start() {
        if (running) return;
        running = true;
        worker = new Thread(this, "acc-rtlog-consumer");
        worker.setDaemon(true);
        worker.start();
        log.info("[ACC] rtlog consumer thread started");
    }

    // @PreDestroy
    public void stop() {
        running = false;
        if (worker != null) {
            worker.interrupt();
        }
        log.info("[ACC] rtlog consumer thread stopped");
    }

    @Override
    public void run() {
        while (running) {
            try {
                String json = redisTemplate.opsForList().leftPop(QUEUE_KEY);
                if (json == null) {
                    try { Thread.sleep(IDLE_SLEEP.toMillis()); } catch (InterruptedException ignored) {}
                    continue;
                }

                Map<String, Object> m = objectMapper.readValue(json, new TypeReference<Map<String, Object>>(){});
                AccTransaction tx = mapToEntity(m);
                // 去重：SN + recordIndex 优先；否则使用 logTime+cardNo+eventCode+inoutStatus
                String uniqueKey = buildUniqueKey(tx);
                String dedupKey = "iot:acc:rtlog:dedup:" + uniqueKey;
                Boolean fresh = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1", DEDUP_TTL);
                if (fresh != null && fresh) {
                    accTransactionService.save(tx);
                } else {
                    log.debug("[ACC] Duplicate rtlog skipped key={}", uniqueKey);
                }
            } catch (Exception e) {
                log.warn("[ACC] rtlog consumer error: {}", e.getMessage(), e);
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            }
        }
    }

    private String buildUniqueKey(AccTransaction tx) {
        if (tx.getSn() == null) tx.setSn("");
        if (tx.getRecordIndex() != null) {
            return tx.getSn() + ":" + tx.getRecordIndex();
        }
        String lt = tx.getLogTime() != null ? String.valueOf(tx.getLogTime().toEpochSecond(java.time.ZoneOffset.ofHours(8))) : "0";
        return tx.getSn() + ":" + lt + ":" + nullSafe(tx.getCardNo()) + ":" + nullSafe(tx.getEventCode()) + ":" + nullSafe(tx.getInoutStatus());
    }

    private String nullSafe(Object o) { return o == null ? "" : String.valueOf(o); }

    private AccTransaction mapToEntity(Map<String, Object> m) {
        
        AccTransaction tx = new AccTransaction();
        tx.setSn(str(m.get("sn")));
        // 填充设备名称（根据 SN 查询设备）
        try {
            if (tx.getSn() != null && !tx.getSn().trim().isEmpty()) {
                AccDeviceVO deviceVO = accDeviceService.getBySn(tx.getSn());
                if (deviceVO != null && deviceVO.getDeviceName() != null) {
                    tx.setDeviceName(deviceVO.getDeviceName());
                }
            }
        } catch (Exception ignored) { }
        tx.setPin(str(m.get("pin")));
        String cardNo = (String) m.get("cardNo");
        tx.setCardNo("0".equals(cardNo) ? null : cardNo);
        tx.setEventAddr(intOrNull(m.get("eventAddr")));
        tx.setEventCode(intOrNull(m.get("eventCode")));
        tx.setInoutStatus(intOrNull(m.get("inoutStatus")));
        tx.setVerifyType(intOrNull(m.get("verifyType")));
        tx.setRecordIndex(intOrNull(m.get("recordIndex")));
        tx.setSiteCode(intOrNull(m.get("siteCode")));
        tx.setLinkId(intOrNull(m.get("linkId")));
        tx.setMaskFlag(intOrNull(m.get("maskFlag")));
        tx.setTemperature(intOrNull(m.get("temperature")));
        tx.setConvTemperature(intOrNull(m.get("convTemperature")));
        tx.setClientIp(str(m.get("clientIp")));
        tx.setRawPayload(str(m.get("rawPayload")));
        tx.setMediaFile(str(m.get("mediaFile")));
        // 填充门名称（根据设备 SN 与门编号 eventAddr 查询）
        try {
            Integer doorNumber = tx.getEventAddr();
            if (tx.getSn() != null && !tx.getSn().trim().isEmpty() && doorNumber != null) {
                LambdaQueryWrapper<AccDoor> qw = new LambdaQueryWrapper<>();
                qw.eq(AccDoor::getDeviceSn, tx.getSn()).eq(AccDoor::getDoorNumber, doorNumber);
                AccDoor door = accDoorService.getOne(qw);
                if (door != null && door.getDoorName() != null) {
                    tx.setDoorName(door.getDoorName());
                }
            }
        } catch (Exception ignored) { }
        // tx.setReaderName(str(m.get("readerName")));
        String logTimeStr = str(m.get("logTime"));
        try {
            tx.setLogTime(logTimeStr != null ? LocalDateTime.parse(logTimeStr) : null);
        } catch (Exception e) {
            tx.setLogTime(LocalDateTime.now());
        }
        return tx;
    }

    private String str(Object o) { return o == null ? null : String.valueOf(o); }
    private Integer intOrNull(Object o) {
        try { return o == null ? null : Integer.valueOf(String.valueOf(o)); } catch (Exception e) { return null; }
    }
}