package org.jeecg.modules.wec.mapper;

import org.jeecg.modules.wec.entity.WecUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import java.util.Map;

public interface WecUserMapper extends BaseMapper<WecUser> {
    
    @Select("<script>" +
            "SELECT " +
            "COUNT(*) as total, " +
            "SUM(CASE WHEN user_type = '1' THEN 1 ELSE 0 END) as white_count, " +
            "SUM(CASE WHEN user_type = '2' THEN 1 ELSE 0 END) as black_count " +
            "FROM wec_user " +
            "<where>" +
            "<if test='sysOrgCode != null and sysOrgCode != \"\"'>" +
            "AND sys_org_code LIKE CONCAT(#{sysOrgCode}, '%')" +
            "</if>" +
            "</where>" +
            "</script>")
    Map<String, Object> getStatistics(@org.apache.ibatis.annotations.Param("sysOrgCode") String sysOrgCode);
}
