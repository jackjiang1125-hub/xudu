package org.jeecg.modules.acc.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface SysDepartLiteMapper {

    @Select("SELECT depart_name FROM sys_depart WHERE org_code = #{orgCode} LIMIT 1")
    String getDepartNameByOrgCode(String orgCode);
}