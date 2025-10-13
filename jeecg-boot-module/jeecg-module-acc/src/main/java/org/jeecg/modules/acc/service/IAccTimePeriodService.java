package org.jeecg.modules.acc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
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
}