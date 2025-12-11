package org.jeecg.modules.iot.device.protocol;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.filter.SsrfFileTypeFilter;
import org.jeecg.modules.iot.device.dto.HkAccessControllerEvent;
import org.jeecg.modules.iot.device.dto.HkEventWrapper;
import org.jeecg.modules.iot.device.entity.IotDevicePhoto;
import org.jeecg.modules.iot.device.entity.IotDeviceRtLog;
import org.jeecg.modules.iot.device.service.DeviceRtLogDispatcher;
import org.jeecg.modules.iot.device.service.IotDevicePhotoService;
import org.jeecg.modules.iot.model.DeviceFilePart;
import org.jeecg.modules.iot.model.DeviceMessage;
import org.jeecg.modules.iot.model.DeviceMultipartFileAdapter;
import org.jeecg.modules.iot.model.DeviceResponse;
import org.jeecg.modules.iot.service.DeviceMessageProcessor;
import org.jeecgframework.boot.iot.api.IotDeviceService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;



@Slf4j
@Component
@RequiredArgsConstructor
public class HkEventRecordProcessor implements DeviceMessageProcessor {

    private final ObjectMapper objectMapper;
    private final DeviceRtLogDispatcher rtLogDispatcher;

    private final IotDevicePhotoService iotDevicePhotoService;


    @Value("${jeecg.uploadType}")
    private String uploadType;



    @Value("${jeecg.path.upload}")
    private String uploadPath;

    @Override
    public boolean supports(DeviceMessage message) {
        // 只处理 /event/record POST
        return "/event/record".equalsIgnoreCase(message.getPath())
                && "POST".equalsIgnoreCase(message.getMethod());
    }

    @Override
    public DeviceResponse process(DeviceMessage message) {
        // event_log 是表单字段，DeviceMessageHandler 已经把它放到了 payload 中
        String json = message.getPayload();
        if (json == null || json.isBlank()) {
            log.warn("HK /event/record empty payload from ip={}", message.getClientIp());
            return DeviceResponse.text(400, "EMPTY EVENT_LOG");
        }

        try {
            HkEventWrapper wrapper = objectMapper.readValue(json, HkEventWrapper.class);
            if (!"AccessControllerEvent".equalsIgnoreCase(wrapper.getEventType())) {
                log.debug("Ignore HK eventType={} from ip={}", wrapper.getEventType(), message.getClientIp());
                return DeviceResponse.text("success");
            }

            HkAccessControllerEvent evt = wrapper.getAccessControllerEvent();
            if (evt == null) {
                log.warn("HK AccessControllerEvent is null, raw={}", json);
                return DeviceResponse.text("success");
            }

            // 1. 先把事件转成 IotDeviceRtLog 并落库
            IotDeviceRtLog rtLog = mapToRtLog(wrapper, evt, message);
            rtLogDispatcher.dispatch(Collections.singletonList(rtLog));

            // 2. 再处理照片（如果有）
            handlePictureUpload(rtLog, message);

            return DeviceResponse.text("success");
        } catch (Exception e) {
            log.error("Failed to parse HK event_log, raw={}", json, e);
            return DeviceResponse.text(500, "PARSE ERROR");
        }
    }

