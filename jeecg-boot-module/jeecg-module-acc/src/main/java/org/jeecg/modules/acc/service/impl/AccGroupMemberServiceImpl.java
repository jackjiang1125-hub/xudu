package org.jeecg.modules.acc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.acc.entity.AccGroupMember;
import org.jeecg.modules.acc.entity.SysUserLite;
import org.jeecg.modules.acc.mapper.AccGroupMemberMapper;
import org.jeecg.modules.acc.mapper.SysDepartLiteMapper;
import org.jeecg.modules.acc.mapper.SysUserLiteMapper;
import org.jeecg.modules.acc.service.IAccGroupMemberService;
import org.jeecg.modules.acc.vo.AccMemberVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccGroupMemberServiceImpl extends ServiceImpl<AccGroupMemberMapper, AccGroupMember> implements IAccGroupMemberService {

    @Autowired
    private SysUserLiteMapper sysUserLiteMapper;

    @Autowired
    private SysDepartLiteMapper sysDepartLiteMapper;

    @Override
    public IPage<AccMemberVO> listMembersByGroupId(String groupId, Integer pageNo, Integer pageSize) {
        // 查询关联的成员ID列表
        List<String> memberIds = listMemberIdsByGroupId(groupId);

        // 使用本服务的轻量级Mapper填充用户信息（先按ID，再按用户名）
        List<AccMemberVO> memberVOs = new ArrayList<>();
        if (!memberIds.isEmpty()) {
            // 1) 按 ID 批量查询
            List<SysUserLite> byIds = sysUserLiteMapper.selectBatchIds(memberIds);
            Map<String, SysUserLite> idMap = byIds == null ? Collections.emptyMap() : byIds.stream()
                    .collect(Collectors.toMap(SysUserLite::getId, u -> u, (a, b) -> a));

            // 2) 未命中的作为可能的用户名再次查询
            Set<String> missing = new HashSet<>(memberIds);
            missing.removeAll(idMap.keySet());
            List<SysUserLite> byNames = new ArrayList<>();
            if (!missing.isEmpty()) {
                QueryWrapper<SysUserLite> qw = new QueryWrapper<>();
                qw.in("username", missing);
                byNames = sysUserLiteMapper.selectList(qw);
            }
            Map<String, SysUserLite> nameMap = byNames.stream().collect(Collectors.toMap(SysUserLite::getUsername, u -> u, (a, b) -> a));

            // 3) 组装 VO
            for (String memberId : memberIds) {
                SysUserLite u = idMap.get(memberId);
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
                    String dn = sysDepartLiteMapper.getDepartNameByOrgCode(u.getOrgCode());
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

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}