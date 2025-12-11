package org.jeecg.modules.pos.service;

import org.jeecg.modules.pos.vo.PosProductCategoryVO;
import org.jeecg.modules.pos.request.PosProductCategoryQuery;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 商品分类Service接口
 */
public interface IPosProductCategoryService {

    /**
     * 分页查询商品分类
     */
    PageResult<PosProductCategoryVO> list(PosProductCategoryQuery query, PageRequest pageRequest, Map<String, String[]> queryParam);

    /**
     * 根据ID查询详情
     */
    PosProductCategoryVO getDetailById(String id);

    /**
     * 保存商品分类
     */
    PosProductCategoryVO saveVO(PosProductCategoryVO vo, String operator);

    /**
     * 更新商品分类
     */
    PosProductCategoryVO updateVO(PosProductCategoryVO vo, String operator);

    /**
     * 删除商品分类
     */
    boolean deleteById(String id);

    /**
     * 批量删除商品分类
     */
    boolean deleteBatchByIds(String[] ids);

    /**
     * 检查分类编号是否重复
     */
    boolean checkCategoryCodeDuplicate(String code, String excludeId);

    /**
     * 获取所有启用的商品分类列表
     */
    List<PosProductCategoryVO> getEnabledCategoryList();
}
