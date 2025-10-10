package org.jeecg.modules.iot.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.iot.device.entity.IotDeviceCommand;

/**
 * Mapper for queued access-control device commands.
 */
@Mapper
public interface IotDeviceCommandMapper extends BaseMapper<IotDeviceCommand> {

    /**
     * Get max(command_code) for a device. command_code is stored as STRING
     * like "2001", so we cast to UNSIGNED for proper numeric MAX.
     * Adjust SQL for non-MySQL databases if needed.
     */
    @Select(
        "SELECT max(command_code) " +
        "FROM iot_device_command WHERE sn = #{sn}"
    )
    Long selectMaxCommandCodeBySn(@org.apache.ibatis.annotations.Param("sn") String sn);

}
