package org.jeecgframework.boot.system.api;

import org.jeecgframework.boot.system.vo.UserLiteVO;

import java.util.List;

/**
 * 系统人员信息查询服务接口（jeecg-module-all 定义）
 * 由 jeecg-module-system 模块提供具体实现。
 */
public interface SystemUserService {

    /**
     * 根据用户ID批量查询人员基础信息
     * @param userIds 用户ID数组
     * @return 人员信息列表（含 id、username、realname、phone、orgCode）
     */
    List<UserLiteVO> queryUsersByIds(String[] userIds);

    /**
     * 根据用户名批量查询人员基础信息
     * @param usernames 用户名数组
     * @return 人员信息列表（含 id、username、realname、phone、orgCode）
     */
    List<UserLiteVO> queryUsersByUsernames(String[] usernames);

    /**
     * 根据组织编码查询部门名称
     * @param orgCode 组织编码
     * @return 部门名称（不存在则返回空字符串）
     */
    String getDepartNameByOrgCode(String orgCode);
}