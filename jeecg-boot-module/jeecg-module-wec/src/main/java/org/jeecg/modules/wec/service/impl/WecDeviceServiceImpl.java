package org.jeecg.modules.wec.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.jeecg.modules.wec.entity.WecDevice;
import org.jeecg.modules.wec.mapper.WecDeviceMapper;
import org.jeecg.modules.wec.service.IWecDeviceService;
import org.jeecgframework.boot.iot.api.IotWaterControlDeviceService;
import org.jeecgframework.boot.iot.vo.IotWaterControlDeviceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import org.jeecgframework.boot.iot.vo.IotDeviceVO; // Keep for backward compatibility if interface needs it, or remove if we change interface
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import java.util.Arrays;

import org.jeecg.common.util.RedisUtil;

import org.jeecg.modules.wec.entity.WecRateTemplate;
import org.jeecg.modules.wec.service.IWecRateTemplateService;
import org.jeecgframework.boot.iot.vo.WaterRateConfigVO;
import java.math.BigDecimal;

@Service
public class WecDeviceServiceImpl extends ServiceImpl<WecDeviceMapper, WecDevice> implements IWecDeviceService {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    @Autowired
    private IotWaterControlDeviceService iotWaterControlDeviceService;

    @Autowired
    private IWecRateTemplateService wecRateTemplateService;

    @Override
    public void executeBatchControl(String cmd, String sns) {
        if (sns == null) return;
        List<String> snList = Arrays.asList(sns.split(","));
        
        for (String sn : snList) {
            if ("restart".equals(cmd)) {
                iotWaterControlDeviceService.restartDevice(sn);
            } else if ("factoryReset".equals(cmd)) {
                iotWaterControlDeviceService.factoryResetDevice(sn);
            } else if ("syncTime".equals(cmd)) {
                iotWaterControlDeviceService.syncTime(sn, System.currentTimeMillis());
            } else if ("stop".equals(cmd)) {
                updateStatusBySn(sn, "0");
            } else if ("start".equals(cmd)) {
                updateStatusBySn(sn, "1");
            } else if ("enableBlacklist".equals(cmd)) {
                updateNamelistModeBySn(sn, 1);
            } else if ("enableWhitelist".equals(cmd)) {
                updateNamelistModeBySn(sn, 0);
            }
        }
    }

    private void updateNamelistModeBySn(String sn, int mode) {
        WecDevice device = this.getOne(new QueryWrapper<WecDevice>().eq("sn", sn));
        if (device != null) {
            device.setNamelistMode(mode);
            this.updateById(device);
            iotWaterControlDeviceService.setNamelistMode(sn, mode);
        }
    }

    @Override
    public void updateRateConfigByTemplateId(String rateTemplateId) {
        if (rateTemplateId == null) return;
        
        // 1. Get Rate Template
        WecRateTemplate template = wecRateTemplateService.getById(rateTemplateId);
        if (template == null) return;
        
        // 2. Prepare Config VO
        WaterRateConfigVO config = new WaterRateConfigVO();
        // Map Work Mode
        String wm = template.getWorkMode();
        if ("real_time".equals(wm)) {
            config.setWorkMode(0);
        } else if ("pre_deduct".equals(wm)) {
            config.setWorkMode(1);
        } else if ("per_count".equals(wm)) {
            config.setWorkMode(2);
        } else if (wm != null) {
            try { config.setWorkMode(Integer.parseInt(wm)); } catch (Exception e) {}
        }

        // Map Deduction Method
        String dm = template.getDeductionMethod();
        if ("timed".equals(dm)) {
            config.setDeductionMethod(0);
        } else if ("pulse".equals(dm)) {
            config.setDeductionMethod(1);
        } else if (dm != null) {
            try { config.setDeductionMethod(Integer.parseInt(dm)); } catch (Exception e) {}
        }
        config.setFreeSeconds(template.getFreeSeconds());
        if (template.getRealTimeAmount() != null) {
            config.setRealTimeAmount(template.getRealTimeAmount().multiply(new BigDecimal(100)).intValue());
        }
        config.setRealTimeDuration(template.getRealTimeDuration());
        config.setPreDeductDuration(template.getPreDeductTime());
        config.setPerTimeDuration(template.getPerTimeDuration());
        
        // 3. Find all devices using this template
        List<WecDevice> devices = this.list(new QueryWrapper<WecDevice>().eq("rate_template_id", rateTemplateId));
        
        // 4. Send config to each device
        for (WecDevice device : devices) {
            if (device.getSn() != null) {
                // Check online status if needed, or just send (service handles offline/timeout)
                // For better performance, could use async or thread pool if many devices
                scheduler.submit(() -> {
                     try {
                         iotWaterControlDeviceService.sendRateConfig(device.getSn(), config);
                     } catch (Exception e) {
                         // log error
                         log.error("Failed to sync rate to device: " + device.getSn(), e);
                     }
                });
            }
        }
    }

