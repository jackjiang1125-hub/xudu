package org.jeecgframework.boot.iot.api;

import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.jeecgframework.boot.iot.query.IotDeviceQuery;
import org.jeecgframework.boot.iot.vo.IotDeviceVO;

import java.util.Map;

public interface IotDeviceService  {

    PageResult<IotDeviceVO> list(IotDeviceQuery iotDeviceQuery,PageRequest pageRequest, Map<String,String[]> queryParam);

    /**
     * 根据设备序列号查询iot设备的完整详情
     * @param sn 设备序列号
     * @return 设备详情（不存在返回null）
     */
    IotDeviceVO getBySn(String sn);

    /**
     * 为设备授权
     * @param deviceSn 设备序列号
     * @param registryCode 注册码
     * @param remark 备注
     * @param operator 操作人
     */
    void authorizeDevice(String deviceSn,
            String registryCode,
            String remark,
            String operator);

    /**
     * 同步时区
     * @param sn 设备序列号
     * @param tz 时区
     */
    void syncTimezone(String sn, String tz);

    /**
     * 同步时间
     * @param sn 设备序列号
     * @param epochSeconds 时间戳（秒）
     */
    void syncTime(String sn, Long epochSeconds);

}