    private void handlePictureUpload(IotDeviceRtLog rtLog, DeviceMessage message) {
        DeviceFilePart picturePart = message.getFileParameters().get("Picture");
        if (picturePart == null || picturePart.getBytes() == null || picturePart.getBytes().length == 0) {
            return;
        }

        try {
            MultipartFile file = new DeviceMultipartFileAdapter(picturePart);

            // 目录我们自己固定，不从设备参数传，避免 biz 注入问题
            String bizPath = "iot/device/photos";

            String savePath;
            if (CommonConstant.UPLOAD_TYPE_LOCAL.equals(uploadType)) {
                // 和 sys/common/upload 一样，本地上传先做文件类型过滤，再走 uploadLocal
                SsrfFileTypeFilter.checkUploadFileType(file);
                savePath = CommonUtils.uploadLocal(file, bizPath, uploadPath);
            } else {
                // 其他类型（OSS、Minio等）统一走 CommonUtils.upload
                savePath = CommonUtils.upload(file, bizPath, uploadType);
            }

            if (StringUtils.isBlank(savePath)) {
                log.warn("HK event picture upload failed, sn={}, pin={}, ip={}",
                        rtLog.getSn(), rtLog.getPin(), message.getClientIp());
                return;
            }

            // 落库 IotDevicePhoto，风格和 ZK 那边保持一致
            IotDevicePhoto photo = new IotDevicePhoto();
            photo.setSn(rtLog.getSn());
            photo.setPin(rtLog.getPin());
            photo.setPhotoName(picturePart.getFilename());
            photo.setFileSize((int) picturePart.getSize());
            photo.setPhotoPath(savePath);
            photo.setUploadedTime(rtLog.getLogTime() != null ? rtLog.getLogTime() : LocalDateTime.now());
            photo.setClientIp(message.getClientIp());

            iotDevicePhotoService.save(photo);

            log.info("Saved HK event picture: sn={}, pin={}, path={}", photo.getSn(), photo.getPin(), savePath);
        } catch (Exception e) {
            log.error("Failed to handle HK event picture, sn={}, ip={}",
                    rtLog.getSn(), message.getClientIp(), e);
        }
    }


    /**
     * HK JSON → IotDeviceRtLog 映射逻辑
     */
    private IotDeviceRtLog mapToRtLog(HkEventWrapper wrapper,
                                      HkAccessControllerEvent evt,
                                      DeviceMessage message) {

        String sn = wrapper.getShortSerialNumber();
        if (sn == null || sn.isBlank()) {
            // 没有短序列号，用 ip 兜底
            sn = wrapper.getIpAddress();
        }

        LocalDateTime logTime = parseDateTime(wrapper.getDateTime());

        IotDeviceRtLog log = new IotDeviceRtLog();
        log.setSn(sn);
        log.setLogTime(logTime);

        // HK 这条记录里：有 cardNo / employeeNo / name 等
        log.setCardNo(evt.getCardNo());
        log.setEmployeeNo(evt.getEmployeeNo());
        log.setUserName(evt.getName());

        // 事件类型：用 subEventType 作为 eventCode，比 ZK 的 event 更接近语义
        log.setMajorEventType(evt.getMajorEventType());
        log.setSubEventType(evt.getSubEventType());
        log.setEventCode(evt.getSubEventType());

        // 记录索引：serialNo
        log.setRecordIndex(evt.getSerialNo());

        // 验证模式：保持原始字符串，verifyType 保留给 ZK 那套数字枚举
        log.setVerifyMode(evt.getCurrentVerifyMode());

        // 通道、门号
        log.setChannelId(wrapper.getChannelId());
        log.setDoorNo(evt.getDoorNo());

        // 口罩标识简单 map 一下："no" -> 0, "yes" -> 1, 其他 null
        log.setMaskFlag(mapMask(evt.getMask()));

        // 其他字段暂时没有对应的，保持 null 即可
        log.setInoutStatus(null);
        log.setVerifyType(null);
        log.setEventAddr(null);
        log.setSiteCode(null);
        log.setLinkId(null);
        log.setTemperature(null);
        log.setConvTemperature(null);

        log.setRawPayload(message.getPayload());
        log.setClientIp(message.getClientIp());

        // 厂商标识
        log.setVendor("HIKVISION");

        return log;
    }

    private LocalDateTime parseDateTime(String dateTime) {
        // 示例："2025-11-11T17:15:23+08:00"
        try {
            return OffsetDateTime.parse(dateTime).toLocalDateTime();
        } catch (Exception e) {
            log.warn("Failed to parse HK dateTime={}, use now()", dateTime, e);
            return LocalDateTime.now();
        }
    }

    private Integer mapMask(String mask) {
        if (mask == null) {
            return null;
        }
        return switch (mask.toLowerCase()) {
            case "yes" -> 1;
            case "no" -> 0;
            default -> null;
        };
    }
}
