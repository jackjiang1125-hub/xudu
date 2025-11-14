package org.jeecg.modules.acc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.acc.entity.AccDoor;
import org.jeecg.modules.acc.mapper.AccDoorMapper;
import org.jeecg.modules.acc.mapstruct.AccDoorMapstruct;
import org.jeecg.modules.acc.service.IAccDoorService;
import org.jeecg.modules.acc.vo.AccDoorVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.stereotype.Service;
import org.jeecgframework.boot.iot.api.IotDeviceService;
import java.util.List;

/**
 * 门列表 ServiceImpl
 */
@Service
public class AccDoorServiceImpl extends JeecgServiceImpl<AccDoorMapper, AccDoor> implements IAccDoorService {

    @Autowired
    private AccDoorMapstruct accDoorMapstruct;

    @Autowired
    private IotDeviceService iotDeviceService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;


    /**
     * 判断设备是否在线：基于最新心跳时间戳（Redis键：iot:acc:heartbeat:<sn>），5秒内视为在线。
     */
    private boolean isDeviceOnline(String sn) {
        if (StringUtils.isBlank(sn)) {
            return false;
        }
        try {
            String json = redisTemplate.opsForValue().get("iot:acc:heartbeat:" + sn);
            if (StringUtils.isNotBlank(json)) {
                JsonNode node = objectMapper.readTree(json);
                if (node != null && node.has("timestamp")) {
                    long ts = node.get("timestamp").asLong();
                    return (System.currentTimeMillis() - ts) <= 5_000L;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }


    @Override
    public IPage<AccDoor> pageDoors(String deviceName, String doorName, String ipAddress, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<AccDoor> qw = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(deviceName)) {
            qw.like(AccDoor::getDeviceName, deviceName);
        }
        if (StringUtils.isNotBlank(doorName)) {
            qw.like(AccDoor::getDoorName, doorName);
        }
        if (StringUtils.isNotBlank(ipAddress)) {
            qw.eq(AccDoor::getIpAddress, ipAddress);
        }
        Page<AccDoor> page = new Page<>(pageNo == null ? 1 : pageNo, pageSize == null ? 10 : pageSize);
        return this.page(page, qw);
    }

    @Override
    public AccDoor saveFromVO(AccDoorVO vo) {
        AccDoor entity = accDoorMapstruct.toEntity(vo);
        if (entity.getId() != null) {
            this.updateById(entity);
        } else {
            this.save(entity);
        }
        return entity;
    }

    @Override
    public void removeByDeviceSn(String deviceSn) {
        if (deviceSn == null) {
            return;
        }
        // 使用 Lambda 与列名两种方式删除，提升兼容性
        this.remove(new LambdaQueryWrapper<AccDoor>().eq(AccDoor::getDeviceSn, deviceSn));
    }

    @Override
    public void remoteOpenDoors(List<String> doorIds, Integer pulseSeconds, String operator) {
        if (doorIds == null || doorIds.isEmpty()) {
            return;
        }
        for (String id : doorIds) {
            AccDoor door = this.getById(id);
            if (door == null) {
                continue;
            }
            String sn = door.getDeviceSn();
            Integer doorNumber = door.getDoorNumber();
            if (sn == null || sn.isBlank() || doorNumber == null || doorNumber <= 0) {
                continue;
            }
            // 在线检测
            if (!isDeviceOnline(sn)) {
                continue;
            }
            // 统一调用 IoT Service 的远程开门方法
            iotDeviceService.remoteOpenDoor(sn, doorNumber, pulseSeconds, operator);
        }
    }

    @Override
    public void remoteCloseDoors(List<String> doorIds, String operator) {
        if (doorIds == null || doorIds.isEmpty()) {
            return;
        }
        for (String id : doorIds) {
            AccDoor door = this.getById(id);
            if (door == null) {
                continue;
            }
            String sn = door.getDeviceSn();
            Integer doorNumber = door.getDoorNumber();
            if (sn == null || sn.isBlank() || doorNumber == null || doorNumber <= 0) {
                continue;
            }
            // 在线检测
            if (!isDeviceOnline(sn)) {
                continue;
            }
            // 统一调用 IoT Service 的远程关门/锁定方法
            iotDeviceService.remoteCloseDoor(sn, doorNumber, operator);
        }
    }

    @Override
    public void remoteCancelAlarmDoors(List<String> doorIds, String operator) {
        if (doorIds == null || doorIds.isEmpty()) {
            return;
        }
        for (String id : doorIds) {
            AccDoor door = this.getById(id);
            if (door == null) {
                continue;
            }
            String sn = door.getDeviceSn();
            Integer doorNumber = door.getDoorNumber();
            if (sn == null || sn.isBlank() || doorNumber == null || doorNumber <= 0) {
                continue;
            }
            if (!isDeviceOnline(sn)) {
                continue;
            }
            iotDeviceService.remoteCancelAlarm(sn, doorNumber, operator);
        }
    }

    @Override
    public void remoteHoldOpenDoors(List<String> doorIds, String operator) {
        if (doorIds == null || doorIds.isEmpty()) {
            return;
        }
        for (String id : doorIds) {
            AccDoor door = this.getById(id);
            if (door == null) {
                continue;
            }
            String sn = door.getDeviceSn();
            Integer doorNumber = door.getDoorNumber();
            if (sn == null || sn.isBlank() || doorNumber == null || doorNumber <= 0) {
                continue;
            }
            if (!isDeviceOnline(sn)) {
                continue;
            }
            iotDeviceService.remoteHoldOpen(sn, doorNumber, operator);
        }
    }

    @Override
    public void remoteLockDoors(List<String> doorIds, String operator) {
        if (doorIds == null || doorIds.isEmpty()) {
            return;
        }
        for (String id : doorIds) {
            AccDoor door = this.getById(id);
            if (door == null) {
                continue;
            }
            String sn = door.getDeviceSn();
            Integer doorNumber = door.getDoorNumber();
            if (sn == null || sn.isBlank() || doorNumber == null || doorNumber <= 0) {
                continue;
            }
            if (!isDeviceOnline(sn)) {
                continue;
            }
            iotDeviceService.remoteLockDoor(sn, doorNumber, operator);
        }
    }

    @Override
    public void remoteUnlockDoors(List<String> doorIds, String operator) {
        if (doorIds == null || doorIds.isEmpty()) {
            return;
        }
        for (String id : doorIds) {
            AccDoor door = this.getById(id);
            if (door == null) {
                continue;
            }
            String sn = door.getDeviceSn();
            Integer doorNumber = door.getDoorNumber();
            if (sn == null || sn.isBlank() || doorNumber == null || doorNumber <= 0) {
                continue;
            }
            if (!isDeviceOnline(sn)) {
                continue;
            }
            iotDeviceService.remoteUnlockDoor(sn, doorNumber, operator);
        }
    }

    @Override
    public void enableTodayAlwaysOpen(List<String> doorIds, String operator) {
        if (doorIds == null || doorIds.isEmpty()) {
            return;
        }
        for (String id : doorIds) {
            AccDoor door = this.getById(id);
            if (door == null) {
                continue;
            }
            String sn = door.getDeviceSn();
            Integer doorNumber = door.getDoorNumber();
            if (sn == null || sn.isBlank() || doorNumber == null || doorNumber <= 0) {
                continue;
            }
            if (!isDeviceOnline(sn)) {
                continue;
            }
            iotDeviceService.enableTodayAlwaysOpen(sn, doorNumber, operator);
        }
    }

    @Override
    public void disableTodayAlwaysOpen(List<String> doorIds, String operator) {
        if (doorIds == null || doorIds.isEmpty()) {
            return;
        }
        for (String id : doorIds) {
            AccDoor door = this.getById(id);
            if (door == null) {
                continue;
            }
            String sn = door.getDeviceSn();
            Integer doorNumber = door.getDoorNumber();
            if (sn == null || sn.isBlank() || doorNumber == null || doorNumber <= 0) {
                continue;
            }
            if (!isDeviceOnline(sn)) {
                continue;
            }
            iotDeviceService.disableTodayAlwaysOpen(sn, doorNumber, operator);
        }
    }
}