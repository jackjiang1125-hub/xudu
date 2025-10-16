package org.jeecg.modules.acc.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.jeecg.common.system.base.service.JeecgService;
import org.jeecg.modules.acc.entity.AccGroupMember;
import org.jeecg.modules.acc.vo.AccMemberVO;

import java.util.List;

public interface IAccGroupMemberService extends JeecgService<AccGroupMember> {

    /**
     * 根据权限组ID查询成员列表（分页）
     * @param groupId 权限组ID
     * @param pageNo 页码
     * @param pageSize 每页大小
     * @return 成员分页列表
     */
    IPage<AccMemberVO> listMembersByGroupId(String groupId, Integer pageNo, Integer pageSize);

    /**
     * 根据权限组ID查询所有成员ID列表
     * @param groupId 权限组ID
     * @return 成员ID列表
     */
    List<String> listMemberIdsByGroupId(String groupId);
}