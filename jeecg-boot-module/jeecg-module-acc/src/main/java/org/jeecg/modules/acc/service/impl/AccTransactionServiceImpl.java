package org.jeecg.modules.acc.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.jeecg.modules.acc.entity.AccTransaction;
import org.jeecg.modules.acc.mapper.AccTransactionMapper;
import org.jeecg.modules.acc.service.IAccTransactionService;

@Service
public class AccTransactionServiceImpl extends ServiceImpl<AccTransactionMapper, AccTransaction>
        implements IAccTransactionService {
}