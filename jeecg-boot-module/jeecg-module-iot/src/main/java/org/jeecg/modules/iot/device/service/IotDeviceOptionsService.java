package org.jeecg.modules.iot.device.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.iot.device.entity.IotDeviceOptions;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 设备参数表 Service 接口
 */
public interface IotDeviceOptionsService extends IService<IotDeviceOptions> {

    /**
     * 保存设备参数信息
     * @param sn 设备序列号
     * @param deviceId 设备ID
     * @param options 参数Map
     * @param rawPayload 原始payload
     * @param clientIp 客户端IP
     * @param reportTime 上报时间
     */
    void saveDeviceOptions(String sn, String deviceId, Map<String, String> options, 
                          String rawPayload, String clientIp, LocalDateTime reportTime);

    /**
     * 根据设备序列号删除所有参数
     * @param sn 设备序列号
     */
    void deleteByDeviceSn(String sn);

    /**
     * 根据设备ID删除所有参数
     * @param deviceId 设备ID
     */
    void deleteByDeviceId(String deviceId);
}
