package org.jeecg.modules.iot.device.service;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.iot.device.entity.IotDevicePhoto;
import org.jeecg.modules.iot.device.vo.AccDevicePhotoVO;

/**
 * Service for persisting photos uploaded by devices.
 */
public interface IotDevicePhotoService extends JeecgService<IotDevicePhoto> {
    
    /**
     * 根据设备SN和日志时间查询照片完整信息
     * @param sn 设备序列号
     * @param logTime 日志时间 (格式: 2025-10-03 19:21:32)
     * @return 照片完整信息
     */
    AccDevicePhotoVO findPhotoInfoBySnAndLogTime(String sn, String logTime);
}
