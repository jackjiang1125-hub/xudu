package org.jeecgframework.boot.acc.api;

import org.jeecgframework.boot.acc.vo.AccUserLiteVO;

/**
 * 门禁人员服务接口
 * @author system
 * @date 2025-01-03
 */
public interface AccSysUserService {

    /**
     * 更新系统用户
     * @param AccUserLiteVO 
     */
    void updateSysUser(AccUserLiteVO userLiteVO);

}
