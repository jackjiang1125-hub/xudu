package org.jeecg.modules.iot.device.protocol;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.iot.device.mapstruct.IotDeviceMapstruct;
import org.springframework.beans.factory.annotation.Value;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.modules.iot.device.cache.AccDeviceRedisCache;
import org.jeecg.modules.iot.device.entity.IotDevice;
import org.jeecg.modules.iot.device.entity.IotDeviceCommandReport;
import org.jeecg.modules.iot.device.entity.IotDevicePhoto;
import org.jeecg.modules.iot.device.entity.IotDeviceRtLog;
import org.jeecg.modules.iot.device.entity.IotDeviceState;
import org.jeecg.modules.iot.device.enums.IotDeviceStatus;

import org.jeecg.modules.iot.device.cache.AccDeviceRedisCache.QueuedCommand;
import org.jeecg.modules.iot.device.service.IotDeviceCommandReportService;
import org.jeecg.modules.iot.device.service.IotDeviceCommandService;
import org.jeecg.modules.iot.device.service.IotDeviceOptionsService;
import org.jeecg.modules.iot.device.service.IotDevicePhotoService;
import org.jeecg.modules.iot.device.service.IotDeviceRtLogService;
import org.jeecg.modules.iot.device.service.IotDeviceInnerService;
import org.jeecg.modules.iot.device.service.IotDeviceStateService;
import org.jeecg.modules.iot.device.util.DevicePayloadParser;
import org.jeecg.modules.iot.model.DeviceMessage;
import org.jeecg.modules.iot.model.DeviceResponse;
import org.jeecg.modules.iot.service.DeviceMessageProcessor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Message processor implementing the proprietary HTTP workflow for access control devices.
 */
@Slf4j
@RequiredArgsConstructor
public class AccDeviceMessageProcessor implements DeviceMessageProcessor {

    private static final String OK = "OK";

    private final IotDeviceInnerService iotDeviceInnerService;
    private final IotDeviceRtLogService iotDeviceRtLogService;
    private final IotDeviceStateService iotDeviceStateService;
    private final IotDevicePhotoService iotDevicePhotoService;
    private final IotDeviceCommandReportService iotDeviceCommandReportService;
    private final IotDeviceOptionsService iotDeviceOptionsService;
    private final IotDeviceCommandService iotDeviceCommandService;
    private final AccDeviceRedisCache redisCache;

    private final ApplicationEventPublisher applicationEventPublisher;
    private final IotDeviceMapstruct iotDeviceMapstruct;




    @Value("${jeecg.path.upload}")
    private String uploadPath;

    @Value("${jeecg.uploadType}")
    private String uploadType;

    @Override
    public DeviceResponse process(DeviceMessage message) {
        try {
            String path = message.getPath();
            if (StringUtils.isBlank(path)) {
                return DeviceResponse.text(404, "NOT FOUND");
            }
            return switch (path) {
                case "/iclock/cdata" -> handleCdata(message);
                case "/iclock/registry" -> handleRegistry(message);
                case "/iclock/push" -> handlePush(message);
                case "/iclock/getrequest" -> handleHeartbeat(message);
                case "/iclock/devicecmd" -> handleDeviceCommandReport(message);
                // 增加回复时区/iclock/rtdata
                case "/iclock/rtdata" -> handleRtdata(message);
                default -> DeviceResponse.text(404, "NOT FOUND");
            };
        } catch (Exception e) {
            log.error("Failed to process device message: uri={}, method={}, payload={}",
                    message.getUri(), message.getMethod(), message.getPayload(), e);
            return DeviceResponse.text(500, "ERROR");
        }
    }

    private DeviceResponse handleCdata(DeviceMessage message) {
        Map<String, String> query = message.getQueryParameters();
        String sn = firstValue(query, "sn");
        String table = firstValue(query, "table");
        String tableName = firstValue(query, "tableName");
        if (StringUtils.equalsIgnoreCase(firstValue(query, "options"), "all")
                && StringUtils.equalsIgnoreCase(firstValue(query, "DeviceType"), "acc")) {
            return handleInitialization(message, query);
        }
        if (StringUtils.isBlank(table)) {
            return DeviceResponse.text(400, "MISSING TABLE");
        }
        switch (table.toLowerCase()) {
            case "rtlog" -> handleRtLog(sn, message);
            case "rtstate" -> handleState(sn, message);
            case "attphoto" -> handlePhoto(sn, message);
            case "tabledata" -> handleTableData(sn,message,tableName);
            case "options" -> handleOptions(sn, message);
            default -> log.warn("Unsupported table {} from device {},content{}", table, sn,message.getPayload());
        }
        return DeviceResponse.text(OK);
    }

