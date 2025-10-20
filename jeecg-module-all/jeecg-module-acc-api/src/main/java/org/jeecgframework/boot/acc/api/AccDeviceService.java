package org.jeecgframework.boot.acc.api;

import org.jeecgframework.boot.acc.query.AccDeviceQuery;
import org.jeecgframework.boot.acc.vo.AccDeviceVO;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;

import java.util.Map;

/**
 * 门禁设备服务接口
 * @author system
 * @date 2025-01-03
 */
public interface AccDeviceService {


    PageResult<AccDeviceVO> list(AccDeviceQuery accDeviceQuery,PageRequest pageRequest, Map<String,String[]> queryParam);

    /**
     * 根据ID查询设备详情
     * @param id 设备ID
     * @return 设备信息
     */
    AccDeviceVO getById(String id);

    /**
     * 根据SN查询设备详情
     * @param sn 设备序列号
     * @return 设备信息
     */
    AccDeviceVO getBySn(String sn);

    /**
     * 新增设备
     * @param deviceVO 设备信息
     * @return 设备信息
     */
    AccDeviceVO save(AccDeviceVO deviceVO);

    /**
     * 更新设备
     * @param deviceVO 设备信息
     * @return 设备信息
     */
    AccDeviceVO update(AccDeviceVO deviceVO);

    /**
     * 删除设备
     * @param id 设备ID
     * @return 是否成功
     */
    boolean deleteById(String id);

    /**
     * 批量删除设备
     * @param ids 设备ID列表
     * @return 是否成功
     */
    boolean deleteBatch(String[] ids);

     /**
      * 将设备信息写入临时表 acc_device_temp
      * @param deviceVO 设备信息（使用 sn、deviceName、isReset）
      */
     void saveTemp(org.jeecgframework.boot.acc.vo.AccDeviceVO deviceVO);
}
