package org.jeecg.modules.pos.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.pos.entity.PosProductCategory;
import org.jeecg.modules.pos.mapper.PosProductCategoryMapper;
import org.jeecg.modules.pos.mapstruct.PosProductCategoryMapstruct;
import org.jeecg.modules.pos.service.IPosProductCategoryService;
import org.jeecg.modules.pos.vo.PosProductCategoryVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品分类Service实现类
 */
@Slf4j
@Service
public class PosProductCategoryServiceImpl extends JeecgServiceImpl<PosProductCategoryMapper, PosProductCategory> implements IPosProductCategoryService {

    @Override
    public IPage<PosProductCategoryVO> pageList(String categoryName, String categoryCode, String status,
                                              Date createTimeStart, Date createTimeEnd,
                                              Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<PosProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        
        // 分类名称或别名模糊查询
        if (StringUtils.isNotBlank(categoryName)) {
            queryWrapper.and(wrapper -> wrapper.like(PosProductCategory::getCategoryName, categoryName)
                                              .or().like(PosProductCategory::getAlias, categoryName));
        }
        
        // 分类编号模糊查询
        if (StringUtils.isNotBlank(categoryCode)) {
            queryWrapper.like(PosProductCategory::getCategoryCode, categoryCode);
        }
        
        // 状态查询
        if (StringUtils.isNotBlank(status)) {
            queryWrapper.eq(PosProductCategory::getStatus, status);
        }
        
        // 创建时间范围查询
        if (createTimeStart != null) {
            queryWrapper.ge(PosProductCategory::getCreateTime, createTimeStart);
        }
        if (createTimeEnd != null) {
            queryWrapper.le(PosProductCategory::getCreateTime, createTimeEnd);
        }
        
        // 按排序号和创建时间排序
        queryWrapper.orderByAsc(PosProductCategory::getDisplayOrder)
                    .orderByDesc(PosProductCategory::getCreateTime);
        
        // 分页查询
        Page<PosProductCategory> page = new Page<>(pageNo, pageSize);
        IPage<PosProductCategory> pageResult = this.page(page, queryWrapper);
        
        // 转换为VO
        return pageResult.convert(entity -> PosProductCategoryMapstruct.INSTANCE.toVO(entity));
    }

    @Override
    public PosProductCategoryVO getDetailById(String id) {
        PosProductCategory entity = this.getById(id);
        if (entity == null) {
            return null;
        }
        return PosProductCategoryMapstruct.INSTANCE.toVO(entity);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PosProductCategoryVO saveVO(PosProductCategoryVO vo, String operator) {
        // 检查分类编号是否重复
        if (checkCategoryCodeDuplicate(vo.getCategoryCode(), null)) {
            throw new RuntimeException("分类编号已存在");
        }
        
        // 转换为实体并保存
        PosProductCategory entity = PosProductCategoryMapstruct.INSTANCE.toEntity(vo);
        entity.setCreateBy(operator);
        entity.setUpdateBy(operator);
        
        this.save(entity);
        
        // 返回保存后的VO
        return this.getDetailById(entity.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public PosProductCategoryVO updateVO(PosProductCategoryVO vo, String operator) {
        // 检查分类编号是否重复（排除当前记录）
        if (checkCategoryCodeDuplicate(vo.getCategoryCode(), vo.getId())) {
            throw new RuntimeException("分类编号已存在");
        }
        
        // 检查记录是否存在
        PosProductCategory entity = this.getById(vo.getId());
        if (entity == null) {
            throw new RuntimeException("商品分类不存在");
        }
        
        // 更新字段
        entity.setCategoryCode(vo.getCategoryCode());
        entity.setCategoryName(vo.getCategoryName());
        entity.setAlias(vo.getAlias());
        entity.setDescription(vo.getDescription());
        entity.setStatus(vo.getStatus());
        entity.setDisplayOrder(vo.getDisplayOrder());
        entity.setRemark(vo.getRemark());
        entity.setUpdateBy(operator);
        
        this.updateById(entity);
        
        // 返回更新后的VO
        return this.getDetailById(entity.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteById(String id) {
        // TODO: 可以在这里添加删除前的业务校验，例如检查是否有商品关联此分类
        return this.removeById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean deleteBatchByIds(String[] ids) {
        // TODO: 可以在这里添加批量删除前的业务校验
        return this.removeByIds(java.util.Arrays.asList(ids));
    }

    @Override
    public boolean checkCategoryCodeDuplicate(String code, String excludeId) {
        LambdaQueryWrapper<PosProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PosProductCategory::getCategoryCode, code);
        
        if (StringUtils.isNotBlank(excludeId)) {
            queryWrapper.ne(PosProductCategory::getId, excludeId);
        }
        
        return this.count(queryWrapper) > 0;
    }

    @Override
    public List<PosProductCategoryVO> getEnabledCategoryList() {
        LambdaQueryWrapper<PosProductCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(PosProductCategory::getStatus, "enabled")
                    .orderByAsc(PosProductCategory::getDisplayOrder)
                    .orderByDesc(PosProductCategory::getCreateTime);
        
        List<PosProductCategory> list = this.list(queryWrapper);
        return list.stream()
                .map(entity -> PosProductCategoryMapstruct.INSTANCE.toVO(entity))
                .collect(Collectors.toList());
    }
}