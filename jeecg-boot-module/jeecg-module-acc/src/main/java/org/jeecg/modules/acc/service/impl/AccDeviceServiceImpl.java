package org.jeecg.modules.acc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.acc.entity.AccDevice;
import org.jeecg.modules.acc.mapper.AccDeviceMapper;
import org.jeecg.modules.acc.mapstruct.AccDeviceMapstruct;
import org.jeecg.modules.acc.mapstruct.AccDeviceQueryMapstruct;
import org.jeecg.modules.acc.mapstruct.RegisterAccDeviceEventMapstruct;
import org.jeecg.modules.events.acc.RegisterAccDeviceEvent;
import org.jeecgframework.boot.acc.api.AccDeviceService;
import org.jeecgframework.boot.acc.query.AccDeviceQuery;
import org.jeecgframework.boot.acc.vo.AccDeviceVO;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.jeecgframework.boot.iot.query.IotDeviceQuery;
import org.jeecgframework.boot.iot.vo.IotDeviceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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

//        if (StringUtils.isBlank(sn)) {
//            throw new IllegalArgumentException("设备SN不能为空");
//        }
//
//        AccDeviceVO device = this.getBySn(sn);
//        if (device == null) {
//            log.warn("未找到设备: {}", sn);
//            return null;
//        }
//        // 更新授权信息
//        device.setAuthorized(1);
//        device.setRegistryCode(registryCode);
//        device.setRemark(remark);
//        device.setLastRegistryTime(LocalDateTime.now());
    }
}
