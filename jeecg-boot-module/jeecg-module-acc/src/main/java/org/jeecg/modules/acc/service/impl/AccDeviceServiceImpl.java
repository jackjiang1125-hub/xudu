package org.jeecg.modules.acc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.acc.entity.AccDevice;
import org.jeecg.modules.acc.entity.AccDoor;
import org.jeecg.modules.acc.entity.AccReader;
import org.jeecg.modules.acc.entity.AccDeviceTemp;
import org.jeecg.modules.acc.mapper.AccDeviceMapper;
import org.jeecg.modules.acc.mapstruct.AccDeviceMapstruct;
import org.jeecg.modules.acc.mapstruct.AccDeviceQueryMapstruct;
import org.jeecg.modules.acc.mapstruct.RegisterAccDeviceEventMapstruct;
import org.jeecg.modules.acc.service.IAccDeviceTempService;
import org.jeecg.modules.acc.service.IAccDoorService;
import org.jeecg.modules.acc.service.IAccReaderService;
import org.jeecg.modules.events.acc.RegisterAccDeviceEvent;
import org.jeecgframework.boot.acc.api.AccDeviceService;
import org.jeecgframework.boot.acc.query.AccDeviceQuery;
import org.jeecgframework.boot.acc.vo.AccDeviceVO;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.jeecgframework.boot.iot.api.IotDeviceService;

import java.util.Map;

/**
 * 门禁设备服务实现类
 * @author system
 * @date 2025-01-03
 */
@Slf4j
@Service
public class AccDeviceServiceImpl extends JeecgServiceImpl<AccDeviceMapper, AccDevice> implements AccDeviceService {


    @Autowired
    private AccDeviceMapstruct accDeviceMapstruct;

    @Autowired
    private AccDeviceQueryMapstruct accDeviceQueryMapstruct;

    @Autowired
    private RegisterAccDeviceEventMapstruct registerAccDeviceEventMapstruct;

    @Autowired
    private IAccDeviceTempService accDeviceTempService;

    @Autowired
    private IotDeviceService iotDeviceService;

    @Autowired
    private IAccDoorService accDoorService;

    @Autowired
    private IAccReaderService accReaderService;


//    @Override
//    public PageResult<IotDeviceVO> list(AccDeviceQuery accDeviceQuery, PageRequest pageRequest, Map<String, String[]> queryParam) {
//        //一行代码搞定 DT0->Entity  查询之后 entity->VO
//
//    }


    @Override
    public PageResult<AccDeviceVO> list(AccDeviceQuery accDeviceQuery, PageRequest pageRequest, Map<String, String[]> queryParam) {
//        return pageByQuery(accDeviceQuery,
//                pageRequest.getPageNo(),
//                pageRequest.getPageSize(),
//                queryParam,
//                accDeviceQueryMapstruct::toIotDevice,
//                accDeviceMapstruct::toAccDeviceVO
//                //  ,qw -> qw.orderByDesc("create_time") //带排序
//        );

        return pageByQuery(accDeviceQuery,pageRequest.getPageNo(),pageRequest.getPageSize(),queryParam,
                accDeviceQueryMapstruct::toIotDevice,
                accDeviceMapstruct::toAccDeviceVO
                );

    }

    @Override
    public AccDeviceVO getById(String id) {
        if (StringUtils.isBlank(id)) {
            return null;
        }
        AccDevice entity = super.getById(id);
        return accDeviceMapstruct.toAccDeviceVO(entity);
    }

