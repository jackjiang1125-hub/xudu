package org.jeecg.modules.pos.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.modules.pos.entity.PosProductCategory;
import org.jeecg.modules.pos.mapper.PosProductCategoryMapper;
import org.jeecg.modules.pos.mapstruct.PosProductCategoryMapstruct;
import org.jeecg.modules.pos.service.IPosProductCategoryService;
import org.jeecg.modules.pos.vo.PosProductCategoryVO;
import org.jeecg.modules.pos.request.PosProductCategoryQuery;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 商品分类Service实现类
 */
@Slf4j
@Service
public class PosProductCategoryServiceImpl extends JeecgServiceImpl<PosProductCategoryMapper, PosProductCategory> implements IPosProductCategoryService {

    @Override
    public PageResult<PosProductCategoryVO> list(PosProductCategoryQuery query,
                                                 PageRequest pageRequest,
                                                 Map<String, String[]> queryParam) {
        PosProductCategoryQuery actual = Optional.ofNullable(query).orElseGet(PosProductCategoryQuery::new);
        long pageNo = pageRequest == null || pageRequest.getPageNo() == null ? 1L : pageRequest.getPageNo();
        long pageSize = pageRequest == null || pageRequest.getPageSize() == null ? 10L : pageRequest.getPageSize();
        Map<String, String[]> params = queryParam == null ? Map.of() : queryParam;

        return pageByQuery(
            actual,
            pageNo,
            pageSize,
            params,
            q -> {
                PosProductCategory entity = new PosProductCategory();
                entity.setCategoryName(q.getCategoryName());
                entity.setCategoryCode(q.getCategoryCode());
                entity.setStatus(q.getStatus());
                return entity;
            },
            PosProductCategoryMapstruct.INSTANCE::toVO,
            qw -> {
                if (actual.getCreateTimeStart() != null) {
                    qw.ge("create_time", actual.getCreateTimeStart());
                }
                if (actual.getCreateTimeEnd() != null) {
                    qw.le("create_time", actual.getCreateTimeEnd());
                }
                qw.orderByAsc("display_order").orderByDesc("create_time");
            }
        );
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
