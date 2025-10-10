package org.jeecg.modules.iot.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.iot.device.entity.IotDevice;
import org.jeecg.modules.iot.device.enums.IotDeviceStatus;
import org.jeecg.modules.iot.device.mapper.IotDeviceMapper;
import org.jeecg.modules.iot.device.service.IotDeviceInnerService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import java.util.UUID;


/**
 * Default implementation for {@link IotDeviceInnerService}.
 */
@Service
@RequiredArgsConstructor
public class IotDeviceInnerServiceImpl extends JeecgServiceImpl<IotDeviceMapper, IotDevice> implements IotDeviceInnerService {

    private final ObjectMapper objectMapper;

    @Override
    public Optional<IotDevice> findBySn(String sn) {
        if (StringUtils.isBlank(sn)) {
            return Optional.empty();
        }
        return Optional.ofNullable(getOne(new LambdaQueryWrapper<IotDevice>()
                .eq(IotDevice::getSn, sn)
                .last("limit 1"), false));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IotDevice recordInitialization(String sn, String deviceType, Map<String, String> queryParams,
                                          String clientIp, String rawPayload, LocalDateTime now) {
        IotDevice device = findBySn(sn).orElseGet(() -> {
            IotDevice created = new IotDevice();
            created.setSn(sn);
            created.setStatus(IotDeviceStatus.PENDING);
            created.setAuthorized(Boolean.FALSE);
            created.setDeviceType(deviceType);
            return created;
        });
        if (StringUtils.isNotBlank(deviceType)) {
            device.setDeviceType(deviceType);
        }
        device.setInitPayload(rawPayload);
        device.setLastInitTime(now);
        device.setLastKnownIp(StringUtils.defaultIfBlank(clientIp, device.getLastKnownIp()));
        if (queryParams != null && queryParams.containsKey("DeviceType")) {
            device.setDeviceType(queryParams.get("DeviceType"));
        }
        saveOrUpdate(device);
        return device;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public IotDevice recordRegistry(String sn, Map<String, String> registryParams, String clientIp,
                                    String rawPayload, LocalDateTime now) {
        IotDevice device = findBySn(sn).orElseGet(() -> {
            IotDevice created = new IotDevice();
            created.setSn(sn);
            created.setStatus(IotDeviceStatus.PENDING);
            created.setAuthorized(Boolean.FALSE);
            return created;
        });
        device.setLastRegistryTime(now);
        device.setLastKnownIp(StringUtils.defaultIfBlank(clientIp, device.getLastKnownIp()));
        if (registryParams != null) {
            device.setDeviceType(StringUtils.defaultIfBlank(registryParams.get("DeviceType"), device.getDeviceType()));
            device.setDeviceName(firstNonBlank(registryParams.get("DeviceName"), registryParams.get("~DeviceName"), device.getDeviceName()));
            device.setFirmwareVersion(StringUtils.defaultIfBlank(registryParams.get("FirmVer"), device.getFirmwareVersion()));
            device.setPushVersion(StringUtils.defaultIfBlank(registryParams.get("PushVersion"), device.getPushVersion()));
            device.setLockCount(parseInteger(registryParams.get("LockCount"), device.getLockCount()));
            device.setReaderCount(parseInteger(registryParams.get("ReaderCount"), device.getReaderCount()));
            device.setMachineType(parseInteger(registryParams.get("MachineType"), device.getMachineType()));
            device.setIpAddress(StringUtils.defaultIfBlank(registryParams.get("IPAddress"), device.getIpAddress()));
            device.setGatewayIp(StringUtils.defaultIfBlank(registryParams.get("GATEIPAddress"), device.getGatewayIp()));
            device.setNetMask(StringUtils.defaultIfBlank(registryParams.get("NetMask"), device.getNetMask()));
            try {
                device.setRegistryPayload(objectMapper.writeValueAsString(registryParams));
            } catch (JsonProcessingException e) {
                device.setRegistryPayload(rawPayload);
            }
        }
        if (StringUtils.isBlank(device.getDeviceType()) && registryParams != null) {
            device.setDeviceType(registryParams.get("DeviceType"));
        }
        saveOrUpdate(device);
        return device;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markHeartbeat(String sn, String clientIp, LocalDateTime heartbeatTime) {
        findBySn(sn).ifPresent(device -> {
            device.setLastHeartbeatTime(heartbeatTime);
            if (StringUtils.isNotBlank(clientIp)) {
                device.setLastKnownIp(clientIp);
            }
            updateById(device);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateStatus(String sn, IotDeviceStatus status, boolean authorized) {
        findBySn(sn).ifPresent(device -> {
            device.setStatus(status);
            device.setAuthorized(authorized);
            updateById(device);
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Optional<IotDevice> authorizeDevice(String sn, String registryCode, String remark, String operator) {
        Optional<IotDevice> optional = findBySn(sn);
        optional.ifPresent(device -> {
            device.setStatus(IotDeviceStatus.AUTHORIZED);
            device.setAuthorized(Boolean.TRUE);
            device.setRegistryCode(StringUtils.defaultIfBlank(registryCode, generateRegistryCode()));
            device.setRemark(StringUtils.defaultIfBlank(remark, device.getRemark()));
            device.setUpdateBy(operator);
            device.setUpdateTime(new java.util.Date());
            updateById(device);
        });
        return optional;
    }

    private String generateRegistryCode() {
        return UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
    }


    private Integer parseInteger(String value, Integer defaultValue) {
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }
}
