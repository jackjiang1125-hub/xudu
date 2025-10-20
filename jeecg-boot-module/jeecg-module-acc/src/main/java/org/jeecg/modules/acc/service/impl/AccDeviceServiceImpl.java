package org.jeecg.modules.acc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.acc.entity.AccDevice;
import org.jeecg.modules.acc.mapper.AccDeviceMapper;
import org.jeecg.modules.acc.mapstruct.AccDeviceMapstruct;
import org.jeecg.modules.acc.mapstruct.AccDeviceQueryMapstruct;
import org.jeecg.modules.acc.mapstruct.RegisterAccDeviceEventMapstruct;
import org.jeecg.modules.acc.service.IAccDeviceTempService;
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
                exist.setIsReboot(reset);
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
        save(accDevice);
        log.info("授权设备成功,添加只门禁模块");

        // 添加门列表
        // 添加读头列表
        // 判断是否需要下发重置设备数据命令
        // 给设备下发软件时间、时区等
    }
}
