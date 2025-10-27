package org.jeecg.modules.pos.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.pos.entity.PosRestaurant;
import org.jeecg.modules.pos.mapper.PosRestaurantMapper;
import org.jeecg.modules.pos.mapstruct.PosRestaurantMapstruct;
import org.jeecg.modules.pos.service.IPosRestaurantService;
import org.jeecg.modules.pos.vo.PosRestaurantVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 餐厅信息服务实现类
 */
@Slf4j
@Service
public class PosRestaurantServiceImpl extends JeecgServiceImpl<PosRestaurantMapper, PosRestaurant> implements IPosRestaurantService {

    @Override
    public IPage<PosRestaurantVO> pageList(String restaurantName, String restaurantCode, String category,
                                         String diningServiceType, Date createTimeStart, Date createTimeEnd,
                                         Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<PosRestaurant> queryWrapper = new LambdaQueryWrapper<>();
        
        // 餐厅名称模糊查询
        if (StringUtils.isNotBlank(restaurantName)) {
            queryWrapper.like(PosRestaurant::getRestaurantName, restaurantName);
        }
        
        // 餐厅编码模糊查询
        if (StringUtils.isNotBlank(restaurantCode)) {
            queryWrapper.like(PosRestaurant::getRestaurantCode, restaurantCode);
        }
        
        // 经营模式查询
        if (StringUtils.isNotBlank(category)) {
            queryWrapper.eq(PosRestaurant::getCategory, category);
        }
        
        // 餐厅类型查询
        if (StringUtils.isNotBlank(diningServiceType)) {
            queryWrapper.eq(PosRestaurant::getDiningServiceType, diningServiceType);
        }
        
        // 创建时间范围查询
        if (createTimeStart != null) {
            queryWrapper.ge(PosRestaurant::getCreateTime, createTimeStart);
        }
        if (createTimeEnd != null) {
            queryWrapper.le(PosRestaurant::getCreateTime, createTimeEnd);
        }
        
        // 按创建时间排序
        queryWrapper.orderByDesc(PosRestaurant::getCreateTime);
        
        // 分页查询
        Page<PosRestaurant> page = new Page<>(pageNo, pageSize);
        IPage<PosRestaurant> pageResult = this.page(page, queryWrapper);
        
        // 转换为VO
        return pageResult.convert(entity -> PosRestaurantMapstruct.INSTANCE.toVO(entity));
    }

    @Override
    public PosRestaurantVO getDetailById(String id) {
        PosRestaurant entity = this.getById(id);
        if (entity == null) {
            return null;
        }
        return PosRestaurantMapstruct.INSTANCE.toVO(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PosRestaurantVO saveVO(PosRestaurantVO vo, String operator) {
        // 检查餐厅编码是否重复
        if (checkRestaurantCodeDuplicate(vo.getRestaurantCode(), null)) {
            throw new RuntimeException("餐厅编码已存在");
        }
        
        // 转换为实体并保存
        PosRestaurant entity = PosRestaurantMapstruct.INSTANCE.toEntity(vo);
        entity.setCreateBy(operator);
        entity.setUpdateBy(operator);
        
        this.save(entity);
        
        // 返回保存后的VO
        return this.getDetailById(entity.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PosRestaurantVO updateVO(PosRestaurantVO vo, String operator) {
        // 检查餐厅编码是否重复（排除当前记录）
        if (checkRestaurantCodeDuplicate(vo.getRestaurantCode(), vo.getId())) {
            throw new RuntimeException("餐厅编码已存在");
        }
        
        // 检查记录是否存在
        PosRestaurant entity = this.getById(vo.getId());
        if (entity == null) {
            throw new RuntimeException("餐厅不存在");
        }
        
        // 更新字段
        entity.setRestaurantCode(vo.getRestaurantCode());
        entity.setRestaurantName(vo.getRestaurantName());
        entity.setCategory(vo.getCategory());
        entity.setDiningServiceType(vo.getDiningServiceType());
        entity.setRemark(vo.getRemark());
        entity.setUpdateBy(operator);
        
        this.updateById(entity);
        
        // 返回更新后的VO
        return this.getDetailById(entity.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteById(String id) {
        // TODO: 可以在这里添加删除前的业务校验，例如检查是否有关联的业务数据
        return this.removeById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteBatchByIds(String[] ids) {
        // TODO: 可以在这里添加批量删除前的业务校验
        return this.removeByIds(java.util.Arrays.asList(ids));
    }

    @Override
    public boolean checkRestaurantCodeDuplicate(String code, String excludeId) {
        LambdaQueryWrapper<PosRestaurant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PosRestaurant::getRestaurantCode, code);
        if (StringUtils.isNotBlank(excludeId)) {
            queryWrapper.ne(PosRestaurant::getId, excludeId);
        }
        return this.count(queryWrapper) > 0;
    }

    @Override
    public List<PosRestaurantVO> getAllRestaurantList() {
        LambdaQueryWrapper<PosRestaurant> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(PosRestaurant::getCreateTime);
        
        List<PosRestaurant> list = this.list(queryWrapper);
        return list.stream()
                .map(entity -> PosRestaurantMapstruct.INSTANCE.toVO(entity))
                .collect(Collectors.toList());
    }
}