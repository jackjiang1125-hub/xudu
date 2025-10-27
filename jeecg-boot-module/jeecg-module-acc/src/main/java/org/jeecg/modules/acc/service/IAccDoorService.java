package org.jeecg.modules.acc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.acc.entity.AccDoor;
import org.jeecg.modules.acc.vo.AccDoorVO;

/**
 * 门列表 Service
 */
public interface IAccDoorService extends IService<AccDoor> {

    /**
     * 分页查询门
     */
    IPage<AccDoor> pageDoors(String deviceName, String doorName, String ipAddress, Integer pageNo, Integer pageSize);

    /**
     * 保存VO（有ID则更新，无ID则新增）
     */
    AccDoor saveFromVO(AccDoorVO vo);

    /**
     * 删除门根据设备sn
     */
    void removeByDeviceSn(String deviceSn);
        
}