    private void handleTableData(String sn, DeviceMessage message,String tableName) {
        log.info("handleTableData: sn={}, tableName={} message={}", sn, tableName,message.getPayload());
        switch (tableName) {
            case "errorLog" -> handleErrorLog(sn, message);
            case "ATTPHOTO" -> handlePhoto(sn, message);
        }

    }

    private void handleErrorLog(String sn, DeviceMessage message) {
        log.error("sn {} 上报错误日志内容为 {}",sn,message.getPayload());
    }

    private DeviceResponse handleInitialization(DeviceMessage message, Map<String, String> query) {
        String sn = firstValue(query, "sn");
        if (StringUtils.isBlank(sn)) {
            return DeviceResponse.text("406");
        }
        String deviceType = firstValue(query, "DeviceType");
        LocalDateTime now = LocalDateTime.now();
        iotDeviceInnerService.recordInitialization(sn, deviceType, query, message.getClientIp(), message.getUri(), now);
        redisCache.cacheInitializationSnapshot(sn, query, message.getClientIp());
        return DeviceResponse.builder()
                .body("OK\nSupportPing=1")
                .contentType("text/plain; charset=UTF-8")
                .build();
    }

    private DeviceResponse handleRegistry(DeviceMessage message) {
        Map<String, String> query = message.getQueryParameters();
        String sn = firstValue(query, "sn");
        if (StringUtils.isBlank(sn)) {
            return DeviceResponse.text("406");
        }
        Map<String, String> body = DevicePayloadParser.parseKeyValuePayload(message.getPayload());
        LocalDateTime now = LocalDateTime.now();
        iotDeviceInnerService.recordRegistry(sn, body, message.getClientIp(), message.getPayload(), now);
        redisCache.cacheRegistrySnapshot(sn, body, message.getClientIp());
        Optional<IotDevice> device = iotDeviceInnerService.findBySn(sn);
        if (device.isPresent() && Boolean.TRUE.equals(device.get().getAuthorized())){

            if(device.get().getRegistryCode()!=null){
                //有注册码，说明是授权通过之后，首次添加至系统中来
                //需要发送通知给acc模块，添加设备信息至门禁模块。
                applicationEventPublisher.publishEvent(iotDeviceMapstruct.toRegisterAccDeviceEvent(device.get()));
            }
            return DeviceResponse.text("RegistryCode=" + device.get().getRegistryCode()+"\nPushProtVer=3.1.2");
        }
        // Ensure device is in pending status for manual authorization.
        device.filter(value -> value.getStatus() == null)
                .ifPresent(value -> iotDeviceInnerService.updateStatus(sn, IotDeviceStatus.PENDING, false));
        return DeviceResponse.text("UNAUTHORIZED");
    }

    private DeviceResponse handlePush(DeviceMessage message) {
        Map<String, String> query = message.getQueryParameters();
        String sn = firstValue(query, "sn");
        if (StringUtils.isNotBlank(sn)) {
            iotDeviceInnerService.findBySn(sn).ifPresent(device -> {
                if (Boolean.FALSE.equals(device.getAuthorized())) {
                    log.warn("Unauthorized device {} attempted to push configuration", sn);
                }
            });
        }
        String response = String.join("\n",
                "ServerVersion=3.0.1",
                "ServerName=ADMS",
                "PushVersion=3.0.1",
                "ErrorDelay=30",
                "RequestDelay=2",
                "TransTimes=00:0014:00",
                "TransInterval=1",
                "TransTables=User Transaction",
                "Realtime=1",
                "SessionID=30BFB04B2C8AECC72C01C03BFD549D15",
                "TimeoutSec=10",
                "");
        return DeviceResponse.builder()
                .body(response)
                .contentType("text/plain; charset=UTF-8")
                .build();
    }

    private DeviceResponse handleHeartbeat(DeviceMessage message) {
        Map<String, String> query = message.getQueryParameters();
        String sn = firstValue(query, "sn");
        if (StringUtils.isBlank(sn)) {
            return DeviceResponse.text(OK);
        }
        LocalDateTime heartbeatTime = LocalDateTime.now();
        redisCache.recordHeartbeat(sn, message.getClientIp());
        iotDeviceInnerService.markHeartbeat(sn, message.getClientIp(), heartbeatTime);

        List<QueuedCommand> commands = redisCache.drainCommands(sn);
        if (commands.isEmpty()) {
            return DeviceResponse.text(OK);
        }
        iotDeviceCommandService.markCommandsSent(commands.stream().map(QueuedCommand::id)
                .collect(Collectors.toList()), heartbeatTime);
        String body = commands.stream().map(QueuedCommand::content).collect(Collectors.joining("\r\n\r\n"));
       // log.info("Send heartbeat to device {}", body);
        return DeviceResponse.text(body);
    }

