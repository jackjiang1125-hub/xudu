package org.jeecg.modules.acc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.LinkedHashMap;

import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.acc.entity.AccTimePeriod;
import org.jeecg.modules.acc.vo.TimePeriodVO;

public interface IAccTimePeriodService extends JeecgService<AccTimePeriod> {

    IPage<TimePeriodVO> pageList(String name, String creator, String updatedBegin, String updatedEnd,
                                 Integer pageNo, Integer pageSize);

    TimePeriodVO getDetailById(String id);

    TimePeriodVO saveVO(TimePeriodVO vo, String operator);

    TimePeriodVO updateVO(TimePeriodVO vo, String operator);

    boolean deleteWithDetails(String id);

    boolean deleteBatchWithDetails(String[] ids);

    /**
     * 根据时间段的序号（排序值）构建设备时区参数映射
     * 返回的 Map 可直接用于设备服务的 updateTimezone(sn, params)
     * @param order 序号（与 acc_time_period.sort_order 对应）
     * @return 设备时区参数
     */
    LinkedHashMap<String, Object> buildTimezoneParamsByOrder(int order);

    /**
     * 根据时间段的序号（排序值）构建并下发设备时区设置到指定设备
     * @param sn 设备SN
     * @param order 序号（与 acc_time_period.sort_order 对应）
     */
    void pushTimezoneByOrder(String sn, int order);
}