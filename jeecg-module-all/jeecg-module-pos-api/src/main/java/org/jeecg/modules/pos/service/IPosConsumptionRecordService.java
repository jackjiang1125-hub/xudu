package org.jeecg.modules.pos.service;

import org.jeecg.modules.pos.vo.PosConsumptionRecordVO;
import org.jeecg.modules.pos.request.PosConsumptionRecordQuery;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;

import java.util.Map;

/**
 * 消费记录服务接口
 */
public interface IPosConsumptionRecordService {

    /**
     * 分页查询消费记录
     */
    PageResult<PosConsumptionRecordVO> list(PosConsumptionRecordQuery query, PageRequest pageRequest, Map<String, String[]> queryParam);

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
