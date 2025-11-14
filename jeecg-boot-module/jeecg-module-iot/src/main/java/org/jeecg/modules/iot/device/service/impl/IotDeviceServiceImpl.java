package org.jeecg.modules.iot.device.service.impl;


import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import org.apache.commons.lang3.StringUtils;

import org.jeecg.modules.iot.device.entity.IotDevice;
import org.jeecg.modules.iot.device.entity.IotDevicePhoto;
import org.jeecg.modules.iot.device.entity.IotDeviceRtLog;
import org.jeecg.modules.iot.device.entity.IotDeviceState;
import org.jeecg.modules.iot.device.entity.IotDeviceCommand;
import org.jeecg.modules.iot.device.entity.IotDeviceCommandReport;
import org.jeecg.modules.iot.device.mapper.IotDeviceMapper;
import org.jeecg.modules.iot.device.service.ControlDeviceCommandDispatcher;
import org.jeecg.modules.iot.device.mapstruct.IotDeviceMapstruct;
import org.jeecg.modules.iot.device.mapstruct.IotDeviceQueryMapstruct;
import org.jeecg.modules.iot.device.service.IotDevicePhotoService;
import org.jeecg.modules.iot.device.service.IotDeviceRtLogService;
import org.jeecg.modules.iot.device.service.IotDeviceStateService;
import org.jeecg.modules.iot.device.service.IotDeviceCommandService;
import org.jeecg.modules.iot.device.service.IotDeviceCommandReportService;
import org.jeecg.modules.iot.device.service.IotDeviceOptionsService;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.jeecgframework.boot.iot.api.IotDeviceService;
import org.jeecgframework.boot.iot.query.IotDeviceQuery;
import org.jeecgframework.boot.iot.vo.IotDeviceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.exception.JeecgBootException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;
import java.util.List;

import org.jeecg.modules.iot.device.seq.CommandSeqService;
import org.jeecg.modules.iot.utils.zkteco.AccessCommandFactory;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;

import org.jeecg.modules.iot.utils.zkteco.OptionsCommandFactory;

@Service
@Slf4j
public class IotDeviceServiceImpl extends JeecgServiceImpl<IotDeviceMapper, IotDevice> implements IotDeviceService {

    @Autowired
    private IotDeviceQueryMapstruct iotDeviceQueryMapstruct;

    @Autowired
    private IotDeviceMapstruct iotDeviceMapstruct;

    @Autowired
    private IotDeviceInnerServiceImpl iotDeviceInnerServiceImpl;

    @Autowired
    private IotDevicePhotoService iotDevicePhotoService;

    @Autowired
    private IotDeviceRtLogService iotDeviceRtLogService;

    @Autowired
    private IotDeviceStateService iotDeviceStateService;

    @Autowired
    private IotDeviceCommandService iotDeviceCommandService;

    @Autowired
    private IotDeviceOptionsService iotDeviceOptionsService;

    @Autowired
    private IotDeviceCommandReportService iotDeviceCommandReportService;

    @Autowired
    private ControlDeviceCommandDispatcher controlDeviceCommandDispatcher;

    @Autowired
    private CommandSeqService commandSeqService;

    // 简单并发删除锁，避免同一SN并发删除
    private final ConcurrentHashMap<String, ReentrantLock> deletionLocks = new ConcurrentHashMap<>();

    @Override
    public PageResult<IotDeviceVO> list(IotDeviceQuery iotDeviceQuery,PageRequest pageRequest, Map<String, String[]> queryParam) {
            //一行代码搞定 DT0->Entity  查询之后 entity->VO
        return pageByQuery(iotDeviceQuery,
                pageRequest.getPageNo(),
                pageRequest.getPageSize(),
                queryParam,
                iotDeviceQueryMapstruct::toIotDevice,
                iotDeviceMapstruct::toIotDeviceVO
              //  ,qw -> qw.orderByDesc("create_time") //带排序
                );
    }

    @Override
    public IotDeviceVO getBySn(String sn) {
        if (StringUtils.isBlank(sn)) {
            return null;
        }
        LambdaQueryWrapper<IotDevice> qw = new LambdaQueryWrapper<>();
        qw.eq(IotDevice::getSn, sn).last("limit 1");
        IotDevice entity = this.getOne(qw, false);
        return entity == null ? null : iotDeviceMapstruct.toIotDeviceVO(entity);
    }

