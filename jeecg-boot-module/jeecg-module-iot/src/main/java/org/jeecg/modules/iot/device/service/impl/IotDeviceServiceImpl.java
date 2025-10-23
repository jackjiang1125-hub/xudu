package org.jeecg.modules.iot.device.service.impl;


import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.lang3.StringUtils;

import org.jeecg.modules.iot.device.entity.IotDevice;

import org.jeecg.modules.iot.device.mapper.IotDeviceMapper;
import org.jeecg.modules.iot.device.service.ControlDeviceCommandDispatcher;
import org.jeecg.modules.iot.device.mapstruct.IotDeviceMapstruct;
import org.jeecg.modules.iot.device.mapstruct.IotDeviceQueryMapstruct;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.jeecgframework.boot.iot.api.IotDeviceService;
import org.jeecgframework.boot.iot.query.IotDeviceQuery;
import org.jeecgframework.boot.iot.vo.IotDeviceVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
// import org.apache.shiro.SecurityUtils;
// import org.jeecg.common.system.vo.LoginUser;
// import org.jeecg.modules.iot.device.entity.IotDeviceCommand;
// import org.jeecg.modules.iot.device.service.IotDeviceCommandService;

@Service
public class IotDeviceServiceImpl extends JeecgServiceImpl<IotDeviceMapper, IotDevice> implements IotDeviceService {

    @Autowired
    private IotDeviceQueryMapstruct iotDeviceQueryMapstruct;

    @Autowired
    private IotDeviceMapstruct iotDeviceMapstruct;

    @Autowired
    private IotDeviceInnerServiceImpl iotDeviceInnerServiceImpl;

    @Autowired
    private ControlDeviceCommandDispatcher controlDeviceCommandDispatcher;

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
    public void syncTime(String deviceSn, Long timestamp) {
        // 调用内部服务实现时间同步
        if (StringUtils.isBlank(deviceSn)) {
            return;
        }
        // LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // String operator = loginUser != null ? loginUser.getUsername() : null;
        controlDeviceCommandDispatcher.syncTime(deviceSn, timestamp, "");
    }

    @Override
    public void syncTimezone(String deviceSn, String timezone) {
        // 调用内部服务实现时区同步
        if (StringUtils.isBlank(deviceSn)) {
            return;
        }
        // LoginUser loginUser = (LoginUser) SecurityUtils.getSubject().getPrincipal();
        // String operator = loginUser != null ? loginUser.getUsername() : null;
        controlDeviceCommandDispatcher.syncTimezone(deviceSn, timezone, "");
    }
}
