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
import org.jeecgframework.boot.wec.api.IWecUserServiceApi;
import org.jeecgframework.boot.wec.vo.WecConsumeRecordDTO;
import org.jeecgframework.boot.wec.vo.WecUserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
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

    @Autowired
    private IWecUserServiceApi wecUserService;
    
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
                iotDeviceInnerService.waterControlMarkHeartbeat(deviceNo, ip, LocalDateTime.now());
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
        
        long cardNumVal = u32(cardLE);
        String cardNo = String.valueOf(cardNumVal);

        WecUserVO user = wecUserService.getUserVoByCardNo(cardNo);

        String l1Str = "卡号:" + cardNo;
        String l2Str = "姓名:未注册";
        String l3Str = "消费:0.00";
        String l4Str = "余额:0.00";
        String nameStr = "未注册";
        int balanceVal = 0;
        int consumeVal = 0;

        if (user == null) {
            l2Str = "姓名:无效卡";
            l4Str = "请联系管理员";
        } else {
            nameStr = user.getRealName();
            if (nameStr == null) nameStr = "用户";
            l2Str = "姓名:" + nameStr;
            
            // Check Blacklist (UserType: 1=White, 2=Black)
            if ("2".equals(user.getUserType())) {
                l2Str = "状态:黑名单";
                l4Str = "禁止使用";
            } 
            // Check Status (Status: 1=Normal)
            else if (!"1".equals(user.getStatus())) {
                l2Str = "状态:异常";
                l4Str = "请联系管理员";
            } else {
                // Normal
                BigDecimal bal = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
                l4Str = "余额:" + bal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                balanceVal = bal.multiply(new BigDecimal(100)).intValue();
            }
        }
        
        byte[] l1 = toGB2312Fixed(l1Str, 16);
        byte[] l2 = toGB2312Fixed(l2Str, 16);
        byte[] l3 = toGB2312Fixed(l3Str, 16);
        byte[] l4 = toGB2312Fixed(l4Str, 16);
        byte[] voice = new byte[]{0x01, 0x13, 0x00};
        byte[] name = toGB2312Fixed(nameStr, 16);
        byte[] consume = to4(consumeVal);
        byte[] balance = to4(balanceVal);
        byte[] valid = new byte[]{toBcd(99), toBcd(12), toBcd(31)};
        byte[] discount = to2(100);
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
        // Data: [Cmd 0x76] [Count 1 byte] [Record 1...N] [Delete Token 4 bytes]
        int cnt = req[7] & 0xFF;
        
        // Records start at offset 8
        int offset = 8;
        
        // Calculate where data ends (to extract delete token)
        int dataEnd = 6 + len; 
        
        for (int i = 0; i < cnt; i++) {
            // Check remaining length. Each record is 25 bytes.
            if (offset + 25 > dataEnd) break;
            
            try {
                // 1. Machine No (2 bytes BE)
                int machineNo = ((req[offset] & 0xFF) << 8) | (req[offset + 1] & 0xFF);
                
                // 2. Operation Mode (1 byte)
                int mode = req[offset + 2] & 0xFF;
                
                // 3. Card No (4 bytes LE)
                long cardVal = u32(cardLE(req, offset + 3));
                String cardNo = String.valueOf(cardVal);
                
                WecConsumeRecordDTO dto = new WecConsumeRecordDTO();
                dto.setCardNo(cardNo);
                dto.setDeviceId(String.valueOf(machineNo));
                dto.setStatus("1"); // Success
                dto.setRecordNo(i + 1);
                
                // Bluetooth Mode: 0x2A(42), 0x2B(43)
                if (mode == 0x2A || mode == 0x2B) {
                    // Bluetooth Record Structure: Machine(2)+Mode(1)+Card(4)+Order(16)+Time/Flow(2) = 25
                    
                    // Order No (16 bytes)
                    byte[] orderBytes = new byte[16];
                    System.arraycopy(req, offset + 7, orderBytes, 0, 16);
                    String tradeNo = HexUtil.toHex(orderBytes);
                    dto.setTradeNo(tradeNo);
                    dto.setType("1"); // Consume
                    
                    // Amount/Balance unknown in this structure, setting to 0
                    dto.setAmount(BigDecimal.ZERO);
                    dto.setBalance(BigDecimal.ZERO);
                    dto.setConsumeTime(new Date()); // Use server time
                } else {
                    // Standard Record Structure: Machine(2)+Mode(1)+Card(4)+Date(6)+Balance(4)+Amount(4)+Rem(2)+Cnt(2) = 25
                    
                    // Date (6 bytes BCD: YYMMDDHHmmss) at offset + 7
                    int y = fromBcd(req[offset + 7]);
                    int m = fromBcd(req[offset + 8]);
                    int d = fromBcd(req[offset + 9]);
                    int h = fromBcd(req[offset + 10]);
                    int min = fromBcd(req[offset + 11]);
                    int s = fromBcd(req[offset + 12]);
                    
                    // Validate
                    if (m < 1 || m > 12) m = 1;
                    if (d < 1 || d > 31) d = 1;
                    if (h < 0 || h > 23) h = 0;
                    if (min < 0 || min > 59) min = 0;
                    if (s < 0 || s > 59) s = 0;
                    
                    LocalDateTime ldt = LocalDateTime.of(2000 + y, m, d, h, min, s);
                    Date consumeTime = Date.from(ldt.toInstant(ZoneOffset.of("+8")));
                    dto.setConsumeTime(consumeTime);
                    
                    // Generate Trade No: T + yyyyMMddHHmmss + Machine + Card
                    String tradeNo = String.format("T%04d%02d%02d%02d%02d%02d%d%s", 
                        2000+y, m, d, h, min, s, machineNo, cardNo);
                    dto.setTradeNo(tradeNo);
                    
                    // Balance (4 bytes BE) at offset + 13
                    long balanceVal = u32IntBE(req, offset + 13);
                    dto.setBalance(new BigDecimal(balanceVal).divide(new BigDecimal(100)));
                    
                    // Amount (4 bytes BE) at offset + 17
                    long amountVal = u32IntBE(req, offset + 17);
                    dto.setAmount(new BigDecimal(amountVal).divide(new BigDecimal(100)));
                    
                    dto.setType("1");
                }
                
                // Fill User Info
                WecUserVO user = wecUserService.getUserVoByCardNo(cardNo);
                if (user != null) {
                    dto.setUserId(user.getUserId());
                    dto.setUserName(user.getRealName());
                } else {
                    dto.setUserName("未注册");
                }
                
                wecUserService.saveConsumeRecord(dto);
                
                offset += 25;
            } catch (Exception e) {
                log.error("Failed to parse upload record index {}", i, e);
                break;
            }
        }

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
        
        long cardNumVal = u32(cardLE);
        String cardNo = String.valueOf(cardNumVal);

        WecUserVO user = wecUserService.getUserVoByCardNo(cardNo);

        String l1Str = "欢迎使用";
        String l2Str = "卡号:" + cardNo;
        String l3Str = "余额:0.00";
        String l4Str = "请开始使用";
        String nameStr = "未注册";
        int balanceVal = 0;
        int consumeVal = 0;

        if (user == null) {
            l1Str = "无效卡号";
            l4Str = "请联系管理员";
        } else {
            nameStr = user.getRealName();
            if (nameStr == null) nameStr = "用户";

            // Check Blacklist (UserType: 1=White, 2=Black)
            if ("2".equals(user.getUserType())) {
                l1Str = "黑名单卡";
                l4Str = "禁止使用";
            } 
            // Check Status (Status: 1=Normal)
            else if (!"1".equals(user.getStatus())) {
                l1Str = "卡状态异常";
                l4Str = "请联系管理员";
            } else {
                // Normal
                BigDecimal bal = user.getBalance() != null ? user.getBalance() : BigDecimal.ZERO;
                l3Str = "余额:" + bal.setScale(2, BigDecimal.ROUND_HALF_UP).toString();
                balanceVal = bal.multiply(new BigDecimal(100)).intValue();
            }
        }

        byte[] l1 = toGB2312Fixed(l1Str, 16);
        byte[] l2 = toGB2312Fixed(l2Str, 16);
        byte[] l3 = toGB2312Fixed(l3Str, 16);
        byte[] l4 = toGB2312Fixed(l4Str, 16);
        byte[] voice = new byte[]{0x00, 0x00, 0x01};
        byte[] name = toGB2312Fixed(nameStr, 16);
        byte[] consume = to4(consumeVal);
        byte[] balance = to4(balanceVal);
        byte[] discount = to2(100); // 100% discount (no discount) ? Or 0? Assuming 100 means 100% (original price) or 100% off? Usually 100 means 100% ratio (no discount).
        byte[] times = to2(0);
        byte[] timesLeft = to2(0);
        byte[] freeSecs = to2(0);
        
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

    private long u32IntBE(byte[] d, int off) {
        return ((long)(d[off] & 0xFF) << 24) |
               ((d[off + 1] & 0xFF) << 16) |
               ((d[off + 2] & 0xFF) << 8) |
               (d[off + 3] & 0xFF);
    }

    private byte[] to4(int v) {
        return new byte[]{(byte) ((v >>> 24) & 0xFF), (byte) ((v >>> 16) & 0xFF), (byte) ((v >>> 8) & 0xFF), (byte) (v & 0xFF)};
    }

    private byte[] to2(int v) {
        return new byte[]{(byte) ((v >>> 8) & 0xFF), (byte) (v & 0xFF)};
    }
}