    private DeviceResponse handleDeviceCommandReport(DeviceMessage message) {
        // 处理多条命令报告，格式如下：
        // ID=500&Return=0&CMD=DATA UPDATE
        // ID=501&Return=0&CMD=DATA UPDATE
        // ID=502&Return=0&CMD=DATA UPDATE

        Map<String, String> query = message.getQueryParameters();
        String sn = firstValue(query, "sn");
        
        // 按换行符分割payload，处理多条记录
        String payload = message.getPayload();
        if (StringUtils.isBlank(payload)) {
            log.warn("Empty payload in device command report from device: {}", sn);
            return DeviceResponse.text(OK);
        }

        String[] commandLines = payload.split("\\r?\\n");
        LocalDateTime reportTime = LocalDateTime.now();
        
        for (String commandLine : commandLines) {
            if (StringUtils.isBlank(commandLine.trim())) {
                continue; // 跳过空行
            }
            
            try {
                // 解析每一行的键值对
                Map<String, String> body = DevicePayloadParser.parseKeyValueCmd(commandLine);
                //ID RETURN CMD

                // 如果查询参数中没有sn，尝试从body中获取
                String deviceSn = StringUtils.isNotBlank(sn) ? sn : firstValue(body, "sn");
                
                if (StringUtils.isBlank(deviceSn)) {
                    log.warn("No device SN found for command report line: {}", commandLine);
                    continue;
                }

                String commandCode = body.get("ID");
                if (StringUtils.isBlank(commandCode)) {
                    log.warn("No command ID found in command report line: {}", commandLine);
                    continue;
                }

                // 创建命令报告记录
                IotDeviceCommandReport report = new IotDeviceCommandReport();
                report.setSn(deviceSn);
                report.setCommandId(commandCode);
                report.setCommandContent(body.get("CMD"));
                report.setResultCode(body.get("Return"));
                //report.setResultMessage(firstValue(body, "Info", "Message", "msg"));
                report.setReportTime(reportTime);
                report.setRawPayload(commandLine);
                report.setClientIp(message.getClientIp());
                
                // 保存单条记录
                iotDeviceCommandReportService.save(report);

                // 处理命令报告
                iotDeviceCommandService.handleCommandReport(deviceSn, commandCode, report.getResultCode(),
                        report.getResultMessage(), commandLine, report.getClientIp());
                        
                log.debug("Processed command report: sn={}, commandId={}, result={}", 
                         deviceSn, commandCode, report.getResultCode());
                         
            } catch (Exception e) {
                log.error("Failed to process command report line: {}", commandLine, e);
            }
        }

        return DeviceResponse.text(OK);
    }

    /**
     * 兼容设备发起的 /iclock/rtdata?SN=xxx&type=time 请求，返回
     * DateTime=<旧编码秒数>,ServerTZ=<服务器时区>
     */
    private DeviceResponse handleRtdata(DeviceMessage message) {
        Map<String, String> query = message.getQueryParameters();
        String type = firstValue(query, "type");
        if (!StringUtils.equalsIgnoreCase(type, "time")) {
            return DeviceResponse.text(404, "NOT FOUND");
        }
        // 按文档要求：DateTime 为格林威治时间（UTC）按附录5旧编码算法转换的秒数
        long tt = toOldEncodedSecondsUTC(Instant.now());
        String serverTz = currentTzOffsetFormatted();
        String body = "DateTime=" + tt + ",ServerTZ=" + serverTz;
        return DeviceResponse.builder()
                .body(body)
                .contentType("text/plain; charset=UTF-8")
                .build();
    }

    /**
     * 附录5旧编码算法（基于 UTC）：
     * tt = ((year-2000)*12*31 + ((mon-1)*31) + day-1) * 86400 + (hour*60+min)*60 + sec
     */
    private long toOldEncodedSecondsUTC(Instant instant) {
        ZonedDateTime dt = instant.atZone(ZoneOffset.UTC);
        long days = ((dt.getYear() - 2000L) * 12L * 31L)
                + ((dt.getMonthValue() - 1L) * 31L)
                + (dt.getDayOfMonth() - 1L);
        long secondsOfDay = (dt.getHour() * 60L + dt.getMinute()) * 60L + dt.getSecond();
        return days * 24L * 60L * 60L + secondsOfDay;
    }

