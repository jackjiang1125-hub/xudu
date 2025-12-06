package org.jeecg.modules.wec.service;

import org.jeecg.modules.wec.entity.WecDevice;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecgframework.boot.iot.vo.IotDeviceVO;
import java.util.List;

public interface IWecDeviceService extends IService<WecDevice> {
    
    /**
     * 搜索待添加的设备（从IOT模块查询）
     * @param keyword 搜索关键字 (sn 或 ip)
     * @return 设备列表
     */
    List<IotDeviceVO> searchPendingDevices(String keyword);
    
    /**
     * 绑定IOT设备到水控
     * @param iotDeviceId IOT设备ID
     * @return WecDevice
     */
    WecDevice bindDevice(String iotDeviceId);

    /**
     * 保存设备并处理绑定逻辑
     * @param entity WecDevice
     * @param resetData 是否重置设备数据
     * @return
     */
    boolean save(WecDevice entity, boolean resetData);

    /**
     * 批量执行远程控制指令
     * @param cmd 指令类型 (restart, factoryReset, syncTime, start, stop)
     * @param sns 设备SN列表 (逗号分隔)
     */
    void executeBatchControl(String cmd, String sns);
    
    /**
     * 更新使用指定费率模板的所有设备的费率配置
     * @param rateTemplateId 费率模板ID
     */
    void updateRateConfigByTemplateId(String rateTemplateId);

    /**
     * 删除设备（单个）
     */
    void removeDevice(String id);
    
    /**
     * 删除设备（批量）
     */
    void removeDevices(List<String> ids);
}
