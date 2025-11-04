package org.jeecg.modules.pos.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import org.jeecg.common.api.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 消费记录服务实现类
 */
@Slf4j
@Service
public class PosConsumptionRecordServiceImpl extends JeecgServiceImpl<PosConsumptionRecordMapper, PosConsumptionRecord> implements IPosConsumptionRecordService {

    @Autowired
    private PosConsumptionDetailMapper consumptionDetailMapper;

    @Override
    public IPage<PosConsumptionRecordVO> pageList(String cardNo, String customerId, String customerName, String type,
                                               String deviceCode, String deviceName, String restaurantCode,
                                               Date consumeTimeStart, Date consumeTimeEnd,
                                               Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<PosConsumptionRecord> queryWrapper = new LambdaQueryWrapper<>();
        
        // 卡号模糊查询
        if (StringUtils.isNotBlank(cardNo)) {
            queryWrapper.like(PosConsumptionRecord::getCardNo, cardNo);
        }
        
        // 人员编号模糊查询
        if (StringUtils.isNotBlank(customerId)) {
            queryWrapper.like(PosConsumptionRecord::getCustomerId, customerId);
        }
        
        // 人员姓名模糊查询
        if (StringUtils.isNotBlank(customerName)) {
            queryWrapper.like(PosConsumptionRecord::getCustomerName, customerName);
        }
        
        // 消费类型查询
        if (StringUtils.isNotBlank(type)) {
            queryWrapper.eq(PosConsumptionRecord::getType, type);
        }
        
        // 设备序列号模糊查询
        if (StringUtils.isNotBlank(deviceCode)) {
            queryWrapper.like(PosConsumptionRecord::getDeviceCode, deviceCode);
        }
        
        // 设备名称模糊查询
        if (StringUtils.isNotBlank(deviceName)) {
            queryWrapper.like(PosConsumptionRecord::getDeviceName, deviceName);
        }
        
        // 餐厅编码模糊查询
        if (StringUtils.isNotBlank(restaurantCode)) {
            queryWrapper.like(PosConsumptionRecord::getRestaurantCode, restaurantCode);
        }
        
        // 消费时间范围查询
        if (consumeTimeStart != null) {
            queryWrapper.ge(PosConsumptionRecord::getConsumeTime, consumeTimeStart);
        }
        if (consumeTimeEnd != null) {
            queryWrapper.le(PosConsumptionRecord::getConsumeTime, consumeTimeEnd);
        }
        
        // 按消费时间降序排序
        queryWrapper.orderByDesc(PosConsumptionRecord::getConsumeTime);
        
        // 分页查询
        Page<PosConsumptionRecord> page = new Page<>(pageNo, pageSize);
        IPage<PosConsumptionRecord> pageResult = this.page(page, queryWrapper);
        
        // 转换为VO
        try {
            return pageResult.convert(entity -> {
                PosConsumptionRecordVO vo = PosConsumptionRecordMapstruct.INSTANCE.toVO(entity);
                // 对于商品类型的消费记录，加载明细
                if (entity != null && "product".equals(entity.getType())) {
                    try {
                        List<PosConsumptionDetail> details = consumptionDetailMapper.selectByRecordId(entity.getId());
                        if (details != null && !details.isEmpty()) {
                            vo.setDetails(PosConsumptionDetailMapstruct.INSTANCE.toVOList(details));
                        }
                    } catch (Exception e) {
                        log.error("加载消费记录明细失败: {}", entity.getId(), e);
                        // 明细加载失败不影响主记录展示
                    }
                }
                return vo;
            });
        } catch (Exception e) {
            log.error("转换消费记录VO失败", e);
            // 如果转换失败，返回空数据
            Page<PosConsumptionRecordVO> emptyPage = new Page<>(pageNo, pageSize);
            emptyPage.setTotal(0);
            return emptyPage;
        }
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