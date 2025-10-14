package org.jeecg.modules.acc.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.jeecg.modules.acc.entity.AccGroupDevice;

/**
 * 权限组-设备关联Mapper
 */
@Mapper
public interface AccGroupDeviceMapper extends BaseMapper<AccGroupDevice> {
}