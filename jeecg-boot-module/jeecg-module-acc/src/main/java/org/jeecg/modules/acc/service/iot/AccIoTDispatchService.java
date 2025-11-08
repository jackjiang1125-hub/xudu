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
import org.jeecgframework.boot.acc.vo.AccUserLiteVO;
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
     * 当权限组绑定的时间规则(periodId)发生变更时，刷新该组在所有设备上的授权：
     * - 对组内每位成员，先删除其在设备上的授权记录(userauthorize)；
     * - 再按新的时区ID重新下发人员新增(含授权)的命令序列。
     */
    public void refreshGroupAuthorizationAfterPeriodChange(String groupId) {
        if (groupId == null || groupId.isBlank()) return;
        List<String> sns = getDeviceSNsByGroupId(groupId);
        if (sns.isEmpty()) {
            log.info("[Dispatch] 组无设备，跳过刷新授权 groupId={}", groupId);
            return;
        }

        List<String> memberIds = listMemberIdsByGroupId(groupId);
        if (memberIds.isEmpty()) {
            log.info("[Dispatch] 组无成员，跳过刷新授权 groupId={}", groupId);
            return;
        }

        Integer tzId = resolveAuthorizeTimezoneId(groupId);
        Map<String, UserLiteVO> userMap = fetchUsersByIds(memberIds);

        int totalOps = 0;
        for (String memberId : memberIds) {
            UserLiteVO u = userMap.get(memberId);
            String username = u == null ? null : safe(u.getUsername());
            String workNo = u == null ? null : safe(u.getWorkNo());
            String pin = resolvePin(workNo, username, memberId);

            for (String sn : sns) {
                try {
                    // 先删除授权（包含权限项），确保新的时区生效
                    iotDeviceService.removeAuthorize(sn, pin);
                } catch (Exception e) {
                    log.warn("[Dispatch] 刷新授权-删除失败 sn={}, pin={}, err={}", sn, pin, e.getMessage());
                }
                try {
                    // 再根据最新时区重新下发人员时间端权限
                    iotDeviceService.addUserAuthorize(sn, pin, tzId, 1, 1);
                    totalOps++;
                } catch (Exception e) {
                    log.warn("[Dispatch] 刷新授权-下发失败 sn={}, pin={}, err={}", sn, pin, e.getMessage());
                }
            }
        }
        log.info("[Dispatch] 权限组授权刷新完成 groupId={}, deviceCount={}, memberCount={}, tzId={}, reissued={} ",
                groupId, sns.size(), memberIds.size(), tzId, totalOps);
    }

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

    /**
     * 更新系统用户在所有已授权设备上的基础信息：
     * - 自动识别用户当前所属的权限组，聚合这些组绑定的设备SN
     * - 逐设备下发更新：优先不打断授权，仅更新用户信息与头像/抠图
     * - 若头像与抠图均为空，则先执行“删除人员相关信息”(含照片/比对照/权限)，再重建用户与授权
     */
    public void updateUserInfoOnAuthorizedDevices(AccUserLiteVO u) {
        if (u == null || u.getId() == null || u.getId().isBlank()) return;

        // 1) 找出该用户所属的所有权限组
        List<AccGroupMember> memberships = accGroupMemberMapper
            .selectList(new QueryWrapper<AccGroupMember>().eq("member_id", u.getId()));
        if (memberships == null || memberships.isEmpty()) {
            log.info("[Dispatch] 用户无任何权限组，跳过下发 userId={}", u.getId());
            return;
        }

        // 2) 聚合组→设备SN，并为每个设备选择一个授权时区ID（如发生冲突，保留首个并记录告警）
        Map<String, Integer> deviceTzMap = new LinkedHashMap<>(); // sn -> tzId
        for (AccGroupMember gm : memberships) {
            String gid = gm.getGroupId();
            if (gid == null || gid.isBlank()) continue;
            Integer tzId = resolveAuthorizeTimezoneId(gid);
            List<String> sns = getDeviceSNsByGroupId(gid);
            for (String sn : sns) {
                if (sn == null || sn.isBlank()) continue;
                Integer prev = deviceTzMap.putIfAbsent(sn, tzId);
                if (prev != null && !prev.equals(tzId)) {
                    log.warn("[Dispatch] 设备SN关联多个权限组且时区冲突 sn={}, tz(prev)={}, tz(curr)={}，沿用首个", sn, prev, tzId);
                }
            }
        }
        if (deviceTzMap.isEmpty()) {
            log.info("[Dispatch] 用户所属权限组下无设备，跳过下发 userId={}", u.getId());
            return;
        }

        // 3) 解析统一的 PIN 与用户字段
        String name = safe(u.getRealname());
        String username = safe(u.getUsername());
        String workNo = safe(u.getWorkNo());
        String pin = resolvePin(workNo, username, u.getId());
        String userPic = toBase64OrNull(safe(u.getAvatar()));
        String bioPhoto = toBase64OrNull(safe(u.getFaceCutout()));
        String cardNumber = safe(u.getCardNumber());
        String verifyPassword = safe(u.getVerifyPassword());
        Integer superUser = u.getSuperUser();
        Integer deviceOpPerm = u.getDeviceOpPerm();
        Boolean extendAccess = u.getExtendAccess();
        Boolean prohibitedRoster = u.getProhibitedRoster();
        Boolean validTimeEnabled = u.getValidTimeEnabled();
        Date validStartTime = u.getValidStartTime();
        Date validEndTime = u.getValidEndTime();

        // 4) 按设备执行下发
        int deviceCount = 0;
        // 根据需求：若最新用户“没有照片”(avatar为空)，则需要清除设备端的照片与抠图
        boolean removeBefore = (u.getAvatar() == null || u.getAvatar().trim().isEmpty());
        for (Map.Entry<String, Integer> e : deviceTzMap.entrySet()) {
            String sn = e.getKey();
            Integer tzId = e.getValue() == null ? 1 : e.getValue();
            try {
                // 若“没有照片”，则先清除照片/抠图/权限/人员后重建（确保设备端不残留旧图片）
                if (removeBefore) {
                    try {
                        iotDeviceService.removeUserPicAndBioPhoto(sn, pin);
                    } catch (Exception ex) {
                        log.warn("[Dispatch] 更新前清除人员信息失败 sn={}, pin={}, err={}", sn, pin, ex.getMessage());
                    }
                }
                iotDeviceService.addUserWithAuthorize(
                    sn, pin, name,
                    tzId, 1, 1,
                    userPic, bioPhoto,
                    cardNumber, verifyPassword,
                    superUser, deviceOpPerm,
                    extendAccess, prohibitedRoster, validTimeEnabled,
                    validStartTime, validEndTime
                );
                deviceCount++;
            } catch (Exception ex) {
                log.warn("[Dispatch] 更新用户信息下发失败 sn={}, pin={}, err={}", sn, pin, ex.getMessage());
            }
        }
        log.info("[Dispatch] 用户信息更新下发完成 userId={}, pin={}, devices={}", u.getId(), pin, deviceCount);
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