package org.jeecg.modules.acc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.acc.entity.AccGroupMember;

/**
 * 权限组-人员关联Mapper
 */
@Mapper
public interface AccGroupMemberMapper extends BaseMapper<AccGroupMember> {
}