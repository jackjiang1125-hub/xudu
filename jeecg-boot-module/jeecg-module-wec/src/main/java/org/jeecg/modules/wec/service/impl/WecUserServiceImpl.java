package org.jeecg.modules.wec.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecgframework.boot.system.api.SystemUserService;
import org.jeecgframework.boot.system.vo.UserLiteVO;
import org.jeecg.modules.wec.entity.WecUser;
import org.jeecg.modules.wec.mapper.WecUserMapper;
import org.jeecg.modules.wec.service.IWecUserService;
import org.jeecgframework.boot.wec.api.IWecUserServiceApi;
import org.jeecgframework.boot.wec.vo.WecUserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jeecg.modules.wec.entity.WecConsumeRecord;
import org.jeecg.modules.wec.entity.WecDevice;
import org.jeecg.modules.wec.service.IWecConsumeRecordService;
import org.jeecg.modules.wec.service.IWecDeviceService;
import org.jeecgframework.boot.wec.vo.WecConsumeRecordDTO;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

@Service
public class WecUserServiceImpl extends ServiceImpl<WecUserMapper, WecUser> implements IWecUserService, IWecUserServiceApi {

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
    public void saveConsumeRecord(WecConsumeRecordDTO recordDTO) {
        if (recordDTO == null) return;
        
        WecConsumeRecord record = new WecConsumeRecord();
        BeanUtils.copyProperties(recordDTO, record);
        
        // Try to fill in user details if missing
        if ((record.getUserName() == null || record.getUserId() == null) && record.getCardNo() != null) {
            WecUserVO user = getUserVoByCardNo(record.getCardNo());
            if (user != null) {
                if (record.getUserName() == null) record.setUserName(user.getRealName());
                if (record.getUserId() == null) record.setUserId(user.getUserId());
            }
        }

        // Fill Device Name if missing and we have device ID
        if (record.getDeviceName() == null && record.getDeviceId() != null) {
             try {
                 // First try by ID
                 WecDevice device = wecDeviceService.getById(record.getDeviceId());
                 if (device == null) {
                     // Try by SN (assuming deviceId might be SN passed from IoT)
                     device = wecDeviceService.getOne(new QueryWrapper<WecDevice>().eq("sn", record.getDeviceId()));
                 }
                 
                 if (device != null) {
                     record.setDeviceName(device.getDeviceName());
                     record.setDeviceId(device.getId()); // Update to system ID
                 } else {
                     record.setDeviceName("未命名设备(" + record.getDeviceId() + ")");
                 }
             } catch (Exception e) {
                 // ignore
             }
        }
        
        wecConsumeRecordService.save(record);
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

    @Override
    public WecUserVO getUserVoByCardNo(String cardNo) {
        // 现根据卡号去SysUser获取人员信息，暂时先不调用WecUser，余额固定写成66
        if (systemUserService == null) {
             return null;
        }
        UserLiteVO sysUser = systemUserService.getUserByCardNo(cardNo);
        if (sysUser == null) {
            return null;
        }
        
        WecUserVO vo = new WecUserVO();
        // Use userId from SysUser
        vo.setUserId(sysUser.getId());
        vo.setRealName(sysUser.getRealname());
        vo.setWorkNo(sysUser.getWorkNo());
        vo.setCardNo(cardNo);
        
        // Default values as requested
        vo.setBalance(new BigDecimal("66"));
        vo.setUserType("1"); // 1:White List
        vo.setStatus("1");   // 1:Normal
        
        return vo;
    }
}
