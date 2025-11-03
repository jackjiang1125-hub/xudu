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

    /**
     * 远程开门（支持批量门ID）。
     * @param doorIds 门ID集合
     * @param pulseSeconds 继电器保持秒数（可空）
     * @param operator 操作人用户名
     */
    void remoteOpenDoors(java.util.List<String> doorIds, Integer pulseSeconds, String operator);

    /**
     * 远程关门/锁定（支持批量门ID）。
     * @param doorIds 门ID集合
     * @param operator 操作人用户名
     */
    void remoteCloseDoors(java.util.List<String> doorIds, String operator);

    /**
     * 取消报警（支持批量门ID）。
     */
    void remoteCancelAlarmDoors(java.util.List<String> doorIds, String operator);

    /**
     * 远程常开（支持批量门ID）。
     */
    void remoteHoldOpenDoors(java.util.List<String> doorIds, String operator);

    /**
     * 远程锁定（支持批量门ID）。
     */
    void remoteLockDoors(java.util.List<String> doorIds, String operator);

    /**
     * 远程解锁（支持批量门ID）。
     */
    void remoteUnlockDoors(java.util.List<String> doorIds, String operator);
        
}