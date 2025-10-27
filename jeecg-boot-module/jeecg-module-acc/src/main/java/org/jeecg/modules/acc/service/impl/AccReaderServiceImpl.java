package org.jeecg.modules.acc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.common.util.oConvertUtils;
import org.jeecg.modules.acc.entity.AccReader;
import org.jeecg.modules.acc.mapper.AccReaderMapper;
import org.jeecg.modules.acc.mapstruct.AccReaderMapstruct;
import org.jeecg.modules.acc.service.IAccReaderService;
import org.jeecg.modules.acc.vo.AccReaderVO;
import org.springframework.stereotype.Service;
import org.apache.commons.lang3.StringUtils;

/**
 * @Description: 读头管理
 * @Author: jeecg-boot
 * @Date: 2025-01-26
 * @Version: V1.0
 */
@Service
public class AccReaderServiceImpl extends ServiceImpl<AccReaderMapper, AccReader> implements IAccReaderService {

    @Override
    public IPage<AccReader> queryPageList(Page<AccReader> page, String readerId, String name, String doorName) {
        LambdaQueryWrapper<AccReader> queryWrapper = new LambdaQueryWrapper<>();
        
        // 根据读头名称查询
        if (oConvertUtils.isNotEmpty(name)) {
            queryWrapper.like(AccReader::getName, name);
        }
        
        // 根据门名称查询
        if (oConvertUtils.isNotEmpty(doorName)) {
            queryWrapper.like(AccReader::getDoorName, doorName);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(AccReader::getCreateTime);
        
        return this.page(page, queryWrapper);
    }

    @Override
    public boolean saveFromVO(AccReaderVO vo) {
        AccReader entity = AccReaderMapstruct.INSTANCE.toEntity(vo);
        
        if (oConvertUtils.isEmpty(entity.getId())) {
            // 新增
            return this.save(entity);
        } else {
            // 更新
            return this.updateById(entity);
        }
    }

    @Override
    public void removeByDeviceSn(String deviceSn) {
        if (deviceSn == null) {
            return;
        }
        this.remove(new LambdaQueryWrapper<AccReader>().eq(AccReader::getDeviceSn, deviceSn));
    }
}