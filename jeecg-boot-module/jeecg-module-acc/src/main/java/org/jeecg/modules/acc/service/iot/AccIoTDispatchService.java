package org.jeecg.modules.acc.service.iot;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.acc.entity.AccDevice;
import org.jeecg.modules.acc.entity.AccGroup;
import org.jeecg.modules.acc.entity.AccGroupDevice;
import org.jeecg.modules.acc.entity.AccGroupMember;
import org.jeecg.modules.acc.entity.AccTimePeriod;
import org.jeecg.modules.acc.mapper.AccDeviceMapper;
import org.jeecg.modules.acc.mapper.AccGroupDeviceMapper;
import org.jeecg.modules.acc.mapper.AccGroupMapper;
import org.jeecg.modules.acc.mapper.AccGroupMemberMapper;
import org.jeecg.modules.acc.mapper.AccTimePeriodMapper;
import org.jeecgframework.boot.iot.api.IotDeviceService;
import org.jeecgframework.boot.system.api.SystemUserService;
import org.jeecgframework.boot.system.vo.UserLiteVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

/**
 * AccIoTDispatchService
 * 封装 ACC 业务下发的通用逻辑：
 * - 成员与设备的新增/移除触发的人员信息同步（新增为 4 条基础命令；删除为 4 条基础命令）
 * - 统一 PIN 解析策略（优先工号workNo，其次用户名username，最后memberId）
 * - 统一授权时区映射（按 AccGroup.periodId → AccTimePeriod.sortOrder）
 * - 统一设备 SN 解析
 */
@Service
@Slf4j
public class AccIoTDispatchService {

    @Autowired(required = false)
    private SystemUserService systemUserService;

    @Autowired
    private IotDeviceService iotDeviceService;

    @Autowired
    private AccGroupDeviceMapper accGroupDeviceMapper;

    @Autowired
    private AccGroupMemberMapper accGroupMemberMapper;

    @Autowired
    private AccDeviceMapper accDeviceMapper;

    @Autowired
    private AccGroupMapper accGroupMapper;

    @Autowired
    private AccTimePeriodMapper accTimePeriodMapper;

    @Value("${jeecg.path.upload:}")
    private String uploadRoot;

    /**
     * 将指定成员新增到组的所有设备：按成员列表为每台设备下发 4 条新增命令。
     */
    public void addMembersToGroupDevices(String groupId, Collection<String> memberIds) {
        if (groupId == null || memberIds == null || memberIds.isEmpty()) return;
        List<String> sns = getDeviceSNsByGroupId(groupId);
        if (sns.isEmpty()) return;

        Integer tzId = resolveAuthorizeTimezoneId(groupId);
        Map<String, UserLiteVO> userMap = fetchUsersByIds(memberIds);
        for (String memberId : memberIds) {
            UserLiteVO u = userMap.get(memberId);
            String name = u == null ? "" : safe(u.getRealname());
            String username = u == null ? null : safe(u.getUsername());
            String workNo = u == null ? null : safe(u.getWorkNo());
            String pin = resolvePin(workNo, username, memberId);
            String userPic = (u == null) ? null : toBase64OrNull(safe(u.getAvatar()));
            String bioPhoto = (u == null) ? null : toBase64OrNull(safe(u.getFaceCutout()));
            String cardNumber = (u == null) ? null : safe(u.getCardNumber());
            String verifyPassword = (u == null) ? null : safe(u.getVerifyPassword());
            Integer superUser = (u == null) ? null : u.getSuperUser();
            Integer deviceOpPerm = (u == null) ? null : u.getDeviceOpPerm();
            Boolean extendAccess = (u == null) ? null : u.getExtendAccess();
            Boolean prohibitedRoster = (u == null) ? null : u.getProhibitedRoster();
            Boolean validTimeEnabled = (u == null) ? null : u.getValidTimeEnabled();
            java.util.Date validStartTime = (u == null) ? null : u.getValidStartTime();
            java.util.Date validEndTime = (u == null) ? null : u.getValidEndTime();
            for (String sn : sns) {
                try {
                    iotDeviceService.addUserWithAuthorize(
                        sn, pin, name,
                        tzId, 1, 1,
                        userPic, bioPhoto,
                        cardNumber, verifyPassword,
                        superUser, deviceOpPerm,
                        extendAccess, prohibitedRoster, validTimeEnabled,
                        validStartTime, validEndTime
                    );
                } catch (Exception e) {
                    log.warn("[Dispatch] 新增成员下发失败 sn={}, pin={}, err={}", sn, pin, e.getMessage());
                }
            }
        }
        log.info("[Dispatch] 成员新增下发完成 groupId={}, members={}, tzId={}, deviceCount={}", groupId, memberIds.size(), tzId, sns.size());
    }