    @Override
    public void authorizeDevice(String deviceSn,
            String registryCode,
            String remark,
            String operator) {
        if (StringUtils.isBlank(deviceSn)) {
            return;
        }
        iotDeviceInnerServiceImpl.authorizeDevice(deviceSn, registryCode, remark, operator);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteByDeviceSn(String sn) {
        // 参数验证
        if (StringUtils.isBlank(sn)) {
            log.warn("删除设备失败: sn为空");
            throw new JeecgBootException("设备序列号不能为空");
        }
        ReentrantLock lock = deletionLocks.computeIfAbsent(sn, k -> new ReentrantLock());
        lock.lock();
        try {
            log.info("开始删除设备及关联数据: sn={}", sn);

            // 记录删除前是否存在主设备记录（便于返回提示）
            boolean deviceExists = this.count(new LambdaQueryWrapper<IotDevice>().eq(IotDevice::getSn, sn)) > 0;

            // 删除子表数据（按sn）
            int photoDeleted = iotDevicePhotoService.remove(new LambdaQueryWrapper<IotDevicePhoto>().eq(IotDevicePhoto::getSn, sn)) ? 1 : 0;
            int rtlogDeleted = iotDeviceRtLogService.remove(new LambdaQueryWrapper<IotDeviceRtLog>().eq(IotDeviceRtLog::getSn, sn)) ? 1 : 0;
            int stateDeleted = iotDeviceStateService.remove(new LambdaQueryWrapper<IotDeviceState>().eq(IotDeviceState::getSn, sn)) ? 1 : 0;
            int cmdDeleted = iotDeviceCommandService.remove(new LambdaQueryWrapper<IotDeviceCommand>().eq(IotDeviceCommand::getSn, sn)) ? 1 : 0;
            // options 表有独立的删除方法，也支持按 sn 删除
            iotDeviceOptionsService.deleteByDeviceSn(sn);
            int cmdReportDeleted = iotDeviceCommandReportService.remove(new LambdaQueryWrapper<IotDeviceCommandReport>().eq(IotDeviceCommandReport::getSn, sn)) ? 1 : 0;

            // 删除主设备记录
            int deviceDeleted = this.remove(new LambdaQueryWrapper<IotDevice>().eq(IotDevice::getSn, sn)) ? 1 : 0;

            log.info("删除完成: device={}, photo={}, rtlog={}, state={}, command={}, options=*, cmdReport={}",
                    deviceDeleted, photoDeleted, rtlogDeleted, stateDeleted, cmdDeleted, cmdReportDeleted);

            if (!deviceExists && deviceDeleted == 0) {
                // 主记录不存在时，给出有意义的提示（但子表已清理不报错）
                log.warn("删除设备结束: 未找到主设备记录 sn={}，已清理其余关联表", sn);
            }
        } catch (JeecgBootException e) {
            // 继续抛出以便上层统一异常处理
            throw e;
        } catch (Exception e) {
            log.error("删除设备过程中发生异常: sn={}", sn, e);
            throw new JeecgBootException("删除设备失败: " + e.getMessage());
        } finally {
            try {
                lock.unlock();
            } finally {
                deletionLocks.remove(sn);
            }
        }
    }

    @Override
    public void syncTime(String deviceSn, Long timestamp) {
        // 调用内部服务实现时间同步
        if (StringUtils.isBlank(deviceSn)) {
            return;
        }
        controlDeviceCommandDispatcher.syncTime(deviceSn, timestamp, "");
    }

    @Override
    public void syncTimezone(String deviceSn, String timezone) {
        // 调用内部服务实现时区同步
        if (StringUtils.isBlank(deviceSn)) {
            return;
        }
        controlDeviceCommandDispatcher.syncTimezone(deviceSn, timezone, "");
    }

    @Override
    public void remoteOpenDoor(String sn, Integer doorId, Integer pulseSeconds, String operator) {
        if (StringUtils.isBlank(sn) || doorId == null || doorId <= 0) {
            return;
        }
        controlDeviceCommandDispatcher.openDoor(sn, doorId, pulseSeconds, StringUtils.defaultString(operator, ""));
    }

    @Override
    public void remoteCloseDoor(String sn, Integer doorId, String operator) {
        if (StringUtils.isBlank(sn) || doorId == null || doorId <= 0) {
            return;
        }
        // 关门：使用控制命令数值编码（示例 01010100）
        controlDeviceCommandDispatcher.closeDoor(sn, doorId, StringUtils.defaultString(operator, ""));
    }

    @Override
    public void remoteCancelAlarm(String sn, Integer doorId, String operator) {
        if (StringUtils.isBlank(sn) || doorId == null || doorId <= 0) {
            return;
        }
        controlDeviceCommandDispatcher.cancelAlarm(sn, doorId, StringUtils.defaultString(operator, ""));
    }

    @Override
    public void remoteHoldOpen(String sn, Integer doorId, String operator) {
        if (StringUtils.isBlank(sn) || doorId == null || doorId <= 0) {
            return;
        }
        controlDeviceCommandDispatcher.holdOpen(sn, doorId, StringUtils.defaultString(operator, ""));
    }

    @Override
    public void remoteLockDoor(String sn, Integer doorId, String operator) {
        if (StringUtils.isBlank(sn) || doorId == null || doorId <= 0) {
            return;
        }
        controlDeviceCommandDispatcher.lockDoor(sn, doorId, StringUtils.defaultString(operator, ""));
    }

    @Override
    public void remoteUnlockDoor(String sn, Integer doorId, String operator) {
        if (StringUtils.isBlank(sn) || doorId == null || doorId <= 0) {
            return;
        }
        controlDeviceCommandDispatcher.unlockDoor(sn, doorId, StringUtils.defaultString(operator, ""));
    }

    @Override
    public void enableTodayAlwaysOpen(String sn, Integer doorId, String operator) {
        if (StringUtils.isBlank(sn) || doorId == null || doorId <= 0) {
            return;
        }
        controlDeviceCommandDispatcher.enableTodayAlwaysOpen(sn, doorId, StringUtils.defaultString(operator, ""));
    }

    @Override
    public void disableTodayAlwaysOpen(String sn, Integer doorId, String operator) {
        if (StringUtils.isBlank(sn) || doorId == null || doorId <= 0) {
            return;
        }
        controlDeviceCommandDispatcher.disableTodayAlwaysOpen(sn, doorId, StringUtils.defaultString(operator, ""));
    }

    @Override
    public String enqueueDataCountUser(String sn, String operator) {
        if (StringUtils.isBlank(sn)) {
            return null;
        }
        int cmdId = (int) commandSeqService.nextSeq(sn);
        String cmd = "C:" + cmdId + ":" + "DATA COUNT user";
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), StringUtils.defaultString(operator, ""));
        return String.valueOf(cmdId);
    }

    @Override
    public List<String> getCommandReportsRaw(String sn, Long sinceMillis, List<String> ids) {
        if (StringUtils.isBlank(sn) || ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        LocalDateTime since = sinceMillis != null ? LocalDateTime.ofInstant(Instant.ofEpochMilli(sinceMillis), ZoneId.systemDefault()) : LocalDateTime.now().minusSeconds(10);
        QueryWrapper<IotDeviceCommandReport> qw = new QueryWrapper<>();
        qw.eq("sn", sn).ge("report_time", since).in("command_id", ids);
        List<IotDeviceCommandReport> reports = iotDeviceCommandReportService.list(qw);
        List<String> lines = new ArrayList<>();
        for (IotDeviceCommandReport r : reports) {
            if (r.getRawPayload() != null) {
                lines.add(r.getRawPayload());
            }
        }
        return lines;
    }

    @Override
    public String enqueueDataCountBioPhoto(String sn, String operator) {
        if (StringUtils.isBlank(sn)) {
            return null;
        }
        int cmdId = (int) commandSeqService.nextSeq(sn);
        String cmd = "C:" + cmdId + ":" + "DATA COUNT biophoto";
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), StringUtils.defaultString(operator, ""));
        return String.valueOf(cmdId);
    }

    @Override
    public String enqueueDataCountBiodata(String sn, Integer type, String operator) {
        if (StringUtils.isBlank(sn) || type == null) {
            return null;
        }
        int cmdId = (int) commandSeqService.nextSeq(sn);
        String cmd = "C:" + cmdId + ":" + ("DATA COUNT biodata" + "\t" + "Type=" + type);
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), StringUtils.defaultString(operator, ""));
        return String.valueOf(cmdId);
    }

    // 删除一体化模板，type=8是掌静脉
    @Override
    public void deleteAllTemplatesType8(String sn) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        int id = (int) commandSeqService.nextSeq(sn);
        String cmd = AccessCommandFactory.buildDeleteBioData(id, 8, null, null);
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), "");
    }

    // 删除一体化模板，type=9是可见光面部
    @Override
    public void deleteAllTemplatesType9(String sn) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        int id = (int) commandSeqService.nextSeq(sn);
        String cmd = AccessCommandFactory.buildDeleteBioData(id, 9, null, null);
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), "");
    }

    // 删除所有用户权限
    @Override
    public void deleteAllUserAuthorize(String sn) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        int id = (int) commandSeqService.nextSeq(sn);
        String cmd = AccessCommandFactory.buildDeleteAllRows(id, "userauthorize");
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), "");
    }

    // 删除所有用户
    @Override
    public void deleteAllUsers(String sn) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        int id = (int) commandSeqService.nextSeq(sn);
        String cmd = AccessCommandFactory.buildDeleteAllRows(id, "user");
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), "");
    }

    // 删除全部联动详细信息数据
    @Override
    public void deleteAllInoutfun(String sn) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        int cmdId = (int) commandSeqService.nextSeq(sn);
        String cmd = AccessCommandFactory.buildDeleteAllRows(cmdId, "inoutfun");
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), "");
    }

    // 删除全部首卡开门数据
    @Override
    public void deleteAllFirstcard(String sn) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        int cmdId = (int) commandSeqService.nextSeq(sn);
        String cmd = AccessCommandFactory.buildDeleteAllRows(cmdId, "firstcard");
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), "");
    }

    // 删除全部不同时段的验证方式数据
    @Override
    public void deleteAllDiffTimezoneVS(String sn) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        int cmdId = (int) commandSeqService.nextSeq(sn);
        String cmd = AccessCommandFactory.buildDeleteAllRows(cmdId, "DiffTimezoneVS");
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), "");
    }

    // 删除全部门不同时段的验证方式数据
    @Override
    public void deleteAllDoorVSTimezone(String sn) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        int cmdId = (int) commandSeqService.nextSeq(sn);
        String cmd = AccessCommandFactory.buildDeleteAllRows(cmdId, "DoorVSTimezone");
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), "");
    }

    // 删除全部人不同时段的验证方式数据
    @Override
    public void deleteAllPersonalVSTimezone(String sn) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        int cmdId = (int) commandSeqService.nextSeq(sn);
        String cmd = AccessCommandFactory.buildDeleteAllRows(cmdId, "PersonalVSTimezone");
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), "");
    }

    // 删除全部输入控制（受时间段限制）数据
    @Override
    public void deleteAllInputIOSetting(String sn) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        int cmdId = (int) commandSeqService.nextSeq(sn);
        String cmd = AccessCommandFactory.buildDeleteAllRows(cmdId, "InputIOSetting");
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), "");
    }

    // 删除全部时间组
    @Override
    public void deleteAllTimezone(String sn) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        int cmdId = (int) commandSeqService.nextSeq(sn);
        String cmd = AccessCommandFactory.buildDeleteAllRows(cmdId, "timezone");
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), "");
    }

    // 删除指定时间段（按 TimeZoneId）
    @Override
    public void deleteTimezoneById(String sn, int timezoneId) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        int cmdId = (int) commandSeqService.nextSeq(sn);
        String cmd = AccessCommandFactory.buildDeleteTimezoneById(cmdId, timezoneId);
        iotDeviceCommandService.enqueueCommands(sn, java.util.List.of(cmd), "");
    }

    // 删除全部节假日
    @Override
    public void deleteAllHoliday(String sn) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        int cmdId = (int) commandSeqService.nextSeq(sn);
        String cmd = AccessCommandFactory.buildDeleteAllRows(cmdId, "holiday");
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), "");
    }

    // 删除全部多卡开门数据
    @Override
    public void deleteAllMultimcard(String sn) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        int cmdId = (int) commandSeqService.nextSeq(sn);
        String cmd = AccessCommandFactory.buildDeleteAllRows(cmdId, "multimcard");
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), "");
    }

    @Override
    public void setInterlock(String sn, Integer code) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        controlDeviceCommandDispatcher.dispatchSetOptions(
                sn,
                List.of(OptionsCommandFactory.setOptions(Map.of("InterLock", String.valueOf(code == null ? 0 : code)))),
                ""
        );
    }

    @Override
    public void setBacktracking(String sn, Integer code) {
        if (StringUtils.isBlank(sn)) {
            return;
        }
        controlDeviceCommandDispatcher.dispatchSetOptions(
                sn,
                List.of(OptionsCommandFactory.setOptions(Map.of("AntiPassback", String.valueOf(code == null ? 0 : code)))),
                ""
        );
    }

    @Override
    public void setDoorFirstCardOpenDoor(String sn, Map<String, Integer> params) {
        if (StringUtils.isBlank(sn) || params == null || params.isEmpty()) {
            return;
        }
        controlDeviceCommandDispatcher.dispatchSetOptions(
                sn,
                List.of(OptionsCommandFactory.setOptions(params)),
                ""
        );
    }

    @Override
    public void setDoorRelatedParameter(String sn, Map<String, Integer> params) {
        if (StringUtils.isBlank(sn) || params == null || params.isEmpty()) {
            return;
        }
        controlDeviceCommandDispatcher.dispatchSetOptions(
                sn,
                List.of(OptionsCommandFactory.setOptions(params)),
                ""
        );
    }

    @Override
    public void setReaderOfflineAccess(String sn, Map<String, Integer> params) {
        if (StringUtils.isBlank(sn) || params == null || params.isEmpty()) {
            return;
        }
        controlDeviceCommandDispatcher.dispatchSetOptions(
                sn,
                List.of(OptionsCommandFactory.setOptions(params)),
                ""
        );
    }

    @Override
    public void setDoorMaskFlag(String sn, Map<String, Integer> params) {
        if (StringUtils.isBlank(sn) || params == null || params.isEmpty()) {
            return;
        }
        controlDeviceCommandDispatcher.dispatchSetOptions(
                sn,
                List.of(OptionsCommandFactory.setOptions(params)),
                ""
        );
    }

    @Override
    public void setMultiCardOpenDoor(String sn, Map<String, Integer> params) {
        if (StringUtils.isBlank(sn) || params == null || params.isEmpty()) {
            return;
        }
        controlDeviceCommandDispatcher.dispatchSetOptions(
                sn,
                List.of(OptionsCommandFactory.setOptions(params)),
                ""
        );
    }

    @Override
    public void updateInputIOSetting(String sn, Map<String, Integer> params) {
        if (StringUtils.isBlank(sn) || params == null || params.isEmpty()) {
            return;
        }
        int cmdId = (int) commandSeqService.nextSeq(sn);
        String cmd = AccessCommandFactory.buildUpdateInputIOSetting(cmdId, params);
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), "");
    }

    @Override
    public void updateTimezone(String sn, Map<String, ?> params) {
        if (StringUtils.isBlank(sn) || params == null || params.isEmpty()) {
            return;
        }
        int cmdId = (int) commandSeqService.nextSeq(sn);
        String cmd = AccessCommandFactory.buildUpdateTimezone(cmdId, params);
        iotDeviceCommandService.enqueueCommands(sn, List.of(cmd), "");
    }

    @Override
    public void addUserWithAuthorize(String sn, String pin, String name, Integer authorizeTimezoneId, Integer authorizeDoorId, Integer devId) {
        // 兼容旧调用：不传用户图片与抠图
        addUserWithAuthorize(sn, pin, name, authorizeTimezoneId, authorizeDoorId, devId, null, null);
    }

    /**
     * 下发人员新增（含权限）命令序列；固定 4 条基础命令：user、userauthorize、userpic、biophoto。
     * 增强版：支持传入用户图片(userPic)与抠图(bioPhoto)，可为空。
     * - 当传入为空时，使用 URL 空串占位，确保 4 条命令始终下发；
     * - 当传入为 data:URI 或 Base64 字符串时，识别为 Base64 下发；
     * - 当传入为 URL（含 "://"、以 "/" 或 "file:" 开头）时，识别为 URL 下发。
     */
    @Override
    public void addUserWithAuthorize(String sn, String pin, String name,
                                     Integer authorizeTimezoneId, Integer authorizeDoorId, Integer devId,
                                     String userPic, String bioPhoto) {
        if (StringUtils.isAnyBlank(sn, pin)) {
            return;
        }
        int startCmdId = (int) commandSeqService.nextSeqRange(sn, 4);

        // 用户信息（按协议字段）
        AccessCommandFactory.CmdUser user = new AccessCommandFactory.CmdUser(pin);
        user.name = name;
        user.group = "0";      // 默认门禁组
        user.privilege = "0";  // 普通权限
        user.disable = "0";    // 非黑名单
        // user.verify = "0";     // 默认验证方式（跟随设备）
        user.starttime = "0";
        user.endtime = "0";
        user.cardno = "";
        user.password = "";

        // 门禁授权（单条，必要时可扩展为多条）
        Integer tzId = authorizeTimezoneId == null ? 1 : authorizeTimezoneId;
        Integer doorId = authorizeDoorId == null ? 1 : authorizeDoorId; // 15: ALL DOORS
        AccessCommandFactory.CmdUserAuthorize ua = new AccessCommandFactory.CmdUserAuthorize(pin, tzId, doorId);
        ua.devId = (devId == null ? 1 : devId);

        // 图片/抠图（均允许为空，保持 4 条命令序列一致）
        AccessCommandFactory.CmdUserPic userPicCmd = null;
        if (StringUtils.isNotBlank(userPic)) {
            userPicCmd = buildUserPicCmd(pin, userPic);
        }
        AccessCommandFactory.CmdBioPhoto bioPhotoCmd = null;
        if (StringUtils.isNotBlank(bioPhoto)) {
            bioPhotoCmd = buildBioPhotoCmd(pin, bioPhoto);
        }

        List<String> cmds = AccessCommandFactory.buildAddUserBundle(startCmdId, user, java.util.List.of(ua), userPicCmd, bioPhotoCmd);
        log.info("[IoT] 下发人员新增(4条) sn={}, pin={}, tzId={}, doorId={}, devId={}, userPicFormat={}, bioPhotoFormat={}, hasUserPic={}, hasBioPhoto={}",
                sn, pin, tzId, doorId, ua.devId,
                (StringUtils.isBlank(userPic) ? "placeholder" : (isDataUri(userPic) ? "base64" : (isLikelyUrl(userPic) ? "url" : (isLikelyBase64(userPic) ? "base64" : "url")))),
                (StringUtils.isBlank(bioPhoto) ? "placeholder" : (isDataUri(bioPhoto) ? "base64" : (isLikelyUrl(bioPhoto) ? "url" : (isLikelyBase64(bioPhoto) ? "base64" : "url")))),
                StringUtils.isNotBlank(userPic), StringUtils.isNotBlank(bioPhoto));
        iotDeviceCommandService.enqueueCommands(sn, cmds, "");
    }

    /**
     * 增强版：支持卡号与管理密码
     */
    @Override
    public void addUserWithAuthorize(String sn, String pin, String name,
                                     Integer authorizeTimezoneId, Integer authorizeDoorId, Integer devId,
                                     String userPic, String bioPhoto,
                                     String cardNumber, String verifyPassword) {
        if (StringUtils.isAnyBlank(sn, pin)) {
            return;
        }
        int startCmdId = (int) commandSeqService.nextSeqRange(sn, 4);

        // 用户信息（按协议字段）
        AccessCommandFactory.CmdUser user = new AccessCommandFactory.CmdUser(pin);
        user.name = name;
        user.group = "0";
        user.privilege = "0";
        user.disable = "0";
        user.starttime = "0";
        user.endtime = "0";
        user.cardno = StringUtils.defaultString(StringUtils.trimToNull(cardNumber), "");
        user.password = StringUtils.defaultString(StringUtils.trimToNull(verifyPassword), "");

        // 门禁授权
        Integer tzId = authorizeTimezoneId == null ? 1 : authorizeTimezoneId;
        Integer doorId = authorizeDoorId == null ? 1 : authorizeDoorId;
        AccessCommandFactory.CmdUserAuthorize ua = new AccessCommandFactory.CmdUserAuthorize(pin, tzId, doorId);
        ua.devId = (devId == null ? 1 : devId);

        // 图片/抠图
        AccessCommandFactory.CmdUserPic userPicCmd = null;
        if (StringUtils.isNotBlank(userPic)) {
            userPicCmd = buildUserPicCmd(pin, userPic);
        }
        AccessCommandFactory.CmdBioPhoto bioPhotoCmd = null;
        if (StringUtils.isNotBlank(bioPhoto)) {
            bioPhotoCmd = buildBioPhotoCmd(pin, bioPhoto);
        }

        List<String> cmds = AccessCommandFactory.buildAddUserBundle(startCmdId, user, java.util.List.of(ua), userPicCmd, bioPhotoCmd);
        log.info("[IoT] 下发人员新增(4条-含卡/密) sn={}, pin={}, tzId={}, doorId={}, devId={}, cardno={}, hasPwd={}, userPicFormat={}, bioPhotoFormat={}, hasUserPic={}, hasBioPhoto={}",
                sn, pin, tzId, doorId, ua.devId,
                user.cardno, StringUtils.isNotBlank(user.password),
                (StringUtils.isBlank(userPic) ? "placeholder" : (isDataUri(userPic) ? "base64" : (isLikelyUrl(userPic) ? "url" : (isLikelyBase64(userPic) ? "base64" : "url")))),
                (StringUtils.isBlank(bioPhoto) ? "placeholder" : (isDataUri(bioPhoto) ? "base64" : (isLikelyUrl(bioPhoto) ? "url" : (isLikelyBase64(bioPhoto) ? "base64" : "url")))),
                StringUtils.isNotBlank(userPic), StringUtils.isNotBlank(bioPhoto));
        iotDeviceCommandService.enqueueCommands(sn, cmds, "");
    }

    /**
     * 增强版：支持业务字段（超级用户、设备操作权限、扩展权限、有效期）。
     */
    @Override
    public void addUserWithAuthorize(String sn, String pin, String name,
                                     Integer authorizeTimezoneId, Integer authorizeDoorId, Integer devId,
                                     String userPic, String bioPhoto,
                                     String cardNumber, String verifyPassword,
                                     Integer superUser, Integer deviceOpPerm,
                                     Boolean extendAccess, Boolean prohibitedRoster, Boolean validTimeEnabled,
                                     java.util.Date validStartTime, java.util.Date validEndTime) {
        if (StringUtils.isAnyBlank(sn, pin)) {
            return;
        }
        int startCmdId = (int) commandSeqService.nextSeqRange(sn, 4);

        // 用户信息（按协议字段）
        AccessCommandFactory.CmdUser user = new AccessCommandFactory.CmdUser(pin);
        user.name = name;
        user.group = "0";
        // privilege 映射：deviceOpPerm（预留）
        String privilege = "0";
        if (deviceOpPerm != null) {
            // 设备操作权限(1一般人员,2管理员,3操作员)
            // 简易映射：14=管理员，2=操作员，0=普通
            privilege = (deviceOpPerm == 1) ? "0" : (deviceOpPerm == 2 ? "14" : "2");
        }
        user.privilege = privilege;
        user.disable = (Boolean.TRUE.equals(prohibitedRoster) ? "1" : "0"); // 禁止名单映射为黑名单

        // 有效期：开启则按纪元秒传入；否则置为 "0"
        boolean useValidTime = Boolean.TRUE.equals(validTimeEnabled) && validStartTime != null && validEndTime != null;

        String startSec = "0";
        String endSec = "0";
        // 如果设备参数DateFmtFunOn的值为1，举例：StartTime=583512660，StartTime由【附录5】算法计算出来
        // 如果设备参数DateFmtFunOn的值为0，格式为YYYYMMDD
        // 值为0，表示该用户无开始时间限制

        startSec = useValidTime ? new SimpleDateFormat("yyyyMMdd").format(validStartTime) : "0";
        endSec = useValidTime ? new SimpleDateFormat("yyyyMMdd").format(validEndTime) : "0";
        ;
        user.starttime = startSec;
        user.endtime = endSec;

        user.cardno = StringUtils.defaultString(StringUtils.trimToNull(cardNumber), "");
        user.password = StringUtils.defaultString(StringUtils.trimToNull(verifyPassword), "");

        // 门禁授权（带有效期）
        Integer tzId = authorizeTimezoneId == null ? 1 : authorizeTimezoneId;
        Integer doorId = authorizeDoorId == null ? 1 : authorizeDoorId;
        AccessCommandFactory.CmdUserAuthorize ua = new AccessCommandFactory.CmdUserAuthorize(pin, tzId, doorId);
        ua.devId = (devId == null ? 1 : devId);
        ua.startTime = startSec;
        ua.endTime = endSec;

        // 图片/抠图
        AccessCommandFactory.CmdUserPic userPicCmd = null;
        if (StringUtils.isNotBlank(userPic)) {
            userPicCmd = buildUserPicCmd(pin, userPic);
        }
        AccessCommandFactory.CmdBioPhoto bioPhotoCmd = null;
        if (StringUtils.isNotBlank(bioPhoto)) {
            bioPhotoCmd = buildBioPhotoCmd(pin, bioPhoto);
        }

        java.util.List<String> cmds = AccessCommandFactory.buildAddUserBundle(startCmdId, user, java.util.List.of(ua), userPicCmd, bioPhotoCmd);
        log.info("[IoT] 下发人员新增(4条-含业务) sn={}, pin={}, tzId={}, doorId={}, devId={}, cardno={}, hasPwd={}, privilege={}, validEnabled={}, startSec={}, endSec={}, userPicFormat={}, bioPhotoFormat={}, hasUserPic={}, hasBioPhoto={}",
                sn, pin, tzId, doorId, ua.devId,
                user.cardno, StringUtils.isNotBlank(user.password), user.privilege,
                useValidTime, startSec, endSec,
                (StringUtils.isBlank(userPic) ? "placeholder" : (isDataUri(userPic) ? "base64" : (isLikelyUrl(userPic) ? "url" : (isLikelyBase64(userPic) ? "base64" : "url")))),
                (StringUtils.isBlank(bioPhoto) ? "placeholder" : (isDataUri(bioPhoto) ? "base64" : (isLikelyUrl(bioPhoto) ? "url" : (isLikelyBase64(bioPhoto) ? "base64" : "url")))),
                StringUtils.isNotBlank(userPic), StringUtils.isNotBlank(bioPhoto));
        iotDeviceCommandService.enqueueCommands(sn, cmds, "");
    }

    @Override
    public void addUserAuthorize(String sn, String pin, Integer authorizeTimezoneId, Integer authorizeDoorId, Integer devId) {
        if (StringUtils.isAnyBlank(sn, pin)) {
            return;
        }
        int startCmdId = (int) commandSeqService.nextSeqRange(sn, 1);

        // 门禁授权（带有效期）
        Integer tzId = authorizeTimezoneId == null ? 1 : authorizeTimezoneId;
        Integer doorId = authorizeDoorId == null ? 1 : authorizeDoorId;
        AccessCommandFactory.CmdUserAuthorize ua = new AccessCommandFactory.CmdUserAuthorize(pin, tzId, doorId);
        ua.devId = (devId == null ? 1 : devId);
        ua.startTime = null;
        ua.endTime = null;

        java.util.List<String> cmds = AccessCommandFactory.buildAddUserAuthorizeBundle(startCmdId, java.util.List.of(ua));
        log.info("[IoT] 下发人员时间端权限 sn={}, pin={}, tzId={}, doorId={}, devId={}, startTime={}, endTime={}",
                sn, pin, tzId, doorId, ua.devId, ua.startTime, ua.endTime);
        iotDeviceCommandService.enqueueCommands(sn, cmds, "");
    }

    /** 根据传入字符串构造用户图片命令对象 */
    private AccessCommandFactory.CmdUserPic buildUserPicCmd(String pin, String pic) {
        if (StringUtils.isBlank(pic)) {
            return AccessCommandFactory.CmdUserPic.fromUrl(pin, "");
        }
        String val = pic.trim();
        if (isDataUri(val)) {
            String base64 = extractDataUriContent(val);
            return AccessCommandFactory.CmdUserPic.fromBase64(pin, base64);
        }
        if (isLikelyBase64(val)) {
            return AccessCommandFactory.CmdUserPic.fromBase64(pin, val);
        }
        if (isLikelyUrl(val)) {
            return AccessCommandFactory.CmdUserPic.fromUrl(pin, val);
        }
        return AccessCommandFactory.CmdUserPic.fromUrl(pin, val);
    }

    /** 根据传入字符串构造比对照片命令对象 */
    private AccessCommandFactory.CmdBioPhoto buildBioPhotoCmd(String pin, String photo) {
        if (StringUtils.isBlank(photo)) {
            return AccessCommandFactory.CmdBioPhoto.fromUrl(pin, "");
        }
        String val = photo.trim();
        if (isDataUri(val)) {
            String base64 = extractDataUriContent(val);
            return AccessCommandFactory.CmdBioPhoto.fromBase64(pin, base64);
        }
        if (isLikelyBase64(val)) {
            return AccessCommandFactory.CmdBioPhoto.fromBase64(pin, val);
        }
        if (isLikelyUrl(val)) {
            return AccessCommandFactory.CmdBioPhoto.fromUrl(pin, val);
        }
        return AccessCommandFactory.CmdBioPhoto.fromUrl(pin, val);
    }

    /** 是否为 data URI（如：data:image/jpeg;base64,xxxxx） */
    private boolean isDataUri(String s) {
        return s != null && s.regionMatches(true, 0, "data:", 0, 5);
    }

    /** 提取 data URI 中逗号后的 Base64 内容 */
    private String extractDataUriContent(String dataUri) {
        if (dataUri == null) return null;
        int idx = dataUri.indexOf(',');
        return idx >= 0 ? dataUri.substring(idx + 1) : dataUri;
    }

    /** 简易 URL 判定：包含 "://" 或以 "/"、"file:" 开头 */
    private boolean isLikelyUrl(String s) {
        if (s == null) return false;
        String v = s.trim();
        return v.contains("://") || v.startsWith("/") || v.regionMatches(true, 0, "file:", 0, 5);
    }

    /**
     * 宽松的 Base64 判定：
     * - 去除所有空白字符后仅包含 Base64 字符集 A-Z a-z 0-9 + / =
     * - 长度至少 16 且为 4 的倍数
     */
    private boolean isLikelyBase64(String s) {
        if (s == null) return false;
        String v = s.trim();
        if (v.isEmpty()) return false;
        // 去掉空白
        v = v.replaceAll("\\s+", "");
        if (v.length() < 16 || (v.length() % 4 != 0)) return false;
        // 只允许 Base64 字符
        if (!v.matches("^[A-Za-z0-9+/]+={0,2}$")) return false;
        return true;
    }

    @Override
    public void removeUserAndAuthorize(String sn, String pin) {
        if (StringUtils.isAnyBlank(sn, pin)) {
            return;
        }
        int id = (int) commandSeqService.nextSeqRange(sn, 4);
        List<String> cmds = new java.util.ArrayList<>();
        // 人员删除按 4 条命令：userauthorize、biophoto、userpic、user
        cmds.add(AccessCommandFactory.buildDeleteUserAuthorize(id++, pin));
        cmds.add(AccessCommandFactory.buildDeleteBioPhoto(id++, pin, 9));
        cmds.add(AccessCommandFactory.buildDeleteUserPic(id++, pin));
        cmds.add(AccessCommandFactory.buildDeleteUser(id++, pin));
        log.info("[IoT] 下发人员删除(4条) sn={}, pin={}", sn, pin);
        iotDeviceCommandService.enqueueCommands(sn, cmds, "");
    }

    @Override
    public void removeAuthorize(String sn, String pin) {
        if (StringUtils.isAnyBlank(sn, pin)) {
            return;
        }
        int id = (int) commandSeqService.nextSeqRange(sn, 1);
        List<String> cmds = new java.util.ArrayList<>();
        cmds.add(AccessCommandFactory.buildDeleteUserAuthorize(id++, pin));
        log.info("[IoT] 下发人员删除时间规则(1条) sn={}, pin={}", sn, pin);
        iotDeviceCommandService.enqueueCommands(sn, cmds, "");
    }

    @Override
    public void removeUserPicAndBioPhoto(String sn, String pin) {
        if (StringUtils.isAnyBlank(sn, pin)) {
            return;
        }
        int id = (int) commandSeqService.nextSeqRange(sn, 2);
        List<String> cmds = new java.util.ArrayList<>();
        // 人员删除按 2 条命令：biophoto、userpic
        cmds.add(AccessCommandFactory.buildDeleteBioPhoto(id++, pin, 9));
        cmds.add(AccessCommandFactory.buildDeleteUserPic(id++, pin));
        log.info("[IoT] 下发人员删除图片和比对模版(2条) sn={}, pin={}", sn, pin);
        iotDeviceCommandService.enqueueCommands(sn, cmds, "");
    }
}
