package org.jeecg.modules.iot.device.service.impl;


import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
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

import org.jeecg.modules.iot.device.service.IotDeviceCommandService;
import org.jeecg.modules.iot.device.seq.CommandSeqService;
import org.jeecg.modules.iot.utils.zkteco.AccessCommandFactory;
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

}