    /**
     * 将指定成员从组的所有设备移除：为每台设备下发 4 条删除命令。
     */
    public void removeMembersFromGroupDevices(String groupId, Collection<String> memberIds) {
        if (groupId == null || memberIds == null || memberIds.isEmpty()) return;
        List<String> sns = getDeviceSNsByGroupId(groupId);
        if (sns.isEmpty()) return;

        Map<String, UserLiteVO> userMap = fetchUsersByIds(memberIds);
        for (String memberId : memberIds) {
            UserLiteVO u = userMap.get(memberId);
            String username = u == null ? null : safe(u.getUsername());
            String workNo = u == null ? null : safe(u.getWorkNo());
            String pin = resolvePin(workNo, username, memberId);
            for (String sn : sns) {
                try {
                    iotDeviceService.removeUserAndAuthorize(sn, pin);
                } catch (Exception e) {
                    log.warn("[Dispatch] 删除成员下发失败 sn={}, pin={}, err={}", sn, pin, e.getMessage());
                }
            }
        }
        log.info("[Dispatch] 成员删除下发完成 groupId={}, members={}, deviceCount={}", groupId, memberIds.size(), sns.size());
    }

    /**
     * 新增设备后同步组内所有成员到这些设备：每成员对每设备下发 4 条新增命令。
     */
    public void syncGroupMembersToDevices(String groupId, Collection<String> deviceIds) {
        if (groupId == null || deviceIds == null || deviceIds.isEmpty()) return;
        List<String> sns = getDeviceSNsByDeviceIds(deviceIds);
        if (sns.isEmpty()) return;

        Integer tzId = resolveAuthorizeTimezoneId(groupId);
        List<String> memberIds = listMemberIdsByGroupId(groupId);
        if (memberIds.isEmpty()) return;

        Map<String, UserLiteVO> userMap = fetchUsersByIds(memberIds);
        for (String memberId : memberIds) {
            UserLiteVO u = userMap.get(memberId);
            String name = u == null ? "" : safe(u.getRealname());
            String username = u == null ? null : safe(u.getUsername());
            String workNo = u == null ? null : safe(u.getWorkNo());
            String pin = resolvePin(workNo, username, memberId);
            String userPic = (u == null) ? null : toBase64OrNull(safe(u.getAvatar()));
            String bioPhoto = (u == null) ? null : toBase64OrNull(safe(u.getFaceCutout()));
            String cardNumber = (u == null) ? null : safe(u.getCardNumber());
            String verifyPassword = (u == null) ? null : safe(u.getVerifyPassword());
            Integer superUser = (u == null) ? null : u.getSuperUser();
            Integer deviceOpPerm = (u == null) ? null : u.getDeviceOpPerm();
            Boolean extendAccess = (u == null) ? null : u.getExtendAccess();
            Boolean prohibitedRoster = (u == null) ? null : u.getProhibitedRoster();
            Boolean validTimeEnabled = (u == null) ? null : u.getValidTimeEnabled();
            java.util.Date validStartTime = (u == null) ? null : u.getValidStartTime();
            java.util.Date validEndTime = (u == null) ? null : u.getValidEndTime();
            for (String sn : sns) {
                try {
                    iotDeviceService.addUserWithAuthorize(
                        sn, pin, name,
                        tzId, 1, 1,
                        userPic, bioPhoto,
                        cardNumber, verifyPassword,
                        superUser, deviceOpPerm,
                        extendAccess, prohibitedRoster, validTimeEnabled,
                        validStartTime, validEndTime
                    );
                } catch (Exception e) {
                    log.warn("[Dispatch] 设备新增同步成员失败 sn={}, pin={}, err={}", sn, pin, e.getMessage());
                }
            }
        }
        log.info("[Dispatch] 设备新增成员同步完成 groupId={}, deviceCount={}, memberCount={}, tzId={} ", groupId, sns.size(), memberIds.size(), tzId);
    }

