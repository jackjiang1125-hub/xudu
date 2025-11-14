package org.jeecg.modules.acc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

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
    void remoteOpenDoors(List<String> doorIds, Integer pulseSeconds, String operator);

    /**
     * 远程关门/锁定（支持批量门ID）。
     * @param doorIds 门ID集合
     * @param operator 操作人用户名
     */
    void remoteCloseDoors(List<String> doorIds, String operator);

    /**
     * 取消报警（支持批量门ID）。
     */
    void remoteCancelAlarmDoors(List<String> doorIds, String operator);

    /**
     * 远程常开（支持批量门ID）。
     */
    void remoteHoldOpenDoors(List<String> doorIds, String operator);

    /**
     * 远程锁定（支持批量门ID）。
     */
    void remoteLockDoors(List<String> doorIds, String operator);

    /**
     * 远程解锁（支持批量门ID）。
     */
    void remoteUnlockDoors(List<String> doorIds, String operator);
    /**
     * 启动当天常开时间段（支持批量门ID）。
     */
    void enableTodayAlwaysOpen(List<String> doorIds, String operator);
    /**
     * 禁用当天常开时间段（支持批量门ID）。
     */
    void disableTodayAlwaysOpen(List<String> doorIds, String operator);
        
}