package org.jeecg.modules.pos.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.pos.entity.PosConsumptionRecord;

/**
 * 消费记录Mapper
 */
@Mapper
public interface PosConsumptionRecordMapper extends BaseMapper<PosConsumptionRecord> {
}