package org.jeecg.modules.acc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.acc.entity.AccDevice;
import org.jeecg.modules.acc.entity.AccGroupDevice;
import org.jeecg.modules.acc.mapper.AccDeviceMapper;
import org.jeecg.modules.acc.mapper.AccGroupDeviceMapper;
import org.jeecg.modules.acc.service.IAccGroupDeviceService;
import org.jeecg.modules.acc.vo.AccDeviceVO;
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
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeDevices(String groupId, List<String> deviceIds) {
        if (groupId == null || deviceIds == null || deviceIds.isEmpty()) return;
        QueryWrapper<AccGroupDevice> qw = new QueryWrapper<>();
        qw.eq("group_id", groupId).in("device_id", deviceIds);
        this.remove(qw);
    }
}