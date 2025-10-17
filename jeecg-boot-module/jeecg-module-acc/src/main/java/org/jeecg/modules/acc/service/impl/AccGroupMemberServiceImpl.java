package org.jeecg.modules.acc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.acc.entity.AccGroupMember;
import org.jeecg.modules.acc.mapper.AccGroupMemberMapper;
import org.jeecg.modules.acc.service.IAccGroupMemberService;
import org.jeecg.modules.acc.vo.AccMemberVO;
import org.jeecgframework.boot.system.api.SystemUserService;
import org.jeecgframework.boot.system.vo.UserLiteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccGroupMemberServiceImpl extends ServiceImpl<AccGroupMemberMapper, AccGroupMember> implements IAccGroupMemberService {

    @Autowired(required = false)
    private SystemUserService systemUserService;

    @Override
    public IPage<AccMemberVO> listMembersByGroupId(String groupId, Integer pageNo, Integer pageSize) {
        // 查询关联的成员ID列表
        List<String> memberIds = listMemberIdsByGroupId(groupId);

        // 使用本服务的轻量级Mapper填充用户信息（先按ID，再按用户名）
        List<AccMemberVO> memberVOs = new ArrayList<>();
        if (!memberIds.isEmpty()) {
            // 1) 按 ID 批量查询（通过 system-api 接口）
            List<UserLiteVO> byIds = systemUserService.queryUsersByIds(memberIds.toArray(new String[0]));
            Map<String, UserLiteVO> idMap = byIds == null ? Collections.emptyMap() : byIds.stream()
                    .collect(Collectors.toMap(UserLiteVO::getId, u -> u, (a, b) -> a));

            // 2) 未命中的作为可能的用户名再次查询（通过 system-api 接口）
            Set<String> missing = new HashSet<>(memberIds);
            missing.removeAll(idMap.keySet());
            List<UserLiteVO> byNames = new ArrayList<>();
            if (!missing.isEmpty()) {
                byNames = systemUserService.queryUsersByUsernames(missing.toArray(new String[0]));
            }
            Map<String, UserLiteVO> nameMap = byNames.stream().collect(Collectors.toMap(UserLiteVO::getUsername, u -> u, (a, b) -> a));

            // 3) 组装 VO
            for (String memberId : memberIds) {
                UserLiteVO u = idMap.get(memberId);
                if (u == null) {
                    u = nameMap.get(memberId);
                }

                AccMemberVO vo = new AccMemberVO();
                vo.setId(memberId);

                String realname = u == null ? "" : nullToEmpty(u.getRealname());
                String username = u == null ? "" : nullToEmpty(u.getUsername());
                String phone = u == null ? "" : nullToEmpty(u.getPhone());

                String deptName = "";
                if (u != null && u.getOrgCode() != null) {
                    String dn = systemUserService.getDepartNameByOrgCode(u.getOrgCode());
                    deptName = nullToEmpty(dn);
                }

                vo.setMemberName(realname);
                vo.setMemberCode(username);
                vo.setDepartment(deptName);
                vo.setPosition("");
                // 同步前端展示字段
                vo.setName(realname);
                vo.setDept(deptName);
                vo.setPhone(phone);
                memberVOs.add(vo);
            }
        }

        // 内存分页
        int total = memberVOs.size();
        int page = Math.max(1, pageNo);
        int size = Math.max(1, pageSize);
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        List<AccMemberVO> pageRecords = start >= total ? new ArrayList<>() : memberVOs.subList(start, end);

        Page<AccMemberVO> result = new Page<>(page, size, total);
        result.setRecords(pageRecords);
        return result;
    }

    @Override
    public List<String> listMemberIdsByGroupId(String groupId) {
        QueryWrapper<AccGroupMember> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("group_id", groupId);
        queryWrapper.select("member_id");
        
        List<AccGroupMember> groupMembers = this.list(queryWrapper);
        return groupMembers.stream()
                .map(AccGroupMember::getMemberId)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addMembers(String groupId, List<String> memberIds) {
        if (groupId == null || memberIds == null || memberIds.isEmpty()) return;

        // 查询已存在的成员，避免重复
        QueryWrapper<AccGroupMember> qw = new QueryWrapper<>();
        qw.eq("group_id", groupId).in("member_id", memberIds);
        List<AccGroupMember> exists = this.list(qw);
        Set<String> existedIds = exists.stream().map(AccGroupMember::getMemberId).collect(Collectors.toSet());

        List<AccGroupMember> toSave = memberIds.stream()
                .filter(id -> id != null && !id.trim().isEmpty())
                .filter(id -> !existedIds.contains(id))
                .map(id -> {
                    AccGroupMember gm = new AccGroupMember();
                    gm.setGroupId(groupId);
                    gm.setMemberId(id);
                    return gm;
                })
                .collect(Collectors.toList());

        if (!toSave.isEmpty()) {
            this.saveBatch(toSave);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeMembers(String groupId, List<String> memberIds) {
        if (groupId == null || memberIds == null || memberIds.isEmpty()) return;
        QueryWrapper<AccGroupMember> qw = new QueryWrapper<>();
        qw.eq("group_id", groupId).in("member_id", memberIds);
        this.remove(qw);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}