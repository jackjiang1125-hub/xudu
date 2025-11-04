package org.jeecg.modules.pos.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.pos.entity.PosConsumptionRecord;
import org.jeecg.modules.pos.vo.PosConsumptionRecordVO;

import java.util.Date;
import java.util.List;

/**
 * 消费记录服务接口
 */
public interface IPosConsumptionRecordService extends JeecgService<PosConsumptionRecord> {

    /**
     * 分页查询消费记录
     */
    IPage<PosConsumptionRecordVO> pageList(String cardNo, String customerId, String customerName, String type,
                                        String deviceCode, String deviceName, String restaurantCode,
                                        Date consumeTimeStart, Date consumeTimeEnd,
                                        Integer pageNo, Integer pageSize);

    /**
     * 根据ID查询消费记录详情
     */
    PosConsumptionRecordVO getDetailById(String id);

    /**
     * 批量删除消费记录
     */
    boolean deleteBatchByIds(String[] ids);

    /**
     * 删除商品分类
     */
    boolean deleteById(String id);
}