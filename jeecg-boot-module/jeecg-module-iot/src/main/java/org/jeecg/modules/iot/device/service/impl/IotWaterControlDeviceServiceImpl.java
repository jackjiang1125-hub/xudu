package org.jeecg.modules.iot.device.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.iot.device.entity.IotDevice;
import org.jeecg.modules.iot.device.enums.IotDeviceStatus;
import org.jeecg.modules.iot.device.service.IotDeviceInnerService;
import org.jeecg.modules.iot.device.mapper.IotDeviceMapper;
import org.jeecgframework.boot.iot.api.IotWaterControlDeviceService;
import org.jeecgframework.boot.iot.vo.IotWaterControlDeviceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.jeecg.modules.iot.device.protocol.WaterDeviceSessionManager;

import org.jeecg.modules.iot.util.Crc16Modbus;
import org.jeecg.modules.iot.util.HexUtil;
import java.nio.charset.StandardCharsets;

import org.jeecg.common.util.RedisUtil;

@Service
public class IotWaterControlDeviceServiceImpl implements IotWaterControlDeviceService {

    @Autowired
    private IotDeviceMapper iotDeviceMapper;
    
    @Autowired
    private IotDeviceInnerService iotDeviceInnerService;
    
    @Autowired
    private WaterDeviceSessionManager sessionManager;
    
    @Autowired
    private RedisUtil redisUtil;
    
    private static final String REDIS_KEY_PREFIX_AUTH = "iot:water:auth:";
    private static final String REDIS_KEY_PREFIX_HEARTBEAT = "iot:water:heartbeat:";

