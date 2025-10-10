package org.jeecg.modules.iot.device.service;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.iot.device.entity.IotDevice;
import org.jeecg.modules.iot.device.enums.IotDeviceStatus;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

/**
 * Service for persisting and updating access control device metadata.
 */
public interface IotDeviceInnerService extends JeecgService<IotDevice> {

    Optional<IotDevice> findBySn(String sn);

    IotDevice recordInitialization(String sn, String deviceType, Map<String, String> queryParams,
                                   String clientIp, String rawPayload, LocalDateTime now);

    IotDevice recordRegistry(String sn, Map<String, String> registryParams, String clientIp,
                             String rawPayload, LocalDateTime now);

    void markHeartbeat(String sn, String clientIp, LocalDateTime heartbeatTime);

    void updateStatus(String sn, IotDeviceStatus status, boolean authorized);


    Optional<IotDevice> authorizeDevice(String sn, String registryCode, String remark, String operator);

}
