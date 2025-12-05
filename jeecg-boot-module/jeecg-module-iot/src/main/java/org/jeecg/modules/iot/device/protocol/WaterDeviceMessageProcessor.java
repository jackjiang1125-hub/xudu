package org.jeecg.modules.iot.device.protocol;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.iot.device.entity.IotDevice;
import org.jeecg.modules.iot.device.enums.IotDeviceStatus;
import org.jeecg.modules.iot.device.service.IotDeviceInnerService;
import org.jeecg.modules.iot.device.service.IotDeviceStateService;
import org.jeecg.modules.iot.model.DeviceMessage;
import org.jeecg.modules.iot.model.DeviceResponse;
import org.jeecg.modules.iot.service.DeviceMessageProcessor;
import org.jeecg.modules.iot.util.Crc16Modbus;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.modules.iot.util.HexUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class WaterDeviceMessageProcessor implements DeviceMessageProcessor {

    @Autowired
    private IotDeviceInnerService iotDeviceInnerService;

    @Autowired
    private IotDeviceStateService iotDeviceStateService;
    
    @Autowired
    private RedisUtil redisUtil;

    private final WaterDeviceSessionManager sessionManager;
    
    private static final String REDIS_KEY_PREFIX_AUTH = "iot:water:auth:";
    private static final String REDIS_KEY_PREFIX_HEARTBEAT = "iot:water:heartbeat:";

    public WaterDeviceMessageProcessor(WaterDeviceSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }

    @Override
    public boolean supports(DeviceMessage message) {
        // Support messages that have rawBody (coming from WaterNettyHandler)
        return "TCP".equals(message.getMethod()) && message.getRawBody() != null;
    }

    @Override
    public DeviceResponse process(DeviceMessage message) {
        byte[] data = message.getRawBody();
        String ip = message.getClientIp();
        
        log.info("Water Processor recv {} bytes from {}: {}", data.length, ip, HexUtil.toHex(data));
        
        // Notify session manager in case there is a pending sync request
        sessionManager.onResponseReceived(ip, data);

        // Heartbeat 0x74
        if (isHeartbeat74(data)) {
            if (checkDeviceAuthorized(data, ip)) {
                byte[] response = buildHeartbeatResponse(data);
                return DeviceResponse.builder().rawBody(response).build();
            } else {
                // 未授权或新设备，不回复心跳，让设备端继续重试或管理员介入
                return DeviceResponse.builder().build(); 
            }
        }
        // Consume 0x63
        else if (isConsume63(data)) {
            byte[] response = buildConsumeResponse(data);
            return DeviceResponse.builder().rawBody(response).build();
        }
        // Upload 0x76
        else if (isUpload76(data)) {
            byte[] response = buildUploadResponse(data);
            return DeviceResponse.builder().rawBody(response).build();
        }
        // Query Balance 0x65
        else if (isQueryBalance65(data)) {
            byte[] response = buildQueryBalanceResponse(data);
            return DeviceResponse.builder().rawBody(response).build();
        }

        // If no match, return empty (or maybe we should log warning)
        return DeviceResponse.builder().build(); 
    }

    private boolean checkDeviceAuthorized(byte[] data, String ip) {
        try {
            int addr = ((data[2] & 0xFF) << 8) | (data[3] & 0xFF);
            String deviceNo = String.valueOf(addr);
            
            // 1. Check Redis first
            String authKey = REDIS_KEY_PREFIX_AUTH + deviceNo;
            if (redisUtil.hasKey(authKey)) {
                // Update heartbeat in Redis
                redisUtil.set(REDIS_KEY_PREFIX_HEARTBEAT + deviceNo, System.currentTimeMillis());
                // Async update DB heartbeat (optional, or periodic)
                iotDeviceInnerService.markHeartbeat(deviceNo, ip, LocalDateTime.now());
                return true;
            }

            // 2. Check DB
            IotDevice device = iotDeviceInnerService.findBySnAndIp(deviceNo, ip);
            
            if (device == null) {
                // ** 自动迁移逻辑：尝试通过 IP 查找旧设备 **
                IotDevice oldDevice = iotDeviceInnerService.findByIp(ip);
                if (oldDevice != null && IotDeviceStatus.AUTHORIZED.equals(oldDevice.getStatus())) {
                    log.info("Device SN changed from {} to {} at IP {}", oldDevice.getSn(), deviceNo, ip);
                    
                    // 更新旧设备 SN 为新 SN
                    String oldSn = oldDevice.getSn();
                    oldDevice.setSn(deviceNo);
                    iotDeviceInnerService.updateDevice(oldDevice);
                    
                    // 清除旧 SN 的缓存（如果有）
                    if (StringUtils.isNotBlank(oldSn)) {
                        redisUtil.del(REDIS_KEY_PREFIX_AUTH + oldSn);
                        redisUtil.del(REDIS_KEY_PREFIX_HEARTBEAT + oldSn);
                    }
                    
                    // 重新授权缓存新 SN
                    redisUtil.set(authKey, "true", 86400);
                    redisUtil.set(REDIS_KEY_PREFIX_HEARTBEAT + deviceNo, System.currentTimeMillis());
                    iotDeviceInnerService.markHeartbeat(deviceNo, ip, LocalDateTime.now());
                    
                    // 同时更新 WecDevice 表中的 SN (如果需要)
                    // 这里 IotDeviceInnerService 最好能广播事件或同步更新业务表
                    // 但由于分层，可能需要 Wec 模块监听或轮询。
                    // 为了简化，假设业务表会通过其他方式同步，或者我们在 WecDeviceServiceImpl 中已经做了预处理。
                    // 但如果设备自己改了 SN (非平台下发)，业务表会脱节。
                    // 鉴于之前的逻辑是平台下发指令改 SN，Wec 模块已经更新了 DB (WecDevice)。
                    // 所以这里只需要确保 IotDevice 表也更新，不产生新记录即可。
                    
                    return true;
                }
                
                log.info("New Water Device detected: SN={}, IP={}. Registering as PENDING.", deviceNo, ip);
                iotDeviceInnerService.recordInitialization(deviceNo, "water_control", null, ip, getPacketDescription(data), LocalDateTime.now());
                return false;
            }
            
            if (!Boolean.TRUE.equals(device.getAuthorized())) {
                log.warn("Water Device Unauthorized: SN={}, IP={}. Waiting for authorization.", deviceNo, ip);
                if (device.getStatus() != IotDeviceStatus.PENDING) {
                    iotDeviceInnerService.updateStatus(deviceNo, IotDeviceStatus.PENDING, false);
                }
                return false;
            }
            
            // 3. Authorized -> Cache in Redis
            // Cache for 1 day, or until status changes (need to clear cache on status update)
            redisUtil.set(authKey, "true", 86400); 
            
            // Update heartbeat
            redisUtil.set(REDIS_KEY_PREFIX_HEARTBEAT + deviceNo, System.currentTimeMillis());
            iotDeviceInnerService.markHeartbeat(deviceNo, ip, LocalDateTime.now());
            return true;
            
        } catch (Exception e) {
            log.warn("Failed to check device authorization", e);
            return false;
        }
    }

    // --- Logic ported from DeviceService ---

    private String getPacketDescription(byte[] data) {
        if (isHeartbeat74(data) && data.length >= 15) {
            try {
                int y1 = fromBcd(data[7]);
                int y2 = fromBcd(data[8]);
                int year = y1 * 100 + y2;
                int month = fromBcd(data[9]);
                int day = fromBcd(data[10]);
                int hour = fromBcd(data[12]);
                int minute = fromBcd(data[13]);
                int second = fromBcd(data[14]);
                return String.format("心跳包 %04d-%02d-%02d %02d:%02d:%02d", year, month, day, hour, minute, second);
            } catch (Exception e) {
                // ignore
            }
        }
        return HexUtil.toHex(data);
    }

    private boolean isHeartbeat74(byte[] d) {
        if (d == null || d.length < 9) return false;
        // Check header FE 03
        if ((d[0] & 0xFF) != 0xFE || (d[1] & 0xFF) != 0x03) return false;
        
        // Addr bytes at d[2], d[3] (ignored here)
        
        // Length at d[4], d[5]
        int len = ((d[4] & 0xFF) << 8) | (d[5] & 0xFF);
        
        // Check total length: 6 header bytes + len data + 2 CRC (optional check, but rawBody usually has CRC)
        // Actually rawBody from Netty might include CRC or not depending on handler.
        // Assuming rawBody is the full frame.
        if (d.length < 6 + len) return false;
        
        // Command code at d[6]
        return (d[6] & 0xFF) == 0x74;
    }

    private byte[] buildHeartbeatResponse(byte[] req) {
        byte[] time = extractAndPlusOneSecond(req);
        byte[] payload = new byte[6 + 1 + 8];
        payload[0] = (byte) 0xFE;
        payload[1] = 0x03;
        payload[2] = req.length > 2 ? req[2] : 0x00;
        payload[3] = req.length > 3 ? req[3] : 0x01;
        payload[4] = 0x00;
        payload[5] = 0x09;
        payload[6] = (byte) 0x74;
        System.arraycopy(time, 0, payload, 7, 8);
        // Demo parameters: startIndex=2, skipLenBytes=false, highFirst=false, type=modbus
        // But check this: Demo says device.crc-start-index=2 (in application.properties)
        // DeviceService.java uses: props.getCrcStartIndex()
        // So we must use startIndex=2 to match demo behavior!
        byte[] frame = Crc16Modbus.appendCrcCustom(payload, 2, false, false); 
        log.info("heartbeat ack generated: {}", HexUtil.toHex(frame));
        return frame;
    }

    private byte[] extractAndPlusOneSecond(byte[] req) {
        try {
            int len = ((req[4] & 0xFF) << 8) | (req[5] & 0xFF);
            if ((req[6] & 0xFF) == 0x74 && len >= 9 && req.length >= 15) {
                int y1 = req[7] & 0xFF;
                int y2 = req[8] & 0xFF;
                int year = y1 * 100 + y2;
                int month = req[9] & 0xFF;
                int day = req[10] & 0xFF;
                int week = req[11] & 0xFF;
                int hour = req[12] & 0xFF;
                int minute = req[13] & 0xFF;
                int second = req[14] & 0xFF;
                LocalDateTime ldt = LocalDateTime.of(year, month, day, hour, minute, second);
                ldt = ldt.plusSeconds(1);
                return new byte[]{
                        (byte) (ldt.getYear() / 100),
                        (byte) (ldt.getYear() % 100),
                        (byte) ldt.getMonthValue(),
                        (byte) ldt.getDayOfMonth(),
                        (byte) week,
                        (byte) ldt.getHour(),
                        (byte) ldt.getMinute(),
                        (byte) ldt.getSecond()
                };
            }
        } catch (Exception ignored) {}
        return buildTimeBytes();
    }

    private byte[] buildTimeBytes() {
        java.time.ZonedDateTime now = java.time.ZonedDateTime.now(java.time.ZoneId.systemDefault());
        int year = now.getYear();
        int y1 = year / 100;
        int y2 = year % 100;
        int month = now.getMonthValue();
        int day = now.getDayOfMonth();
        int week = now.getDayOfWeek().getValue();
        int hour = now.getHour();
        int minute = now.getMinute();
        int second = now.getSecond();
        return new byte[]{
                toBcd(y1),
                toBcd(y2),
                toBcd(month),
                toBcd(day),
                toBcd(week),
                toBcd(hour),
                toBcd(minute),
                toBcd(second)
        };
    }

    private byte toBcd(int n) {
        int tens = (n / 10) % 10;
        int ones = n % 10;
        return (byte) ((tens << 4) | ones);
    }

    private int fromBcd(byte b) {
        int hi = (b >> 4) & 0xF;
        int lo = b & 0xF;
        return hi * 10 + lo;
    }

    private boolean isConsume63(byte[] d) {
        if (d == null || d.length < 9) return false;
        if ((d[0] & 0xFF) != 0xFE || (d[1] & 0xFF) != 0x03) return false;
        int len = ((d[4] & 0xFF) << 8) | (d[5] & 0xFF);
        if (d.length < 6 + len) return false;
        return (d[6] & 0xFF) == 0x63;
    }

    private byte[] buildConsumeResponse(byte[] req) {
        // Simplified logic from DeviceService
        int len = ((req[4] & 0xFF) << 8) | (req[5] & 0xFF);
        int base = 7;
        int cardLE = 0;
        if (len >= 1 + 4) {
             // card bytes extraction
             cardLE = cardLE(req, base);
        }
        
        byte[] l1 = toGB2312Fixed("卡号:" + u32(cardLE), 16);
        byte[] l2 = toGB2312FixedLimit("姓名:张三", 8, 16);
        byte[] l3 = toGB2312FixedLimit("消费:10.85", 8, 16);
        byte[] l4 = toGB2312FixedLimit("余额:100.00", 8, 16);
        byte[] voice = new byte[]{0x01, 0x13, 0x00};
        byte[] name = toGB2312FixedLimit("张三", 8, 16);
        byte[] consume = to4(1085);
        byte[] balance = to4(10000);
        byte[] valid = new byte[]{toBcd(99), toBcd(12), toBcd(31)};
        byte[] discount = to2(1000);
        byte[] times = to2(1);
        byte[] timesLeft = to2(99);
        byte[] freeSecs = to2(60);
        
        byte[] data = new byte[64 + 3 + 16 + 4 + 4 + 3 + 2 + 2 + 2 + 2];
        int off = 0;
        System.arraycopy(l1, 0, data, off, 16); off += 16;
        System.arraycopy(l2, 0, data, off, 16); off += 16;
        System.arraycopy(l3, 0, data, off, 16); off += 16;
        System.arraycopy(l4, 0, data, off, 16); off += 16;
        System.arraycopy(voice, 0, data, off, 3); off += 3;
        System.arraycopy(name, 0, data, off, 16); off += 16;
        System.arraycopy(consume, 0, data, off, 4); off += 4;
        System.arraycopy(balance, 0, data, off, 4); off += 4;
        System.arraycopy(valid, 0, data, off, 3); off += 3;
        System.arraycopy(discount, 0, data, off, 2); off += 2;
        System.arraycopy(times, 0, data, off, 2); off += 2;
        System.arraycopy(timesLeft, 0, data, off, 2); off += 2;
        System.arraycopy(freeSecs, 0, data, off, 2); off += 2;

        byte[] payload = new byte[6 + 1 + data.length];
        payload[0] = (byte) 0xFE;
        payload[1] = 0x03;
        payload[2] = req.length > 2 ? req[2] : 0x00;
        payload[3] = req.length > 3 ? req[3] : 0x01;
        payload[4] = (byte) (((1 + data.length) >>> 8) & 0xFF);
        payload[5] = (byte) ((1 + data.length) & 0xFF);
        payload[6] = (byte) 0x63;
        System.arraycopy(data, 0, payload, 7, data.length);
        
        byte[] frame = Crc16Modbus.appendCrcCustom(payload, 2, false, false);
        log.info("consume reply generated: {}", HexUtil.toHex(frame));
        return frame;
    }

    private boolean isUpload76(byte[] d) {
        if (d == null || d.length < 9) return false;
        if ((d[0] & 0xFF) != 0xFE || (d[1] & 0xFF) != 0x03) return false;
        int len = ((d[4] & 0xFF) << 8) | (d[5] & 0xFF);
        if (d.length < 6 + len) return false;
        return (d[6] & 0xFF) == 0x76;
    }

    private byte[] buildUploadResponse(byte[] req) {
        int len = ((req[4] & 0xFF) << 8) | (req[5] & 0xFF);
        int dataStart = 6;
        int dataEnd = dataStart + len;
        // Logic to parse records and log them would go here (omitted for brevity, but logging key info)
        int cnt = req[7] & 0xFF;
        byte[] cred = new byte[4];
        if (dataEnd >= 4) {
            System.arraycopy(req, dataEnd - 4, cred, 0, 4);
        }
        
        byte[] payload = new byte[6 + 1 + 8];
        payload[0] = (byte) 0xFE;
        payload[1] = 0x03;
        payload[2] = req.length > 2 ? req[2] : 0x00;
        payload[3] = req.length > 3 ? req[3] : 0x01;
        payload[4] = 0x00;
        payload[5] = 0x09;
        payload[6] = (byte) 0x77;
        byte[] cnt4 = to4(cnt);
        System.arraycopy(cnt4, 0, payload, 7, 4);
        System.arraycopy(cred, 0, payload, 11, 4);
        
        byte[] frame = Crc16Modbus.appendCrcCustom(payload, 2, false, false);
        log.info("upload ack generated: {}", HexUtil.toHex(frame));
        return frame;
    }

    private boolean isQueryBalance65(byte[] d) {
        if (d == null || d.length < 9) return false;
        if ((d[0] & 0xFF) != 0xFE || (d[1] & 0xFF) != 0x03) return false;
        int len = ((d[4] & 0xFF) << 8) | (d[5] & 0xFF);
        if (d.length < 6 + len) return false;
        return (d[6] & 0xFF) == 0x65;
    }

    private byte[] buildQueryBalanceResponse(byte[] req) {
        int len = ((req[4] & 0xFF) << 8) | (req[5] & 0xFF);
        int base = 7;
        int cardLE = 0;
        if (len >= 1 + 4) {
            cardLE = cardLE(req, base);
        }
        
        byte[] l1 = toGB2312Fixed("欢迎使用", 16);
        byte[] l2 = toGB2312Fixed("卡号" + u32(cardLE), 16);
        byte[] l3 = toGB2312Fixed("余额:123.45", 16);
        byte[] l4 = toGB2312Fixed("请开始使用", 16);
        byte[] voice = new byte[]{0x00, 0x00, 0x01};
        byte[] name = toGB2312Fixed("张三", 16);
        byte[] consume = to4(1234);
        byte[] balance = to4(567890);
        byte[] discount = to2(80);
        byte[] times = to2(1);
        byte[] timesLeft = to2(0);
        byte[] freeSecs = to2(60);
        
        byte[] data = new byte[64 + 3 + 16 + 4 + 4 + 2 + 2 + 2 + 2];
        int off = 0;
        System.arraycopy(l1, 0, data, off, 16); off += 16;
        System.arraycopy(l2, 0, data, off, 16); off += 16;
        System.arraycopy(l3, 0, data, off, 16); off += 16;
        System.arraycopy(l4, 0, data, off, 16); off += 16;
        System.arraycopy(voice, 0, data, off, 3); off += 3;
        System.arraycopy(name, 0, data, off, 16); off += 16;
        System.arraycopy(consume, 0, data, off, 4); off += 4;
        System.arraycopy(balance, 0, data, off, 4); off += 4;
        System.arraycopy(discount, 0, data, off, 2); off += 2;
        System.arraycopy(times, 0, data, off, 2); off += 2;
        System.arraycopy(timesLeft, 0, data, off, 2); off += 2;
        System.arraycopy(freeSecs, 0, data, off, 2); off += 2;
        
        byte[] payload = new byte[6 + 1 + data.length];
        payload[0] = (byte) 0xFE;
        payload[1] = 0x03;
        payload[2] = req.length > 2 ? req[2] : 0x00;
        payload[3] = req.length > 3 ? req[3] : 0x01;
        payload[4] = (byte) (((1 + data.length) >>> 8) & 0xFF);
        payload[5] = (byte) ((1 + data.length) & 0xFF);
        payload[6] = (byte) 0x65;
        System.arraycopy(data, 0, payload, 7, data.length);
        
        byte[] frame = Crc16Modbus.appendCrcCustom(payload, 2, false, false);
        log.info("balance reply generated: {}", HexUtil.toHex(frame));
        return frame;
    }

    // Helpers
    private byte[] toGB2312Fixed(String s, int len) {
        return toGB2312FixedLimit(s, len, len);
    }

    private byte[] toGB2312FixedLimit(String s, int limit, int total) {
        byte[] out = new byte[total];
        for (int i = 0; i < total; i++) out[i] = 0x20;
        byte[] bs;
        try {
            bs = s == null ? new byte[0] : s.getBytes("GB2312");
        } catch (Exception e) {
            bs = s == null ? new byte[0] : s.getBytes(StandardCharsets.UTF_8);
        }
        int n = Math.min(limit, bs.length);
        if (n > 0) System.arraycopy(bs, 0, out, 0, n);
        return out;
    }

    private int cardLE(byte[] d, int off) {
        return ((d[off + 3] & 0xFF) << 24) | ((d[off + 2] & 0xFF) << 16) | ((d[off + 1] & 0xFF) << 8) | (d[off] & 0xFF);
    }

    private long u32(int v) {
        return v & 0xFFFFFFFFL;
    }

    private byte[] to4(int v) {
        return new byte[]{(byte) ((v >>> 24) & 0xFF), (byte) ((v >>> 16) & 0xFF), (byte) ((v >>> 8) & 0xFF), (byte) (v & 0xFF)};
    }

    private byte[] to2(int v) {
        return new byte[]{(byte) ((v >>> 8) & 0xFF), (byte) (v & 0xFF)};
    }
}
