package org.jeecg.modules.pos.service;

import org.jeecg.modules.pos.vo.PosRestaurantVO;
import org.jeecg.modules.pos.request.PosRestaurantQuery;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;

import java.util.List;
import java.util.Map;

/**
 * 餐厅信息服务接口
 */
public interface IPosRestaurantService {

    /**
     * 分页查询餐厅信息
     */
    PageResult<PosRestaurantVO> list(PosRestaurantQuery query, PageRequest pageRequest, Map<String, String[]> queryParam);

    /**
     * 根据ID查询餐厅详情
     */
    PosRestaurantVO getDetailById(String id);

    /**
     * 保存餐厅信息
     */
    PosRestaurantVO saveVO(PosRestaurantVO vo, String operator);

    /**
     * 更新餐厅信息
     */
    PosRestaurantVO updateVO(PosRestaurantVO vo, String operator);

    /**
     * 删除餐厅
     */
    boolean deleteById(String id);

    /**
     * 批量删除餐厅
     */
    boolean deleteBatchByIds(String[] ids);

    /**
     * 检查餐厅编码是否重复
     */
    boolean checkRestaurantCodeDuplicate(String code, String excludeId);

    /**
     * 获取所有餐厅列表
     */
    List<PosRestaurantVO> getAllRestaurantList();
}
