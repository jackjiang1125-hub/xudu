package org.jeecg.modules.acc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.acc.entity.AccGroup;
import org.jeecg.modules.acc.entity.AccGroupDevice;
import org.jeecg.modules.acc.entity.AccGroupMember;
import org.jeecg.modules.acc.mapper.AccGroupMapper;
import org.jeecg.modules.acc.mapper.AccTimePeriodMapper;
import org.jeecg.modules.acc.entity.AccTimePeriod;
import org.jeecg.modules.acc.mapper.AccGroupDeviceMapper;
import org.jeecg.modules.acc.mapper.AccGroupMemberMapper;
import org.jeecg.modules.acc.service.IAccGroupService;
import org.jeecg.modules.acc.vo.AccGroupVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccGroupServiceImpl extends JeecgServiceImpl<AccGroupMapper, AccGroup> implements IAccGroupService {

    @Autowired
    private AccGroupMemberMapper groupMemberMapper;

    @Autowired
    private AccGroupDeviceMapper groupDeviceMapper;

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final SimpleDateFormat TS_FMT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private AccTimePeriodMapper timePeriodMapper;

    @Override
    public IPage<AccGroupVO> pageList(String groupName, Integer memberCount, Integer deviceCount, Integer pageNo, Integer pageSize) {
        // 先按名称过滤所有记录，再做数量过滤，最后内存分页
        LambdaQueryWrapper<AccGroup> qw = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(groupName)) {
            qw.like(AccGroup::getGroupName, groupName);
        }
        List<AccGroup> allGroups = this.list(qw);

        List<AccGroupVO> filtered = new ArrayList<>();
        for (AccGroup g : allGroups) {
            AccGroupVO vo = toVOWithoutDetail(g);
            int memCnt = countMembers(g.getId());
            int devCnt = countDevices(g.getId());
            vo.setMemberCount(memCnt);
            vo.setDeviceCount(devCnt);

            boolean pass = true;
            if (memberCount != null) {
                pass = pass && memCnt == memberCount;
            }
            if (deviceCount != null) {
                pass = pass && devCnt == deviceCount;
            }
            if (pass) {
                filtered.add(vo);
            }
        }

        // 内存分页
        int total = filtered.size();
        int page = Math.max(1, pageNo);
        int size = Math.max(1, pageSize);
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        List<AccGroupVO> pageRecords = start >= total ? new ArrayList<>() : filtered.subList(start, end);

        Page<AccGroupVO> result = new Page<>(page, size, total);
        result.setRecords(pageRecords);
        return result;
    }

    @Override
    public AccGroupVO getDetailById(String id) {
        AccGroup g = this.getById(id);
        if (g == null) return null;
        AccGroupVO vo = toVOWithoutDetail(g);
        vo.setMembers(listMemberIds(id));
        vo.setDevices(listDeviceIds(id));
        vo.setMemberCount(vo.getMembers() == null ? 0 : vo.getMembers().size());
        vo.setDeviceCount(vo.getDevices() == null ? 0 : vo.getDevices().size());
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccGroupVO saveVO(AccGroupVO vo, String operator) {
        AccGroup entity = new AccGroup();
        entity.setGroupName(vo.getGroupName());
        entity.setRemark(vo.getRemark());
        entity.setPeriodId(vo.getPeriodId());
        this.save(entity);

        // 关联保存
        saveMemberRelations(entity.getId(), vo.getMembers());
        saveDeviceRelations(entity.getId(), vo.getDevices());

        // 返回VO
        AccGroupVO saved = toVOWithoutDetail(entity);
        saved.setMembers(listMemberIds(entity.getId()));
        saved.setDevices(listDeviceIds(entity.getId()));
        saved.setMemberCount(countMembers(entity.getId()));
        saved.setDeviceCount(countDevices(entity.getId()));
        return saved;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public AccGroupVO updateVO(AccGroupVO vo, String operator) {
        Objects.requireNonNull(vo.getId(), "更新操作必须提供ID");
        AccGroup entity = this.getById(vo.getId());
        if (entity == null) {
            throw new IllegalArgumentException("权限组不存在: " + vo.getId());
        }
        entity.setGroupName(vo.getGroupName());
        entity.setRemark(vo.getRemark());
        entity.setPeriodId(vo.getPeriodId());
        this.updateById(entity);

        // 覆盖关联
        deleteMemberRelations(entity.getId());
        deleteDeviceRelations(entity.getId());
        saveMemberRelations(entity.getId(), vo.getMembers());
        saveDeviceRelations(entity.getId(), vo.getDevices());

        AccGroupVO updated = toVOWithoutDetail(entity);
        updated.setMembers(listMemberIds(entity.getId()));
        updated.setDevices(listDeviceIds(entity.getId()));
        updated.setMemberCount(countMembers(entity.getId()));
        updated.setDeviceCount(countDevices(entity.getId()));
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteWithRelations(String id) {
        deleteMemberRelations(id);
        deleteDeviceRelations(id);
        return this.removeById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteBatchWithRelations(String[] ids) {
        for (String id : ids) {
            deleteMemberRelations(id);
            deleteDeviceRelations(id);
        }
        return this.removeByIds(java.util.Arrays.asList(ids));
    }

    // ===================== helper methods =====================

    private AccGroupVO toVOWithoutDetail(AccGroup g) {
        AccGroupVO vo = new AccGroupVO();
        vo.setId(g.getId());
        vo.setGroupName(g.getGroupName());
        vo.setRemark(g.getRemark());
        vo.setPeriodId(g.getPeriodId());
        // 读取时间段名称
        if (StringUtils.isNotBlank(g.getPeriodId())) {
            AccTimePeriod tp = timePeriodMapper.selectById(g.getPeriodId());
            vo.setPeriodName(tp != null ? tp.getName() : null);
        }
        vo.setCreateTime(formatCreateTime(g.getCreateTime()));
        return vo;
    }

    private String formatCreateTime(Date date) {
        if (date == null) return null;
        try { return TS_FMT.format(date); } catch (Exception e) { return null; }
    }

    // 移除旧的时间范围与适用日期相关辅助方法

    private int countMembers(String groupId) {
        return groupMemberMapper.selectCount(new LambdaQueryWrapper<AccGroupMember>().eq(AccGroupMember::getGroupId, groupId)).intValue();
    }

    private int countDevices(String groupId) {
        return groupDeviceMapper.selectCount(new LambdaQueryWrapper<AccGroupDevice>().eq(AccGroupDevice::getGroupId, groupId)).intValue();
    }

    private List<String> listMemberIds(String groupId) {
        return groupMemberMapper.selectList(new LambdaQueryWrapper<AccGroupMember>().eq(AccGroupMember::getGroupId, groupId))
                .stream().map(AccGroupMember::getMemberId).collect(Collectors.toList());
    }

    private List<String> listDeviceIds(String groupId) {
        return groupDeviceMapper.selectList(new LambdaQueryWrapper<AccGroupDevice>().eq(AccGroupDevice::getGroupId, groupId))
                .stream().map(AccGroupDevice::getDeviceId).collect(Collectors.toList());
    }

    private void saveMemberRelations(String groupId, List<String> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) return;
        for (String mid : memberIds) {
            AccGroupMember gm = new AccGroupMember();
            gm.setGroupId(groupId);
            gm.setMemberId(mid);
            groupMemberMapper.insert(gm);
        }
    }

    private void saveDeviceRelations(String groupId, List<String> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) return;
        for (String did : deviceIds) {
            AccGroupDevice gd = new AccGroupDevice();
            gd.setGroupId(groupId);
            gd.setDeviceId(did);
            groupDeviceMapper.insert(gd);
        }
    }

    private void deleteMemberRelations(String groupId) {
        groupMemberMapper.delete(new LambdaQueryWrapper<AccGroupMember>().eq(AccGroupMember::getGroupId, groupId));
    }

    private void deleteDeviceRelations(String groupId) {
        groupDeviceMapper.delete(new LambdaQueryWrapper<AccGroupDevice>().eq(AccGroupDevice::getGroupId, groupId));
    }
}