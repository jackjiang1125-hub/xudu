package org.jeecgframework.boot.iot.api;

import org.jeecgframework.boot.iot.vo.IotWaterControlDeviceVO;
import org.jeecgframework.boot.iot.vo.WaterRateConfigVO;
import java.util.List;
import java.util.Map;

public interface IotWaterControlDeviceService {
    
    /**
     * 下发费率配置
     * @param sn 设备序列号
     * @param config 费率配置
     */
    void sendRateConfig(String sn, WaterRateConfigVO config);

    /**
     * 查询待绑定的水控设备
     * @param keyword SN或IP关键字
     * @return
     */
    List<IotWaterControlDeviceVO> queryPendingWaterDevices(String keyword);

    /**
     * 绑定设备（授权）
     * @param sn 设备序列号
     * @param authorized 是否授权
     * @return
     */
    boolean setAuthorization(String sn, boolean authorized);

    /**
     * 更新设备名称（下发指令）
     * @param sn 设备序列号
     * @param deviceName 新名称
     */
    void updateDeviceName(String sn, String deviceName);

    /**
     * 清空设备数据（下发指令）
     * @param sn 设备序列号
     */
    void clearDeviceData(String sn);

    void syncTime(String sn, Long timestamp);
    
    /**
     * 设备重启
     */
    void restartDevice(String sn);

    /**
     * 恢复出厂设置
     */
    void factoryResetDevice(String sn);

    /**
     * 清除设备缓存（Redis Auth 和 Heartbeat）
     * @param sn 设备序列号
     */
    void removeCache(String sn);

    /**
     * 修改设备SN（下发指令并更新数据库）
     * @param oldSn 旧SN
     * @param newSn 新SN
     */
    void updateDeviceSn(String oldSn, String newSn);

    void removeUserAndAuthorize(String sn, String userCode);
    void setDoorFirstCardOpenDoor(String sn, Map<String, Integer> params);
    Map<String, Object> getLatestOptionsBySn(String sn);

    /**
     * 设置名单模式
     * @param sn 设备SN
     * @param mode 0:白名单, 1:黑名单
     */
    void setNamelistMode(String sn, int mode);

    /**
     * 查询设备总使用统计（发送0xCA指令）
     * @param sn 设备SN
     */
    void queryTotalUsage(String sn);

    /**
     * 查询当前名单模式（发送0x49指令，数据01）
     * @param sn 设备SN
     */
    void getNamelistMode(String sn);
}
