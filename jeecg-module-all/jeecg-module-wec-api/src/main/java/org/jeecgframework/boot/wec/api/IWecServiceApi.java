package org.jeecgframework.boot.wec.api;

import org.jeecgframework.boot.wec.vo.WecConsumeRecordDTO;
import org.jeecgframework.boot.wec.vo.WecUserVO;

public interface IWecServiceApi {
    /**
     * 根据卡号获取用户信息
     * @param cardNo
     * @return
     */
    WecUserVO getUserVoByCardNo(String cardNo);

    /**
     * 保存消费记录
     * @param dto 消费记录
     */
    void saveConsumeRecord(WecConsumeRecordDTO dto);

    /**
     * 根据SN更新设备的名单模式
     * @param sn 设备SN
     * @param mode 名单模式 (0:白名单, 1:黑名单)
     */
    void updateDeviceNamelistMode(String sn, int mode);

    /**
     * 同步设备SN变更（更新WecDevice表）
     * @param oldSn 旧SN
     * @param newSn 新SN
     */
    void syncDeviceSn(String oldSn, String newSn);

    /**
     * 更新设备IP地址
     * @param sn 设备SN
     * @param ip IP地址
     */
    void updateDeviceIp(String sn, String ip);
}