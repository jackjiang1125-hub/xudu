package org.jeecg.modules.acc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.acc.entity.AccGroup;
import org.jeecg.modules.acc.vo.AccGroupVO;

public interface IAccGroupService extends JeecgService<AccGroup> {

    IPage<AccGroupVO> pageList(String groupName, Integer memberCount, Integer deviceCount,
                               Integer pageNo, Integer pageSize);

    AccGroupVO getDetailById(String id);

    AccGroupVO saveVO(AccGroupVO vo, String operator);

    AccGroupVO updateVO(AccGroupVO vo, String operator);

    /**
     * 仅更新权限组基础信息（名称、时间段、备注），不改动成员/设备关联
     */
    AccGroupVO updateBaseInfo(AccGroupVO vo, String operator);

    boolean deleteWithRelations(String id);

    boolean deleteBatchWithRelations(String[] ids);
}