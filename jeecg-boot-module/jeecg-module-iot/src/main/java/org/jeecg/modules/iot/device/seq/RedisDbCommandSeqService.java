package org.jeecg.modules.iot.device.seq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.iot.device.mapper.IotDeviceCommandMapper;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Objects;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RedisDbCommandSeqService implements CommandSeqService {

    private final StringRedisTemplate redis;
    private final IotDeviceCommandMapper mapper;

    private static final String SEQ_KEY_PREFIX  = "iot:acc:cmdseq:";
    private static final String LOCK_KEY_PREFIX = "iot:acc:cmdseq:lock:";

    @Override
    public long nextSeq(String sn) {
        if (StringUtils.isBlank(sn)) throw new IllegalArgumentException("sn cannot be blank");
        final String key = SEQ_KEY_PREFIX + sn;
        try {
            // Fast path: if key exists, INCR
            String cur = redis.opsForValue().get(key);
            if (cur != null) {
                Long v = redis.opsForValue().increment(key);
                if (v == null) throw new IllegalStateException("Redis INCR returned null");
                return v;
            }
            // Initialize from DB if needed (first time or after FLUSHALL)
            initializeFromDbIfNeeded(sn, key, false);
            Long v = redis.opsForValue().increment(key);
            if (v == null) throw new IllegalStateException("Redis INCR returned null after init");
            return v;
        } catch (DataAccessException | IllegalStateException e) {
            // Redis unavailable → degrade to DB: max + 1
            Long max = safeMaxFromDb(sn);
            return (max == null ? 1L : max + 1L);
        }
    }

    @Override
    public void rebuildFromDb(String sn) {
        if (StringUtils.isBlank(sn)) return;
        initializeFromDbIfNeeded(sn, SEQ_KEY_PREFIX + sn, true);
    }

    private Long safeMaxFromDb(String sn) {
        try {
            return mapper.selectMaxCommandCodeBySn(sn);
        } catch (Exception ex) {
            log.warn("selectMaxCommandCodeBySn failed for sn={}", sn, ex);
            return null;
        }
    }

    private void initializeFromDbIfNeeded(String sn, String key, boolean force) {
        final String lockKey = LOCK_KEY_PREFIX + sn;
        final String token = UUID.randomUUID().toString();
        Boolean ok = redis.opsForValue().setIfAbsent(lockKey, token, Duration.ofSeconds(5));
        if (Boolean.TRUE.equals(ok)) {
            try {
                String again = redis.opsForValue().get(key);
                if (force || again == null) {
                    Long max = safeMaxFromDb(sn);
                    long base = (max == null ? 0L : max);
                    redis.opsForValue().set(key, String.valueOf(base));
                }
            } finally {
                String v = redis.opsForValue().get(lockKey);
                if (Objects.equals(v, token)) {
                    redis.delete(lockKey);
                }
            }
        } else {
            // another node is initializing; tiny backoff
            try { Thread.sleep(30); } catch (InterruptedException ignored) {}
        }
    }
}
