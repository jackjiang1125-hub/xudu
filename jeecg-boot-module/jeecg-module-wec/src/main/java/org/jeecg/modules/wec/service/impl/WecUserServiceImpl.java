package org.jeecg.modules.wec.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecgframework.boot.system.api.SystemUserService;
import org.jeecgframework.boot.system.vo.UserLiteVO;
import org.jeecg.modules.wec.entity.WecUser;
import org.jeecg.modules.wec.mapper.WecUserMapper;
import org.jeecg.modules.wec.service.IWecUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jeecg.modules.wec.service.IWecConsumeRecordService;
import org.jeecg.modules.wec.service.IWecDeviceService;

@Service
public class WecUserServiceImpl extends ServiceImpl<WecUserMapper, WecUser> implements IWecUserService {

    @Autowired(required = false)
    private SystemUserService systemUserService;
    
    @Autowired
    private IWecConsumeRecordService wecConsumeRecordService;
    
    @Autowired
    private IWecDeviceService wecDeviceService;

    @Override
    public Map<String, Object> getStatistics(String sysOrgCode) {
        // Use the custom mapper query which aggregates real data
        return baseMapper.getStatistics(sysOrgCode);
    }

    @Override
    @Transactional
    public void importUsers(List<WecUser> users) {
        // Simple batch save for now
        this.saveBatch(users);
    }

    @Override
    @Transactional
    public void addSystemUsers(List<String> userIds, String userType) {
        if (userIds == null || userIds.isEmpty()) return;
        
        List<UserLiteVO> sysUsers = systemUserService.queryUsersByIds(userIds.toArray(new String[0]));
        if (sysUsers == null || sysUsers.isEmpty()) return;

        List<WecUser> wecUsers = new ArrayList<>();
        
        // Get existing WecUsers to avoid duplicates by WorkNo or UserID
        List<WecUser> existingList = this.list();
        List<String> existingUserIds = existingList.stream()
                .map(WecUser::getUserId)
                .collect(Collectors.toList());
        List<String> existingWorkNos = existingList.stream()
                .map(WecUser::getWorkNo)
                .filter(w -> w != null && !w.isEmpty())
                .collect(Collectors.toList());

        for (UserLiteVO sysUser : sysUsers) {
            // Filter by UserID
            if (existingUserIds.contains(sysUser.getId())) {
                continue;
            }
            // Filter by WorkNo (if available)
            if (sysUser.getWorkNo() != null && existingWorkNos.contains(sysUser.getWorkNo())) {
                continue;
            }
            
            WecUser wecUser = new WecUser();
            wecUser.setUserId(sysUser.getId());
            wecUser.setRealName(sysUser.getRealname());
            wecUser.setWorkNo(sysUser.getWorkNo());
            // CardNo might be empty initially
            wecUser.setUserType(userType != null ? userType : "1"); // Default to White List
            wecUser.setStatus("1"); // Normal
            
            wecUsers.add(wecUser);
        }
        
        if (!wecUsers.isEmpty()) {
            this.saveBatch(wecUsers);
        }
    }

    @Override
    public WecUser getUserByCardNo(String cardNo) {
        if (cardNo == null || cardNo.isEmpty()) {
            return null;
        }
        return this.lambdaQuery().eq(WecUser::getCardNo, cardNo).one();
    }

}