    /**
     * 服务器时区偏移，格式 +HHmm / -HHmm
     */
    private String currentTzOffsetFormatted() {
        ZoneOffset offset = ZonedDateTime.now(ZoneId.systemDefault()).getOffset();
        int totalSeconds = offset.getTotalSeconds();
        int abs = Math.abs(totalSeconds);
        int hours = abs / 3600;
        int minutes = (abs % 3600) / 60;
        String sign = totalSeconds >= 0 ? "+" : "-";
        return sign + String.format("%02d%02d", hours, minutes);
    }

    private void handleRtLog(String sn, DeviceMessage message) {
        log.info("处理上传门禁实时记录....jwz");
        List<Map<String, String>> records = DevicePayloadParser.parseKeyValueRecords(message.getPayload());
        if (records.isEmpty()) {
            return;
        }
        List<IotDeviceRtLog> logs = records.stream().map(map -> {
            IotDeviceRtLog logEntity = new IotDeviceRtLog();
            logEntity.setSn(sn);
            logEntity.setLogTime(DevicePayloadParser.parseDateTime(firstValue(map, "time")));
            logEntity.setPin(firstValue(map, "pin"));
            logEntity.setCardNo(firstValue(map, "cardno", "CardNo"));
            logEntity.setEventAddr(parseInteger(firstValue(map, "eventaddr", "EventAddr")));
            logEntity.setEventCode(parseInteger(firstValue(map, "event", "Event")));
            logEntity.setInoutStatus(parseInteger(firstValue(map, "inoutstatus", "InOutStatus")));
            logEntity.setVerifyType(parseInteger(firstValue(map, "verifytype", "VerifyType")));
            logEntity.setRecordIndex(parseInteger(firstValue(map, "index", "Index")));
            logEntity.setSiteCode(parseInteger(firstValue(map, "sitecode", "SiteCode")));
            logEntity.setLinkId(parseInteger(firstValue(map, "linkid", "LinkID")));
            logEntity.setMaskFlag(parseInteger(firstValue(map, "maskflag", "MaskFlag")));
            logEntity.setTemperature(parseInteger(firstValue(map, "temperature", "Temperature")));
            logEntity.setConvTemperature(parseInteger(firstValue(map, "convtemperature", "ConvTemperature")));
            logEntity.setRawPayload(String.join("\t", map.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.toList())));
            logEntity.setClientIp(message.getClientIp());
            return logEntity;
        }).collect(Collectors.toList());
        iotDeviceRtLogService.saveBatch(logs);
    }

    private void handleState(String sn, DeviceMessage message) {
        List<Map<String, String>> records = DevicePayloadParser.parseKeyValueRecords(message.getPayload());
        if (records.isEmpty()) {
            return;
        }
        List<IotDeviceState> states = records.stream().map(map -> {
            IotDeviceState state = new IotDeviceState();
            state.setSn(sn);
            state.setLogTime(DevicePayloadParser.parseDateTime(firstValue(map, "time")));
            state.setSensor(firstValue(map, "sensor", "Sensor"));
            state.setRelay(firstValue(map, "relay", "Relay"));
            state.setAlarm(firstValue(map, "alarm", "Alarm"));
            state.setDoor(firstValue(map, "door", "Door"));
            state.setRawPayload(String.join("\t", map.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + entry.getValue())
                    .collect(Collectors.toList())));
            state.setClientIp(message.getClientIp());
            return state;
        }).collect(Collectors.toList());
        iotDeviceStateService.saveBatch(states);
    }

    private void handlePhoto(String sn, DeviceMessage message) {
        Map<String, String> payload = DevicePayloadParser.parseKeyValuePayload(message.getPayload());
        if (payload.isEmpty()) {
            return;
        }
        
        String deviceSn = StringUtils.defaultIfBlank(sn, firstValue(payload, "sn"));
        String pin = firstValue(payload, "pin");
        String photoName = firstValue(payload, "message");
        String base64Photo = firstValue(payload, "photo");
        
        if (StringUtils.isBlank(base64Photo)) {
            log.warn("No photo data found in payload from device: {}", deviceSn);
            return;
        }
        
        // 生成文件名：设备SN_用户PIN_照片名称
        String fileName = deviceSn;
        if (StringUtils.isNotBlank(pin)) {
            fileName += "_" + pin;
        }
        if (StringUtils.isNotBlank(photoName)) {
            fileName += "_" + photoName;
        }
        
        // 保存图片文件
        String filePath = saveBase64Image(base64Photo, fileName);
        if (StringUtils.isBlank(filePath)) {
            log.error("Failed to save photo file for device: {}, pin: {}", deviceSn, pin);
            return;
        }
        
        IotDevicePhoto photo = new IotDevicePhoto();
        photo.setSn(deviceSn);
        photo.setPin(pin);
        photo.setPhotoName(photoName);
        photo.setFileSize(parseInteger(firstValue(payload, "size")));
        photo.setPhotoPath(filePath); // 保存文件路径而不是base64
        photo.setUploadedTime(LocalDateTime.now());
      //  photo.setRawPayload(message.getPayload()); 不存这个了 含有base64 太大了
        photo.setClientIp(message.getClientIp());
        
        iotDevicePhotoService.save(photo);
        
        log.info("Successfully processed device photo: sn={}, pin={}, photoName={}, filePath={}", 
                deviceSn, pin, photoName, filePath);
    }

    private void handleOptions(String sn, DeviceMessage message) {
        if (StringUtils.isBlank(sn)) {
            log.warn("Missing device SN for options data");
            return;
        }
        
        String payload = message.getPayload();
        if (StringUtils.isBlank(payload)) {
            log.warn("Empty payload for device options from device: {}", sn);
            return;
        }
        
        try {
            // 解析设备参数
            Map<String, String> options = DevicePayloadParser.parseDeviceOptions(payload);
            if (options.isEmpty()) {
                log.warn("No valid options parsed from payload: {}", payload);
                return;
            }
            
            log.info("Received {} device options from device {}: {}", options.size(), sn, options.keySet());
            
            // 查找设备ID
            String deviceId = null;
            Optional<IotDevice> device = iotDeviceInnerService.findBySn(sn);
            if (device.isPresent()) {
                deviceId = device.get().getId();
            }
            
            // 保存设备参数
            LocalDateTime reportTime = LocalDateTime.now();
            iotDeviceOptionsService.saveDeviceOptions(sn, deviceId, options, payload,
                                                    message.getClientIp(), reportTime);
            
            log.info("Successfully saved {} device options for device sn={}", options.size(), sn);
            
        } catch (Exception e) {
            log.error("Failed to process device options for device {}: {}", sn, payload, e);
        }
    }

    private Integer parseInteger(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 保存 base64 图片到文件系统
     * @param base64Data base64 图片数据
     * @param fileName 文件名（不含扩展名）
     * @return 文件访问路径
     */
    private String saveBase64Image(String base64Data, String fileName) {
        if (StringUtils.isBlank(base64Data)) {
            return null;
        }
        
        try {
            // 移除 data:image/jpeg;base64, 前缀（如果存在）
            String cleanBase64 = base64Data;
            if (base64Data.contains(",")) {
                cleanBase64 = base64Data.split(",", 2)[1];
            }
            
            // 解码 base64
            byte[] imageBytes = Base64.getDecoder().decode(cleanBase64);
            
            // 生成文件名
            String fullFileName = fileName + "_" + System.currentTimeMillis() + ".jpg";
            String bizPath = "iot/device/photos";
            
            // 根据配置保存文件
            String filePath;
            if (CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)) {
                filePath = CommonUtils.uploadOnlineImage(imageBytes, uploadPath, bizPath, uploadType);
            } else {
                filePath = CommonUtils.uploadOnlineImage(imageBytes, uploadPath, bizPath, uploadType);
            }
            
            log.info("Successfully saved device photo: fileName={}, filePath={}, size={} bytes", 
                    fullFileName, filePath, imageBytes.length);
            
            return filePath;
            
        } catch (Exception e) {
            log.error("Failed to save base64 image: fileName={}", fileName, e);
            return null;
        }
    }

    private String firstValue(Map<String, String> map, String... keys) {
        if (map == null || map.isEmpty() || keys == null) {
            return null;
        }
        for (String key : keys) {
            if (key == null) {
                continue;
            }
            if (map.containsKey(key)) {
                return map.get(key);
            }
            String value = map.entrySet().stream()
                    .filter(entry -> entry.getKey() != null && entry.getKey().equalsIgnoreCase(key))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
            if (value != null) {
                return value;
            }
        }
        return null;
    }
}
