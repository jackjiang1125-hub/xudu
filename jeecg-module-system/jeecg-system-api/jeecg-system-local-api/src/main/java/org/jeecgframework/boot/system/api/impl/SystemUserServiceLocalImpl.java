package org.jeecgframework.boot.system.api.impl;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecgframework.boot.system.api.SystemUserService;
import org.jeecgframework.boot.system.vo.UserLiteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 基于本地实现的系统人员查询适配器，实现 jeecg-module-all 的接口
 */
@Component
@ConditionalOnClass(name = "org.jeecg.modules.system.service.impl.SysBaseApiImpl")
public class SystemUserServiceLocalImpl implements SystemUserService {

    @Autowired
    private ISysBaseAPI sysBaseAPI;

    @Override
    public List<UserLiteVO> queryUsersByIds(String[] userIds) {
        List<UserLiteVO> result = new ArrayList<>();
        if (userIds == null || userIds.length == 0) {
            return result;
        }
        String ids = String.join(",", userIds);
        List<JSONObject> users = sysBaseAPI.queryUsersByIds(ids);
        if (users == null) {
            return result;
        }
        for (JSONObject jo : users) {
            if (jo == null) continue;
            UserLiteVO vo = new UserLiteVO()
                    .setId(jo.getString("id"))
                    .setUsername(jo.getString("username"))
                    .setRealname(jo.getString("realname"))
                    .setPhone(jo.getString("phone"))
                    .setOrgCode(jo.getString("orgCode"))
                    .setWorkNo(jo.getString("workNo"))
                    .setAvatar(jo.getString("avatar"))
                    .setFaceCutout(jo.getString("faceCutout"))
                    .setCardNumber(jo.getString("cardNumber"))
                    .setAdminPassword(jo.getString("adminPassword"));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<UserLiteVO> queryUsersByUsernames(String[] usernames) {
        List<UserLiteVO> result = new ArrayList<>();
        if (usernames == null || usernames.length == 0) {
            return result;
        }
        String names = String.join(",", usernames);
        List<JSONObject> users = sysBaseAPI.queryUsersByUsernames(names);
        if (users == null) {
            return result;
        }
        for (JSONObject jo : users) {
            if (jo == null) continue;
            UserLiteVO vo = new UserLiteVO()
                    .setId(jo.getString("id"))
                    .setUsername(jo.getString("username"))
                    .setRealname(jo.getString("realname"))
                    .setPhone(jo.getString("phone"))
                    .setOrgCode(jo.getString("orgCode"))
                    .setWorkNo(jo.getString("workNo"))
                    .setAvatar(jo.getString("avatar"))
                    .setFaceCutout(jo.getString("faceCutout"))
                    .setCardNumber(jo.getString("cardNumber"))
                    .setAdminPassword(jo.getString("adminPassword"));
            result.add(vo);
        }
        return result;
    }

    @Override
    public String getDepartNameByOrgCode(String orgCode) {
        if (orgCode == null || orgCode.trim().isEmpty()) {
            return "";
        }
        List<JSONObject> departs = sysBaseAPI.queryDepartsByOrgcodes(orgCode);
        if (departs == null || departs.isEmpty()) {
            return "";
        }
        JSONObject first = departs.get(0);
        return Objects.toString(first.getString("departName"), "");
    }
}