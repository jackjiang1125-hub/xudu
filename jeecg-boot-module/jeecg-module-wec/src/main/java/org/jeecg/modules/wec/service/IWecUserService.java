package org.jeecg.modules.wec.service;

import org.jeecg.modules.wec.entity.WecUser;
import com.baomidou.mybatisplus.extension.service.IService;
import java.util.Map;
import java.util.List;

public interface IWecUserService extends IService<WecUser> {
    Map<String, Object> getStatistics(String sysOrgCode);
    void importUsers(List<WecUser> users);
    void addSystemUsers(List<String> userIds, String userType);
}
