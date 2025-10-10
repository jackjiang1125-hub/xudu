package org.jeecgframework.boot.iot.api;

import org.jeecgframework.boot.common.api.CommonService;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.jeecgframework.boot.iot.query.IotDeviceQuery;
import org.jeecgframework.boot.iot.vo.IotDeviceVO;

import java.util.Map;

public interface IotDeviceService  {

    PageResult<IotDeviceVO> list(IotDeviceQuery iotDeviceQuery,PageRequest pageRequest, Map<String,String[]> queryParam);

}
