package org.jeecg.modules.iot.device.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.jeecg.modules.iot.device.entity.IotDevicePhoto;
import org.jeecg.modules.iot.device.vo.AccDevicePhotoVO;

@Mapper
public interface IotDevicePhotoMapper extends BaseMapper<IotDevicePhoto> {
    
    /**
     * 根据设备SN和时间戳查询照片完整信息
     * @param sn 设备序列号
     * @param timeStamp 时间戳 (格式: 20251003192132)
     * @return 照片完整信息
     */
    @Select("SELECT photo_path as photoPath, pin as photoName, file_size as fileSize, uploaded_time as uploadedTime " +
            "FROM iot_device_photo WHERE sn = #{sn} AND pin LIKE CONCAT(#{timeStamp}, '-%') LIMIT 1")
    AccDevicePhotoVO findPhotoInfoBySnAndTimeStamp(@Param("sn") String sn, @Param("timeStamp") String timeStamp);
}