    @Override
    public List<IotWaterControlDeviceVO> queryPendingWaterDevices(String keyword) {
        LambdaQueryWrapper<IotDevice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(IotDevice::getDeviceType, "water_control");
        // PENDING 状态 或者 未授权
        wrapper.and(w -> w.eq(IotDevice::getStatus, IotDeviceStatus.PENDING)
                .or()
                .eq(IotDevice::getAuthorized, false));
        
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(IotDevice::getSn, keyword)
                    .or()
                    .like(IotDevice::getIpAddress, keyword));
        }
        wrapper.orderByDesc(IotDevice::getLastInitTime);
        
        List<IotDevice> list = iotDeviceMapper.selectList(wrapper);
        if (list == null || list.isEmpty()) {
            return Collections.emptyList();
        }
        
        return list.stream().map(d -> {
            IotWaterControlDeviceVO vo = new IotWaterControlDeviceVO();
            vo.setId(d.getId());
            vo.setSn(d.getSn());
            vo.setDeviceName(d.getDeviceName());
            vo.setIpAddress(d.getIpAddress());
            vo.setDeviceType(d.getDeviceType());
            vo.setStatus(d.getStatus() != null ? d.getStatus().name() : "UNKNOWN");
            vo.setLastInitTime(d.getLastInitTime());
            vo.setLastHeartbeatTime(d.getLastHeartbeatTime());
            if (vo.getIpAddress() == null) {
                vo.setIpAddress(d.getLastKnownIp());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public boolean setAuthorization(String sn, boolean authorized) {
        if (StringUtils.isBlank(sn)) return false;
        
        IotDeviceStatus status = authorized ? IotDeviceStatus.AUTHORIZED : IotDeviceStatus.PENDING;
        // Note: The real status logic might be more complex (e.g. keep ONLINE if just re-authorizing)
        // But for initial binding, we usually set it to AUTHORIZED (which might then become ONLINE on next heartbeat)
        // Or if it was PENDING, set to AUTHORIZED.
        
        iotDeviceInnerService.updateStatus(sn, status, authorized);
        return true;
    }

    @Override
    public void updateDeviceName(String sn, String deviceName) {
        if (StringUtils.isBlank(sn) || StringUtils.isBlank(deviceName)) return;
        
        // 1. Update in DB
        IotDevice device = iotDeviceMapper.selectOne(new LambdaQueryWrapper<IotDevice>().eq(IotDevice::getSn, sn));
        if (device != null) {
            device.setDeviceName(deviceName);
            iotDeviceMapper.updateById(device);
            
            // 2. Send command to device
            if (device.getIpAddress() != null) {
                try {
                    byte[] command = buildUpdateNameCommand(sn, deviceName);
                    sessionManager.sendTo(device.getIpAddress(), command);
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    @Override
    public void clearDeviceData(String sn) {
        if (StringUtils.isBlank(sn)) return;
        
        IotDevice device = iotDeviceMapper.selectOne(new LambdaQueryWrapper<IotDevice>().eq(IotDevice::getSn, sn));
        if (device != null && device.getIpAddress() != null) {
            try {
                byte[] command = buildClearDataCommand(sn);
                sessionManager.sendTo(device.getIpAddress(), command);
            } catch (Exception e) {
                // ignore
            }
        }
    }

    private byte[] buildUpdateNameCommand(String sn, String deviceName) {
        byte[] nameBytes;
        try {
            nameBytes = deviceName.getBytes("GB2312");
        } catch (Exception e) {
            nameBytes = deviceName.getBytes(StandardCharsets.UTF_8);
        }

        // 截取前14个字节，并填充到16字节（GB2312）
        byte[] data = new byte[16];
        int n = Math.min(14, nameBytes.length);
        if (n > 0) System.arraycopy(nameBytes, 0, data, 0, n);
        for (int i = n; i < 16; i++) data[i] = 0x20; // 填充空格

        byte[] addr = getAddrBytes(sn);
        int dataLen = 1 + data.length; // 1(cmd) + 16(data)
        
        byte[] payload = new byte[6 + dataLen];
        payload[0] = (byte) 0xFE;
        payload[1] = 0x03;
        payload[2] = addr[0];
        payload[3] = addr[1];
        payload[4] = (byte) ((dataLen >>> 8) & 0xFF);
        payload[5] = (byte) (dataLen & 0xFF);
        payload[6] = (byte) 0x48; // 功能码 0x48
        System.arraycopy(data, 0, payload, 7, data.length);
        
        return Crc16Modbus.appendCrcCustom(payload, 2, false, false);
    }

    private byte[] buildClearDataCommand(String sn) {
        byte[] addr = getAddrBytes(sn);
        int dataLen = 1; 
        
        byte[] payload = new byte[6 + dataLen];
        payload[0] = (byte) 0xFE;
        payload[1] = 0x03;
        payload[2] = addr[0];
        payload[3] = addr[1];
        payload[4] = (byte) ((dataLen >>> 8) & 0xFF);
        payload[5] = (byte) (dataLen & 0xFF);
        payload[6] = (byte) 0x82; // Clear Data
        
        return Crc16Modbus.appendCrcCustom(payload, 2, false, false);
    }

    private byte[] getAddrBytes(String sn) {
        try {
            int addr = Integer.parseInt(sn);
            return new byte[]{(byte) ((addr >>> 8) & 0xFF), (byte) (addr & 0xFF)};
        } catch (NumberFormatException e) {
            return new byte[]{0x00, 0x01}; 
        }
    }

    @Override
    public void restartDevice(String sn) {
        sendCommand(sn, buildRestartCommand(sn));
    }

    @Override
    public void factoryResetDevice(String sn) {
        sendCommand(sn, buildFactoryResetCommand(sn));
    }
    
    private void sendCommand(String sn, byte[] command) {
        if (StringUtils.isBlank(sn) || command == null) return;
        IotDevice device = iotDeviceMapper.selectOne(new LambdaQueryWrapper<IotDevice>().eq(IotDevice::getSn, sn));
        if (device != null) {
            String targetIp = device.getLastKnownIp();
            if (StringUtils.isBlank(targetIp)) {
                targetIp = device.getIpAddress();
            }
            if (StringUtils.isNotBlank(targetIp)) {
                try {
                    sessionManager.sendTo(targetIp, command);
                } catch (Exception e) {
                    // ignore
                }
            }
        }
    }

    private byte[] buildRestartCommand(String sn) {
        byte[] addr = getAddrBytes(sn);
        int dataLen = 1; 
        byte[] payload = new byte[6 + dataLen];
        payload[0] = (byte) 0xFE;
        payload[1] = 0x03;
        payload[2] = addr[0];
        payload[3] = addr[1];
        payload[4] = (byte) ((dataLen >>> 8) & 0xFF);
        payload[5] = (byte) (dataLen & 0xFF);
        payload[6] = (byte) 0xC5; // Restart
        return Crc16Modbus.appendCrcCustom(payload, 2, false, false);
    }

    private byte[] buildFactoryResetCommand(String sn) {
        byte[] addr = getAddrBytes(sn);
        int dataLen = 1; 
        byte[] payload = new byte[6 + dataLen];
        payload[0] = (byte) 0xFE;
        payload[1] = 0x03;
        payload[2] = addr[0];
        payload[3] = addr[1];
        payload[4] = (byte) ((dataLen >>> 8) & 0xFF);
        payload[5] = (byte) (dataLen & 0xFF);
        payload[6] = (byte) 0x29; // Factory Reset
        return Crc16Modbus.appendCrcCustom(payload, 2, false, false);
    }

    @Override
    public void syncTime(String sn, Long timestamp) {

    }

    @Override
    public void removeCache(String sn) {
        if (StringUtils.isNotBlank(sn)) {
            redisUtil.del(REDIS_KEY_PREFIX_AUTH + sn);
            redisUtil.del(REDIS_KEY_PREFIX_HEARTBEAT + sn);
        }
    }

    @Override
    public void updateDeviceSn(String oldSn, String newSn) {
        if (StringUtils.isBlank(oldSn) || StringUtils.isBlank(newSn)) return;
        
        // 1. Update in DB
        IotDevice device = iotDeviceMapper.selectOne(new LambdaQueryWrapper<IotDevice>().eq(IotDevice::getSn, oldSn));
        if (device != null) {
            // Send command to device first (using old SN addressing)
            if (device.getLastKnownIp() != null || device.getIpAddress() != null) {
                try {
                    byte[] command = buildUpdateSnCommand(oldSn, newSn);
                    String targetIp = StringUtils.isNotBlank(device.getLastKnownIp()) ? device.getLastKnownIp() : device.getIpAddress();
                    if (StringUtils.isNotBlank(targetIp)) {
                        sessionManager.sendTo(targetIp, command);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            // 3. Clean old cache to force re-auth/re-check on next packet
            removeCache(oldSn);
        }
    }

    private byte[] buildUpdateSnCommand(String oldSn, String newSn) {
        byte[] addr = getAddrBytes(oldSn); // Use old SN for addressing
        byte[] newSnBytes = getAddrBytes(newSn); // New SN is 2 bytes data
        
        // Format: FE 03 [AddrH] [AddrL] [LenH] [LenL] [Cmd] [Data...]
        // Len = Length of (Cmd + Data)
        // Cmd = 1 byte (0x39)
        // Data = 2 bytes (new SN)
        // Total Len = 1 + 2 = 3 bytes
        
        int payloadLen = 1 + 2; 
        byte[] fixedPayload = new byte[6 + payloadLen]; // 6 + 3 = 9 bytes. Indices 0..8.
        
        fixedPayload[0] = (byte) 0xFE;
        fixedPayload[1] = 0x03;
        fixedPayload[2] = addr[0];
        fixedPayload[3] = addr[1];
        fixedPayload[4] = (byte) ((payloadLen >>> 8) & 0xFF);
        fixedPayload[5] = (byte) (payloadLen & 0xFF);
        fixedPayload[6] = (byte) 0x39; 
        fixedPayload[7] = newSnBytes[0];
        fixedPayload[8] = newSnBytes[1];
        
        return Crc16Modbus.appendCrcCustom(fixedPayload, 2, false, false);
    }

    @Override
    public void removeUserAndAuthorize(String sn, String userCode) {

    }

    @Override
    public void setDoorFirstCardOpenDoor(String sn, Map<String, Integer> params) {

    }

    @Override
    public Map<String, Object> getLatestOptionsBySn(String sn) {
        return null;
    }
}
