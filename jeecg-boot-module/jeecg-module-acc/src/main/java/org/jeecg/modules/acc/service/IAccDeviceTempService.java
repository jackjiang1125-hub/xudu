package org.jeecg.modules.acc.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.acc.entity.AccDeviceTemp;
import org.jeecgframework.boot.acc.vo.AccDeviceVO;

/**
 * 门禁设备临时表 Service 接口
 */
public interface IAccDeviceTempService extends IService<AccDeviceTemp> {

    /**
     * 从设备VO保存一条临时记录
     * @param deviceVO 设备信息（使用 sn、deviceName、isReset）
     */
    void saveFromVO(AccDeviceVO deviceVO);

    /**
     * 根据设备SN删除临时记录
     * @param sn 设备SN
     */
    void removeByDeviceSn(String sn);
}