    private void updateStatusBySn(String sn, String status) {
        WecDevice device = this.getOne(new QueryWrapper<WecDevice>().eq("sn", sn));
        if (device != null) {
            device.setStatus(status);
            this.updateById(device);
        }
    }

    @Override
    public void removeDevice(String id) {
        WecDevice device = this.getById(id);
        if (device != null) {
            // 1. Remove authorization in IoT module and clear cache
            if (device.getSn() != null) {
                iotWaterControlDeviceService.setAuthorization(device.getSn(), false);
                iotWaterControlDeviceService.removeCache(device.getSn());
            }
            // 2. Remove from DB
            this.removeById(id);
        }
    }

    @Override
    public void removeDevices(List<String> ids) {
        if (ids == null || ids.isEmpty()) return;
        List<WecDevice> devices = this.listByIds(ids);
        for (WecDevice device : devices) {
            if (device.getSn() != null) {
                iotWaterControlDeviceService.setAuthorization(device.getSn(), false);
                iotWaterControlDeviceService.removeCache(device.getSn());
            }
        }
        this.removeByIds(ids);
    }

    @Override
    public List<IotDeviceVO> searchPendingDevices(String keyword) {
        // Adapt IotWaterControlDeviceVO to IotDeviceVO for the interface
        List<IotWaterControlDeviceVO> list = iotWaterControlDeviceService.queryPendingWaterDevices(keyword);

        return list.stream().map(d -> {
            IotDeviceVO vo = new IotDeviceVO();
            vo.setId(d.getId());
            vo.setSn(d.getSn());
            vo.setDeviceName(d.getDeviceName());
            vo.setIpAddress(d.getIpAddress());
            vo.setDeviceType(d.getDeviceType());
//            vo.setStatus(d.getStatus());
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public WecDevice bindDevice(String iotDeviceId) {
        // 1. 获取 IoT 设备信息 (We might need getBySn or getById in IotWaterControlDeviceService too, 
        //    but for now assuming we pass SN or have it. Wait, iotDeviceId is ID or SN? 
        //    Usually SN is better for binding.)

        // NOTE: The current requirement is just to implement logic. 
        // We assume the caller (Controller) has the WecDevice populated with SN.

        // If this method is supposed to take an ID/SN and do everything:
        // We need to find the IotDevice first.

        // But let's look at how Controller uses this. Controller calls save(wecDevice).
        // We should probably add logic in save/add flow.

        return null;
    }

    @Override
    public boolean save(WecDevice entity) {
        return save(entity, false);
    }

    @Autowired
    private RedisUtil redisUtil;
    
    private static final String REDIS_KEY_PREFIX_HEARTBEAT = "iot:water:heartbeat:";

    @Override
    public boolean updateById(WecDevice entity) {
        // Pre-fetch original data to check if SN changed
        WecDevice originalDevice = this.getById(entity.getId());
        if (originalDevice == null) return false;
        String oldSn = originalDevice.getSn();
        
        // Check online status
        if (oldSn != null) {
            Object val = redisUtil.get(REDIS_KEY_PREFIX_HEARTBEAT + oldSn);
            boolean isOnline = false;
            if (val != null) {
                try {
                    long lastHeartbeat = Long.parseLong(val.toString());
                    // 70s threshold
                    if (System.currentTimeMillis() - lastHeartbeat <= 70000) {
                        isOnline = true;
                    }
                } catch (Exception e) {
                    // ignore
                }
            }
            if (!isOnline) {
                throw new RuntimeException("设备离线，无法编辑");
            }
        }

        boolean result = super.updateById(entity);
        if (result) {
            // Logic to re-send device name command on edit
            // Ensure we have SN and DeviceName. If not in entity, fetch from DB.
            String sn = entity.getSn();
            String deviceName = entity.getDeviceName();
            
            if (sn == null || deviceName == null) {
                WecDevice fullEntity = this.getById(entity.getId());
                if (fullEntity != null) {
                    if (sn == null) sn = fullEntity.getSn();
                    if (deviceName == null) deviceName = fullEntity.getDeviceName();
                }
            }

            // Check if SN has changed and send command
            if (sn != null && !sn.equals(oldSn)) {
                iotWaterControlDeviceService.updateDeviceSn(oldSn, sn);
            }

            if (sn != null && deviceName != null) {
                final String finalSn = sn;
                final String finalDeviceName = deviceName;
                
                // Send immediately
                iotWaterControlDeviceService.updateDeviceName(finalSn, finalDeviceName);
            }

            // 根据绑定的费率模式id：rateTemplateId，查询费率详细信息，根据设备协议文档进行下发
            if (sn != null) {
                String rateTemplateId = entity.getRateTemplateId();
                if (rateTemplateId == null) {
                    WecDevice fullEntity = this.getById(entity.getId());
                    if (fullEntity != null) {
                        rateTemplateId = fullEntity.getRateTemplateId();
                    }
                }

                if (rateTemplateId != null) {
                    WecRateTemplate template = wecRateTemplateService.getById(rateTemplateId);
                    if (template != null) {
                        WaterRateConfigVO config = new WaterRateConfigVO();
                        // Map Work Mode
                        String wm = template.getWorkMode();
                        if ("real_time".equals(wm)) {
                            config.setWorkMode(0);
                        } else if ("pre_deduct".equals(wm)) {
                            config.setWorkMode(1);
                        } else if ("per_count".equals(wm)) {
                            config.setWorkMode(2);
                        } else if (wm != null) {
                            try { config.setWorkMode(Integer.parseInt(wm)); } catch (Exception e) {}
                        }

                        // Map Deduction Method
                        String dm = template.getDeductionMethod();
                        if ("timed".equals(dm)) {
                            config.setDeductionMethod(0);
                        } else if ("pulse".equals(dm)) {
                            config.setDeductionMethod(1);
                        } else if (dm != null) {
                            try { config.setDeductionMethod(Integer.parseInt(dm)); } catch (Exception e) {}
                        }
                        config.setFreeSeconds(template.getFreeSeconds());
                        if (template.getRealTimeAmount() != null) {
                            config.setRealTimeAmount(template.getRealTimeAmount().multiply(new BigDecimal(100)).intValue());
                        }
                        config.setRealTimeDuration(template.getRealTimeDuration());
                        config.setPreDeductDuration(template.getPreDeductTime());
                        config.setPerTimeDuration(template.getPerTimeDuration());

                        iotWaterControlDeviceService.sendRateConfig(sn, config);
                    }
                }
            }
            
        }
        return result;
    }

    @Override
    public boolean save(WecDevice entity, boolean resetData) {
        // Check for duplicate SN
        if (entity.getSn() != null) {
            long count = this.count(new QueryWrapper<WecDevice>().eq("sn", entity.getSn()));
            if (count > 0) {
                throw new RuntimeException("设备SN已存在: " + entity.getSn());
            }
        }
        
        // Override save to include binding logic
        boolean result = super.save(entity);
        if (result && entity.getSn() != null) {
            // Authorize the device in IoT module
            iotWaterControlDeviceService.setAuthorization(entity.getSn(), true);
            
            // Update Device Name
            if (entity.getDeviceName() != null) {
                iotWaterControlDeviceService.updateDeviceName(entity.getSn(), entity.getDeviceName());
            }
        }
        return result;
    }
}