    @Override
    public AccDeviceVO getBySn(String sn) {
        if (StringUtils.isBlank(sn)) {
            return null;
        }
        LambdaQueryWrapper<AccDevice> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AccDevice::getSn, sn);
        return accDeviceMapstruct.toAccDeviceVO(this.getOne(queryWrapper));
    }

    @Override
    public AccDeviceVO save(AccDeviceVO deviceVO) {
        if (deviceVO == null) {
            return null;
        }
        AccDevice entity = accDeviceMapstruct.toAccDevice(deviceVO);
        // 默认设备类型
        if (StringUtils.isBlank(entity.getDeviceType())) {
            entity.setDeviceType("acc");
        }
        // 默认授权状态
        if (entity.getAuthorized() == null) {
            entity.setAuthorized(Boolean.FALSE);
        }
        this.save(entity);
        return accDeviceMapstruct.toAccDeviceVO(entity);
    }

    @Override
    public void saveTemp(org.jeecgframework.boot.acc.vo.AccDeviceVO deviceVO) {
        // 参数校验
        if (deviceVO == null || org.apache.commons.lang3.StringUtils.isBlank(deviceVO.getSn())) {
            log.warn("saveTemp: deviceVO is null or SN missing, skip save and authorize");
            return;
        }

        // 写入之前先判断这个sn的设备记录是否已经存在，如果存在走修改逻辑
        org.jeecg.modules.acc.entity.AccDeviceTemp exist = accDeviceTempService
                .lambdaQuery()
                .eq(org.jeecg.modules.acc.entity.AccDeviceTemp::getSn, deviceVO.getSn())
                .one();
        if (exist != null) {
            exist.setDeviceName(deviceVO.getDeviceName());
            Boolean reset = deviceVO.getIsReset();
            if (reset != null) {
                exist.setIsReset(reset);
            }
            try {
                org.jeecg.common.system.vo.LoginUser lu =
                        (org.jeecg.common.system.vo.LoginUser) org.apache.shiro.SecurityUtils.getSubject().getPrincipal();
                String op = lu != null ? lu.getUsername() : null;
                exist.setUpdateBy(op);
                exist.setUpdateTime(new java.util.Date());
            } catch (Exception ignored) { }
            accDeviceTempService.updateById(exist);
            log.info("[AccDeviceTemp] 已更新临时设备记录 SN={}", exist.getSn());
        } else {
            accDeviceTempService.saveFromVO(deviceVO);
        }

        // 获取当前操作人
        String operator = null;
        try {
            org.jeecg.common.system.vo.LoginUser loginUser =
                (org.jeecg.common.system.vo.LoginUser) org.apache.shiro.SecurityUtils.getSubject().getPrincipal();
            operator = loginUser != null ? loginUser.getUsername() : null;
        } catch (Exception ignored) { }

        // 调用 IoT 内部服务进行授权（传递 sn、registryCode、remark、operator）
        iotDeviceService.authorizeDevice(
            deviceVO.getSn(),
            deviceVO.getRegistryCode(),
            deviceVO.getRemark(),
            operator
        );
    }

    @Override
    public AccDeviceVO update(AccDeviceVO deviceVO) {
        if (deviceVO == null || StringUtils.isBlank(deviceVO.getId())) {
            return null;
        }
        AccDevice entity = accDeviceMapstruct.toAccDevice(deviceVO);
        this.updateById(entity);
        AccDevice latest = super.getById(entity.getId());
        return accDeviceMapstruct.toAccDeviceVO(latest);
    }

    @Override
    public boolean deleteById(String id) {
        if (StringUtils.isBlank(id)) {
            return false;
        }
        return this.removeById(id);
    }

    @Override
    public boolean deleteBatch(String[] ids) {
        if (ids == null || ids.length == 0) {
            return false;
        }
        java.util.List<String> idList = java.util.Arrays.stream(ids)
                .filter(org.apache.commons.lang3.StringUtils::isNotBlank)
                .collect(java.util.stream.Collectors.toList());
        if (idList.isEmpty()) {
            return false;
        }
        return this.removeByIds(idList);
    }


    @Transactional(rollbackFor = Exception.class)
    @EventListener
    public void authorizeDevice(RegisterAccDeviceEvent registerAccDeviceEvent) {

        if(getBySn(registerAccDeviceEvent.getSn())!=null){
            log.info("设备已存在");
            return;
        }
        AccDevice accDevice = registerAccDeviceEventMapstruct.toAccDevice(registerAccDeviceEvent);

        // 查询临时表
        AccDeviceTemp accDeviceTemp = accDeviceTempService.lambdaQuery()
                .eq(AccDeviceTemp::getSn, registerAccDeviceEvent.getSn())
                .one();
        if (accDeviceTemp == null) {
            log.warn("未找到临时设备记录 SN={}", registerAccDeviceEvent.getSn());
            return;
        }

        accDevice.setDeviceName(accDeviceTemp.getDeviceName());
        save(accDevice);
        log.info("授权设备成功,添加只门禁模块");

        // 判断是否需要下发重置设备数据命令
        if (accDeviceTemp.getIsReset()) {
            
        }

        // 添加门列表，有几个门，往acc_door表添加几条记录，doorName命名是第一个门  设备名-1  第二个门  设备名-2  以此类推
        for (int i = 0; i < accDevice.getLockCount(); i++) {
            AccDoor accDoor = new AccDoor();
            accDoor.setDoorNumber(i + 1);
            accDoor.setDeviceSn(accDevice.getSn());
            accDoor.setDeviceName(accDevice.getDeviceName());
            accDoor.setDoorName(accDevice.getDeviceName() + "-" + (i + 1));

            // 设置默认参数
            accDoor.setOperationInterval(0);    //操作间隔
            accDoor.setVerificationMethod("自动识别");  //验证方式
            accDoor.setAntiBacktrackingDuration(0);    //入反潜时长

            accDoor.setCoercionPassword("");    //胁迫密码
            accDoor.setEmergencyPassword("");   //紧急状态密码
            accDoor.setHostAccessStatus("入");   //主机出入状态
            accDoor.setSlaveAccessStatus("出");   //从机出入状态
            accDoor.setLockDriveDuration(5);    //锁开时长5秒
            accDoor.setDoorContactDelay(30);    //门磁延时30秒
            accDoor.setMultiPersonOpenInterval(10);  //多人开门间隔10秒

            // TODO 按理说这里应该默认赋值24小时通行
            accDoor.setDoorValidTimeRange("");   //门有效时间段
            accDoor.setDoorAlwaysOpenTime("");   //门常开时间段

            accDoorService.save(accDoor);

            // 一个门两个读头, 一个入一个出，读头名称  设备名是门名称-入  门名称-出
            for (int j = 0; j < 2; j++) {
                AccReader accReader = new AccReader();

                accReader.setDoorName(accDoor.getDoorName());
                accReader.setType(j == 0 ? "入" : "出");
                accReader.setName(accDoor.getDoorName() + "-" + (j == 0 ? "入" : "出"));
                // 如果是第一个门是 1 2，第二个门是 3 4，以此类推
                accReader.setNum(String.valueOf(j == 0 ? (i * 2 + 1) : (i * 2 + 2)));
                accReaderService.save(accReader);
            }
        }
        
        // 给设备下发软件时间、时区等
    }
}
