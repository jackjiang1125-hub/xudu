package org.jeecg.modules.wec.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.wec.entity.WecConsumeRecord;
import org.jeecg.modules.wec.entity.WecDevice;
import org.jeecg.modules.wec.entity.WecUser;
import org.jeecg.modules.wec.mapper.WecUserMapper;
import org.jeecg.modules.wec.service.IWecConsumeRecordService;
import org.jeecg.modules.wec.service.IWecDeviceService;
import org.jeecgframework.boot.system.api.SystemUserService;
import org.jeecgframework.boot.system.vo.UserLiteVO;
import org.jeecgframework.boot.wec.api.IWecServiceApi;
import org.jeecgframework.boot.wec.vo.WecConsumeRecordDTO;
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

@Service
public class WecIotApiServiceImpl implements IWecServiceApi {

    @Autowired(required = false)
    private SystemUserService systemUserService;
    
    @Autowired
    private IWecConsumeRecordService wecConsumeRecordService;
    
    @Autowired
    private IWecDeviceService wecDeviceService;

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
    public void updateDeviceNamelistMode(String sn, int mode) {
        if (sn == null) return;
        WecDevice device = wecDeviceService.getOne(new QueryWrapper<WecDevice>().eq("sn", sn));
        if (device != null) {
            device.setNamelistMode(mode);
            wecDeviceService.updateById(device);
        }
    }

    @Override
    public void syncDeviceSn(String oldSn, String newSn) {
        if (oldSn == null || newSn == null) return;
        WecDevice device = wecDeviceService.getOne(new QueryWrapper<WecDevice>().eq("sn", oldSn));
        if (device != null) {
            device.setSn(newSn);
            wecDeviceService.updateById(device);
        }
    }

    @Override
    public void updateDeviceIp(String sn, String ip) {
        if (sn == null || ip == null) return;
        // 异步更新或直接更新，这里直接更新
        // 为避免频繁DB操作，可以加个检查：如果IP没变就不更新
        WecDevice device = wecDeviceService.getOne(new QueryWrapper<WecDevice>().eq("sn", sn));
        if (device != null && !ip.equals(device.getIpAddress())) {
            device.setIpAddress(ip);
            wecDeviceService.updateById(device);
        }
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
