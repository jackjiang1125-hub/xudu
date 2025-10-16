package org.jeecg.modules.acc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.acc.entity.AccGroupDevice;
import org.jeecg.modules.acc.vo.AccDeviceVO;

import java.util.List;

public interface IAccGroupDeviceService extends JeecgService<AccGroupDevice> {

    /**
     * 根据权限组ID查询设备列表（分页）
     * @param groupId 权限组ID
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 设备分页列表
     */
    IPage<AccDeviceVO> listDevicesByGroupId(String groupId, Integer pageNo, Integer pageSize);

    /**
     * 根据权限组ID查询所有设备ID列表
     * @param groupId 权限组ID
     * @return 设备ID列表
     */
    List<String> listDeviceIdsByGroupId(String groupId);

    /**
     * 批量添加设备到权限组
     * @param groupId 权限组ID
     * @param deviceIds 设备ID列表
     */
    void addDevices(String groupId, List<String> deviceIds);

    /**
     * 批量从权限组移除设备
     * @param groupId 权限组ID
     * @param deviceIds 设备ID列表
     */
    void removeDevices(String groupId, List<String> deviceIds);
}