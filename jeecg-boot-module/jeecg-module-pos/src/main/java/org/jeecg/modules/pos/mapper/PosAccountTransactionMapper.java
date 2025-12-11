package org.jeecg.modules.pos.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.pos.entity.PosAccountTransaction;

/**
 * POS账户交易 Mapper
 */
@Mapper
public interface PosAccountTransactionMapper extends BaseMapper<PosAccountTransaction> {
}
