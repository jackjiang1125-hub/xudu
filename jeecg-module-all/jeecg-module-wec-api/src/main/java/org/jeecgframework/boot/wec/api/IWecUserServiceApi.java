package org.jeecgframework.boot.wec.api;

import org.jeecgframework.boot.wec.vo.WecConsumeRecordDTO;
import org.jeecgframework.boot.wec.vo.WecUserVO;

public interface IWecUserServiceApi {
    /**
     * 根据卡号获取用户信息
     * @param cardNo
     * @return
     */
    WecUserVO getUserVoByCardNo(String cardNo);

    /**
     * 保存消费记录
     * @param recordDTO
     */
    void saveConsumeRecord(WecConsumeRecordDTO recordDTO);
}