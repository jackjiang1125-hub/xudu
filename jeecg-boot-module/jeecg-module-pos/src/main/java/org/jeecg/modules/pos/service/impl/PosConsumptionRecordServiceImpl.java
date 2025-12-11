package org.jeecg.modules.pos.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.pos.entity.PosConsumptionDetail;
import org.jeecg.modules.pos.entity.PosConsumptionRecord;
import org.jeecg.modules.pos.mapper.PosConsumptionDetailMapper;
import org.jeecg.modules.pos.mapper.PosConsumptionRecordMapper;
import org.jeecg.modules.pos.mapstruct.PosConsumptionDetailMapstruct;
import org.jeecg.modules.pos.mapstruct.PosConsumptionRecordMapstruct;
import org.jeecg.modules.pos.service.IPosConsumptionRecordService;
import org.jeecg.modules.pos.vo.PosConsumptionRecordVO;
import org.jeecg.modules.pos.request.PosConsumptionRecordQuery;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 消费记录服务实现类
 */
@Slf4j
@Service
public class PosConsumptionRecordServiceImpl extends JeecgServiceImpl<PosConsumptionRecordMapper, PosConsumptionRecord> implements IPosConsumptionRecordService {

    @Autowired
    private PosConsumptionDetailMapper consumptionDetailMapper;

    
@Override
public PageResult<PosConsumptionRecordVO> list(PosConsumptionRecordQuery query,
                                               PageRequest pageRequest,
                                               Map<String, String[]> queryParam) {
    PosConsumptionRecordQuery actual = Optional.ofNullable(query).orElseGet(PosConsumptionRecordQuery::new);
    long pageNo = pageRequest == null || pageRequest.getPageNo() == null ? 1L : pageRequest.getPageNo();
    long pageSize = pageRequest == null || pageRequest.getPageSize() == null ? 10L : pageRequest.getPageSize();
    Map<String, String[]> params = queryParam == null ? Collections.emptyMap() : queryParam;

    PageResult<PosConsumptionRecordVO> page = pageByQuery(
        actual,
        pageNo,
        pageSize,
        params,
        q -> {
            PosConsumptionRecord entity = new PosConsumptionRecord();
            entity.setCardNo(q.getCardNo());
            entity.setCustomerId(q.getCustomerId());
            entity.setCustomerName(q.getCustomerName());
            entity.setType(q.getType());
            entity.setDeviceCode(q.getDeviceCode());
            entity.setDeviceName(q.getDeviceName());
            entity.setRestaurantCode(q.getRestaurantCode());
            return entity;
        },
        PosConsumptionRecordMapstruct.INSTANCE::toVO,
        qw -> {
            if (actual.getConsumeTimeStart() != null) {
                qw.ge("consume_time", actual.getConsumeTimeStart());
            }
            if (actual.getConsumeTimeEnd() != null) {
                qw.le("consume_time", actual.getConsumeTimeEnd());
            }
            qw.orderByDesc("consume_time");
        }
    );

        if (page.getRecords() != null) {
            page.getRecords().forEach(vo -> {
                if (vo != null && "product".equals(vo.getType())) {
                    try {
                        List<PosConsumptionDetail> details = consumptionDetailMapper.selectByRecordId(vo.getId());
                        if (details != null && !details.isEmpty()) {
                            vo.setDetails(PosConsumptionDetailMapstruct.INSTANCE.toVOList(details));
                        }
                    } catch (Exception e) {
                        log.error("加载消费记录明细失败: {}", vo.getId(), e);
                    }
                }
            });
        }
        return page;
}

@Override
    public PosConsumptionRecordVO getDetailById(String id) {
        try {
            if (id == null || id.trim().isEmpty()) {
                log.warn("查询消费记录详情时ID为空");
                return null;
            }
            
            PosConsumptionRecord entity = this.getById(id);
            if (entity == null) {
                log.warn("未找到消费记录: {}", id);
                return null;
            }
            
            PosConsumptionRecordVO vo = PosConsumptionRecordMapstruct.INSTANCE.toVO(entity);
            if (vo == null) {
                log.error("转换消费记录VO失败: {}", id);
                return null;
            }
            
            // 加载明细
            try {
                List<PosConsumptionDetail> details = consumptionDetailMapper.selectByRecordId(id);
                if (details != null && !details.isEmpty()) {
                    vo.setDetails(PosConsumptionDetailMapstruct.INSTANCE.toVOList(details));
                }
            } catch (Exception e) {
                log.error("加载消费记录明细失败: {}", id, e);
                // 明细加载失败不影响主记录返回
            }
            
            return vo;
        } catch (Exception e) {
            log.error("查询消费记录详情失败: {}", id, e);
            return null;
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteBatchByIds(String[] ids) {
        if (ids == null || ids.length == 0) {
            return true;
        }
        
        // 转换为逗号分隔字符串
        String idsStr = String.join(",", ids);
        
        // 删除消费记录明细
        consumptionDetailMapper.deleteByRecordIds(idsStr);
        
        // 删除消费记录
        return this.removeByIds(Arrays.asList(ids));
    }
    
    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteById(String id) {
        if (StringUtils.isEmpty(id)) {
            log.error("删除失败，id为空");
            return false;
        }
        
        try {
            // 查询消费记录是否存在
            PosConsumptionRecord entity = this.getById(id);
            if (entity == null) {
                log.warn("删除失败，消费记录不存在，id: {}", id);
                return false;
            }
            
            // 删除消费记录明细
            consumptionDetailMapper.deleteByRecordIds(id);
            
            // 删除消费记录
            return super.removeById(id);
        } catch (Exception e) {
            log.error("删除消费记录失败，id: {}", id, e);
            return false;
        }
    }
    
    
}
