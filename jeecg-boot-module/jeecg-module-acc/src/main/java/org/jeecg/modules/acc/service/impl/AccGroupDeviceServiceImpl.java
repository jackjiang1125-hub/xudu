package org.jeecg.modules.acc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.acc.entity.AccDevice;
import org.jeecg.modules.acc.entity.AccGroupMember;
import org.jeecg.modules.acc.entity.AccGroupDevice;
import org.jeecg.modules.acc.mapper.AccDeviceMapper;
import org.jeecg.modules.acc.mapper.AccGroupDeviceMapper;
import org.jeecg.modules.acc.mapper.AccGroupMemberMapper;
import org.jeecg.modules.acc.service.iot.AccIoTDispatchService;
import org.jeecg.modules.acc.service.IAccGroupDeviceService;
import org.jeecg.modules.acc.vo.AccDeviceVO;
import org.jeecgframework.boot.system.vo.UserLiteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AccGroupDeviceServiceImpl extends ServiceImpl<AccGroupDeviceMapper, AccGroupDevice> implements IAccGroupDeviceService {

    @Autowired
    private AccDeviceMapper accDeviceMapper;

    @Autowired
    private AccGroupMemberMapper accGroupMemberMapper;

    @Autowired
    private AccIoTDispatchService accIoTDispatchService;

    @Override
    public IPage<AccDeviceVO> listDevicesByGroupId(String groupId, Integer pageNo, Integer pageSize) {
        // 查询关联的设备ID列表
        List<String> deviceIds = listDeviceIdsByGroupId(groupId);
        
        // 根据设备ID批量查询设备详情
        List<AccDeviceVO> deviceVOs = new ArrayList<>();
        if (!deviceIds.isEmpty()) {
            List<AccDevice> devices = accDeviceMapper.selectBatchIds(deviceIds);
            for (AccDevice d : devices) {
                AccDeviceVO vo = new AccDeviceVO();
                vo.setId(d.getId());
                vo.setDeviceName(d.getDeviceName());
                // 同步序列号与设备编码
                vo.setSn(d.getSn());
                vo.setDeviceCode(d.getSn());
                vo.setDeviceType(d.getDeviceType());
                vo.setIpAddress(d.getIpAddress());
                vo.setStatus(d.getStatus());
                // 前端展示授权状态：已授权/未授权
                String authText = (d.getAuthorized() != null && d.getAuthorized()) ? "已授权" : "未授权";
                vo.setAuthorized(authText);
                // 位置字段暂缺实体映射，如有请补充；默认空
                vo.setLocation(null);
                deviceVOs.add(vo);
            }
        }
        
        // 内存分页
        int total = deviceVOs.size();
        int page = Math.max(1, pageNo);
        int size = Math.max(1, pageSize);
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        List<AccDeviceVO> pageRecords = start >= total ? new ArrayList<>() : deviceVOs.subList(start, end);

        Page<AccDeviceVO> result = new Page<>(page, size, total);
        result.setRecords(pageRecords);
        return result;
    }

    @Override
    public List<String> listDeviceIdsByGroupId(String groupId) {
        QueryWrapper<AccGroupDevice> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", groupId);
        queryWrapper.select("device_id");
        
        List<AccGroupDevice> groupDevices = this.list(queryWrapper);
        return groupDevices.stream()
                .map(AccGroupDevice::getDeviceId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addDevices(String groupId, List<String> deviceIds) {
        if (groupId == null || deviceIds == null || deviceIds.isEmpty()) return;

        // 查询已存在的设备，避免重复
        QueryWrapper<AccGroupDevice> qw = new QueryWrapper<>();
        qw.eq("group_id", groupId).in("device_id", deviceIds);
        List<AccGroupDevice> exists = this.list(qw);
        java.util.Set<String> existedIds = exists.stream().map(AccGroupDevice::getDeviceId).collect(java.util.stream.Collectors.toSet());

        List<AccGroupDevice> toSave = deviceIds.stream()
                .filter(id -> id != null && !id.trim().isEmpty())
                .filter(id -> !existedIds.contains(id))
                .map(id -> {
                    AccGroupDevice gd = new AccGroupDevice();
                    gd.setGroupId(groupId);
                    gd.setDeviceId(id);
                    return gd;
                })
                .collect(java.util.stream.Collectors.toList());

        if (!toSave.isEmpty()) {
            this.saveBatch(toSave);
            try {
                List<String> newDeviceIds = toSave.stream().map(AccGroupDevice::getDeviceId).collect(java.util.stream.Collectors.toList());
                org.slf4j.LoggerFactory.getLogger(AccGroupDeviceServiceImpl.class).info("[ACC] 设备新增关系完成，准备下发成员同步 groupId={}, addCount={}", groupId, newDeviceIds.size());
                // 下发：设备新增后同步组内成员到这些设备（每成员4条新增命令）
                accIoTDispatchService.syncGroupMembersToDevices(groupId, newDeviceIds);
            } catch (Exception e) {
                org.slf4j.LoggerFactory.getLogger(AccGroupDeviceServiceImpl.class).warn("[ACC] 设备新增后成员同步下发失败 groupId={}, err={} ", groupId, e.getMessage());
                // 下发失败不影响关系保存
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeDevices(String groupId, List<String> deviceIds) {
        if (groupId == null || deviceIds == null || deviceIds.isEmpty()) return;
        QueryWrapper<AccGroupDevice> qw = new QueryWrapper<>();
        qw.eq("group_id", groupId).in("device_id", deviceIds);
        this.remove(qw);
        try {
            org.slf4j.LoggerFactory.getLogger(AccGroupDeviceServiceImpl.class).info("[ACC] 设备关系删除完成，准备下发移除成员 groupId={}, removeCount={}", groupId, deviceIds.size());
            // 下发：从这些设备移除组内所有成员（每成员4条删除命令）
            accIoTDispatchService.removeGroupMembersFromDevices(groupId, deviceIds);
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AccGroupDeviceServiceImpl.class).warn("[ACC] 设备移除成员下发失败 groupId={}, err={} ", groupId, e.getMessage());
            // 下发失败不影响关系删除
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeByDeviceId(String deviceId) {
        if (deviceId == null || deviceId.trim().isEmpty()) return;
        // 查询该设备关联的组ID，便于下发删除
        List<AccGroupDevice> relations = this.list(new QueryWrapper<AccGroupDevice>().eq("device_id", deviceId));
        java.util.Set<String> groupIds = relations.stream().map(AccGroupDevice::getGroupId).collect(java.util.stream.Collectors.toSet());

        // 先删除关系
        QueryWrapper<AccGroupDevice> qw = new QueryWrapper<>();
        qw.eq("device_id", deviceId);
        this.remove(qw);

        // 下发删除（不影响关系删除）
        try {
            if (!groupIds.isEmpty()) {
                org.slf4j.LoggerFactory.getLogger(AccGroupDeviceServiceImpl.class).info("[ACC] 设备ID删除完成，准备批量下发移除成员 deviceId={}, groupCount={}", deviceId, groupIds.size());
                // 下发：对该设备按组集合批量移除所有成员（每成员4条删除命令）
                accIoTDispatchService.removeMembersFromDevicesForGroups(groupIds, java.util.List.of(deviceId));
            }
        } catch (Exception e) {
            org.slf4j.LoggerFactory.getLogger(AccGroupDeviceServiceImpl.class).warn("[ACC] 设备批量移除成员下发失败 deviceId={}, err={} ", deviceId, e.getMessage());
            // swallow
        }
    }

    // ===================== 说明 =====================
    // 下发逻辑统一由 AccIoTDispatchService 承担，避免成员/设备服务重复实现。
    // 该服务负责 PIN 解析、授权时区映射、设备SN解析，并保证新增/删除均为 4 条基础命令。
}