    /**
     * 移除设备后，从这些设备移除组内所有成员：每成员对每设备下发 4 条删除命令。
     */
    public void removeGroupMembersFromDevices(String groupId, Collection<String> deviceIds) {
        if (groupId == null || deviceIds == null || deviceIds.isEmpty()) return;
        List<String> sns = getDeviceSNsByDeviceIds(deviceIds);
        if (sns.isEmpty()) return;

        List<String> memberIds = listMemberIdsByGroupId(groupId);
        if (memberIds.isEmpty()) return;
        Map<String, UserLiteVO> userMap = fetchUsersByIds(memberIds);
        for (String memberId : memberIds) {
            UserLiteVO u = userMap.get(memberId);
            String username = u == null ? null : safe(u.getUsername());
            String workNo = u == null ? null : safe(u.getWorkNo());
            String pin = resolvePin(workNo, username, memberId);
            for (String sn : sns) {
                try {
                    iotDeviceService.removeUserAndAuthorize(sn, pin);
                } catch (Exception e) {
                    log.warn("[Dispatch] 设备移除成员失败 sn={}, pin={}, err={}", sn, pin, e.getMessage());
                }
            }
        }
        log.info("[Dispatch] 设备移除成员下发完成 groupId={}, deviceCount={}, memberCount={}", groupId, sns.size(), memberIds.size());
    }

    /**
     * 按组集合批量移除成员（用于设备级批量移除场景）。
     */
    public void removeMembersFromDevicesForGroups(Set<String> groupIds, Collection<String> deviceIds) {
        if (groupIds == null || groupIds.isEmpty() || deviceIds == null || deviceIds.isEmpty()) return;
        List<String> sns = getDeviceSNsByDeviceIds(deviceIds);
        if (sns.isEmpty()) return;
        for (String groupId : groupIds) {
            List<String> memberIds = listMemberIdsByGroupId(groupId);
            if (memberIds.isEmpty()) continue;
            Map<String, UserLiteVO> userMap = fetchUsersByIds(memberIds);
            for (String memberId : memberIds) {
                UserLiteVO u = userMap.get(memberId);
                String username = u == null ? null : safe(u.getUsername());
                String workNo = u == null ? null : safe(u.getWorkNo());
                String pin = resolvePin(workNo, username, memberId);
                for (String sn : sns) {
                    try {
                        iotDeviceService.removeUserAndAuthorize(sn, pin);
                    } catch (Exception e) {
                        log.warn("[Dispatch] 设备移除成员(批量)失败 sn={}, pin={}, err={}", sn, pin, e.getMessage());
                    }
                }
            }
        }
        log.info("[Dispatch] 设备移除成员(按组集合)下发完成 groups={}, deviceCount={}", groupIds.size(), sns.size());
    }

    /* ===================== 私有辅助 ===================== */

    private List<String> getDeviceSNsByGroupId(String groupId) {
        List<AccGroupDevice> relations = accGroupDeviceMapper.selectList(new QueryWrapper<AccGroupDevice>().eq("group_id", groupId));
        if (relations == null || relations.isEmpty()) return new ArrayList<>();
        List<String> deviceIds = relations.stream().map(AccGroupDevice::getDeviceId).filter(id -> id != null && !id.isBlank()).collect(Collectors.toList());
        if (deviceIds.isEmpty()) return new ArrayList<>();
        List<AccDevice> devices = accDeviceMapper.selectBatchIds(deviceIds);
        return devices == null ? new ArrayList<>() : devices.stream().map(AccDevice::getSn).filter(sn -> sn != null && !sn.isBlank()).collect(Collectors.toList());
    }

    private List<String> getDeviceSNsByDeviceIds(Collection<String> deviceIds) {
        if (deviceIds == null || deviceIds.isEmpty()) return new ArrayList<>();
        List<AccDevice> devices = accDeviceMapper.selectBatchIds(new ArrayList<>(deviceIds));
        return devices == null ? new ArrayList<>() : devices.stream().map(AccDevice::getSn).filter(sn -> sn != null && !sn.isBlank()).collect(Collectors.toList());
    }

