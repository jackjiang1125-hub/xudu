package org.jeecg.common.system.base.service.impl;



import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.jeecg.common.system.base.entity.JeecgEntity;
import org.jeecg.common.system.base.service.JeecgService;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecgframework.boot.common.vo.PageResult;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * @Description: ServiceImpl基类
 * @Author: dangzhenghui@163.com
 * @Date: 2019-4-21 8:13
 * @Version: 1.0
 */
@Slf4j
public class JeecgServiceImpl<M extends BaseMapper<T>, T extends JeecgEntity>
        extends ServiceImpl<M, T> implements JeecgService<T> {

    /** IPage<S> -> PageResult<R>（公用转换） */
    protected <S, R> PageResult<R> toPageResult(IPage<S> page, Function<? super S, ? extends R> mapper) {
        // JDK8
        List<R> list = page.getRecords().stream().map(mapper).collect(Collectors.toList());
        return PageResult.of(page.getTotal(), page.getCurrent(), page.getSize(), list);
    }

    /**
     * 通用：DTO 查询 + 动态参数 + 分页 -> PageResult<VO>
     *
     * @param queryDto       控制器/上层传入的查询 DTO
     * @param pageNo         页码
     * @param pageSize       每页大小
     * @param rawParams      直接传 HttpServletRequest#getParameterMap()（可为 null）
     * @param dtoToEntity    DTO -> Entity（MapStruct 方法引用）
     * @param entityToVo     Entity -> VO（MapStruct 方法引用）
     * @param wrapperTuner   可选，自定义补充条件/排序（例如 qw.orderByDesc("create_time")）
     */
    protected <Q, R> PageResult<R> pageByQuery(Q queryDto,
                                               long pageNo,
                                               long pageSize,
                                               Map<String, String[]> rawParams,
                                               Function<? super Q, ? extends T> dtoToEntity,
                                               Function<? super T, ? extends R> entityToVo,
                                               Consumer<QueryWrapper<T>> wrapperTuner) {
        T probe = dtoToEntity.apply(queryDto);

        // 可选：做一层白名单过滤，防止不该进来的参数（如有需要可 override allowedFields 或传 null 跳过）
        Map<String, String[]> safe = filterParams(rawParams, allowedFields());

        QueryWrapper<T> qw = QueryGenerator.initQueryWrapper(probe, safe);
        if (wrapperTuner != null) {
            wrapperTuner.accept(qw);
        }

        IPage<T> page = page(new Page<>(pageNo, pageSize), qw);
        return toPageResult(page, entityToVo);
    }

    /** 便捷重载：不需要自定义排序/条件时用 */
    protected <Q, R> PageResult<R> pageByQuery(Q queryDto,
                                               long pageNo,
                                               long pageSize,
                                               Map<String, String[]> rawParams,
                                               Function<? super Q, ? extends T> dtoToEntity,
                                               Function<? super T, ? extends R> entityToVo) {
        return pageByQuery(queryDto, pageNo, pageSize, rawParams, dtoToEntity, entityToVo, null);
    }

    /** 允许的查询字段白名单（逻辑名，非列名）；子类可覆盖返回 Set.of("sn","deviceName",...)。返回空表示不做过滤。 */
    protected Set<String> allowedFields() {
        return Collections.emptySet();
    }

    /** 过滤分页/排序等公共参数，并按白名单过滤字段（支持 _like/_begin/_end/_ge/_le/_gt/_lt/_in 后缀） */
    private Map<String, String[]> filterParams(Map<String, String[]> in, Set<String> allow) {
        if (in == null || in.isEmpty() || allow == null || allow.isEmpty()) return in == null ? Collections.emptyMap() : in;
        Map<String, String[]> out = new HashMap<>();
        for (Map.Entry<String, String[]> e : in.entrySet()) {
            String k = e.getKey();
            if (isPagingOrSort(k)) continue;
            String logical = stripSuffix(k);
            if (allow.contains(logical)) out.put(k, e.getValue());
        }
        return out;
    }
    private boolean isPagingOrSort(String k) {
        return "pageNo".equals(k) || "pageSize".equals(k) || "column".equals(k) || "order".equals(k);
    }
    private String stripSuffix(String k) {
        for (String s : new String[]{"_like","_begin","_end","_ge","_le","_gt","_lt","_in"}) {
            if (k.endsWith(s)) return k.substring(0, k.length() - s.length());
        }
        return k;
    }
}

