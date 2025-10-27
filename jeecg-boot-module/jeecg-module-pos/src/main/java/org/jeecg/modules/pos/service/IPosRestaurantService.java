package org.jeecg.modules.pos.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.pos.entity.PosRestaurant;
import org.jeecg.modules.pos.vo.PosRestaurantVO;

import java.util.Date;
import java.util.List;

/**
 * 餐厅信息服务接口
 */
public interface IPosRestaurantService extends JeecgService<PosRestaurant> {

    /**
     * 分页查询餐厅信息
     */
    IPage<PosRestaurantVO> pageList(String restaurantName, String restaurantCode, String category,
                                  String diningServiceType, Date createTimeStart, Date createTimeEnd,
                                  Integer pageNo, Integer pageSize);

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