    private Integer resolveAuthorizeTimezoneId(String groupId) {
        AccGroup g = accGroupMapper.selectById(groupId);
        if (g == null || g.getPeriodId() == null) return 1;
        AccTimePeriod p = accTimePeriodMapper.selectById(g.getPeriodId());
        Integer sortOrder = (p == null ? null : p.getSortOrder());
        return sortOrder == null ? 1 : sortOrder;
    }

    private List<String> listMemberIdsByGroupId(String groupId) {
        List<AccGroupMember> members = accGroupMemberMapper.selectList(new QueryWrapper<AccGroupMember>().eq("group_id", groupId));
        if (members == null) return new ArrayList<>();
        return members.stream().map(AccGroupMember::getMemberId).filter(Objects::nonNull).collect(Collectors.toList());
    }

    private Map<String, UserLiteVO> fetchUsersByIds(Collection<String> ids) {
        Map<String, UserLiteVO> out = new HashMap<>();
        if (ids == null || ids.isEmpty()) return out;
        List<String> idList = ids.stream()
            .filter(Objects::nonNull)
            .map(String::trim)
            .filter(s -> !s.isEmpty())
            .distinct()
            .collect(Collectors.toList());
        if (idList.isEmpty()) return out;
        try {
            List<UserLiteVO> users = systemUserService.queryUsersByIds(idList.toArray(new String[0]));
            if (users != null) {
                for (UserLiteVO u : users) {
                    if (u != null && u.getId() != null && !u.getId().isBlank()) {
                        out.put(u.getId(), u);
                    }
                }
            }
        } catch (Exception ignored) {}
        return out;
    }

    private static String safe(String s) { return s == null ? null : s.trim(); }

    private static String resolvePin(String workNo, String username, String fallback) {
        if (workNo != null && !workNo.isBlank()) return workNo;
        if (username != null && !username.isBlank()) return username;
        return fallback;
    }

    /**
     * 将传入的头像/抠图字符串统一转换为 Base64 内容（不带 data: 前缀），失败返回 null。
     * 支持：
     * - data:URI（提取逗号后内容）
     * - http/https/file: URL（下载并转为Base64）
     * - 本地路径（绝对路径或者以 uploadRoot 作为根的相对路径）
     * - 纯Base64字符串（直接返回）
     */
    private String toBase64OrNull(String src) {
        if (src == null || src.isBlank()) return null;
        String val = src.trim();
        try {
            if (isDataUri(val)) {
                return extractDataUriContent(val);
            }
            if (isUrl(val)) {
                try (java.io.InputStream is = new URL(val).openStream()) {
                    byte[] bytes = is.readAllBytes();
                    return Base64.getEncoder().encodeToString(bytes);
                }
            }
            // 视为文件路径：绝对或相对（相对则拼接上传根）
            Path p = Paths.get(val);
            Path file = p.isAbsolute() ? p :
                    ((uploadRoot == null || uploadRoot.isBlank()) ? p : Paths.get(uploadRoot, val));
            if (Files.exists(file) && Files.isRegularFile(file)) {
                byte[] bytes = Files.readAllBytes(file);
                return Base64.getEncoder().encodeToString(bytes);
            }
            // 非URL且非有效文件路径，返回空避免误将相对路径当作Base64
            return null;
        } catch (Exception e) {
            log.warn("[Dispatch] 图片转Base64失败 src={}, err={}", val, e.toString());
            return null;
        }
    }

    private boolean isDataUri(String s) {
        return s != null && s.regionMatches(true, 0, "data:", 0, 5);
    }

    private String extractDataUriContent(String dataUri) {
        int idx = dataUri.indexOf(',');
        return idx >= 0 ? dataUri.substring(idx + 1) : dataUri;
    }

    private boolean isUrl(String s) {
        if (s == null) return false;
        String v = s.trim().toLowerCase();
        return v.startsWith("http://") || v.startsWith("https://") || v.startsWith("file:");
    }
}