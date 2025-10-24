package org.jeecg.modules.pos.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.pos.entity.PosProductCategory;
import org.jeecg.modules.pos.vo.PosProductCategoryVO;

import java.util.Date;
import java.util.List;

/**
 * 商品分类Service接口
 */
public interface IPosProductCategoryService extends JeecgService<PosProductCategory> {

    /**
     * 分页查询商品分类
     */
    IPage<PosProductCategoryVO> pageList(String categoryName, String categoryCode, String status,
                                        Date createTimeStart, Date createTimeEnd,
                                        Integer pageNo, Integer pageSize);

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