package org.jeecg.modules.acc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.modules.acc.entity.AccReader;
import org.jeecg.modules.acc.vo.AccReaderVO;

/**
 * @Description: 读头管理
 * @Author: jeecg-boot
 * @Date: 2025-01-26
 * @Version: V1.0
 */
public interface IAccReaderService extends IService<AccReader> {

    /**
     * 分页查询读头列表
     * @param page 分页参数
     * @param readerId 读头ID
     * @param name 读头名称
     * @param doorName 门名称
     * @return 分页结果
     */
    IPage<AccReader> queryPageList(Page<AccReader> page, String readerId, String name, String doorName);

    /**
     * 保存或更新读头信息（通过VO）
     * @param vo 读头VO对象
     * @return 是否成功
     */
    boolean saveFromVO(AccReaderVO vo);
}