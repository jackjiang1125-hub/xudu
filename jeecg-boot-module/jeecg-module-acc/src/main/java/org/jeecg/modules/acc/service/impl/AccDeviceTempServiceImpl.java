package org.jeecg.modules.acc.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.modules.acc.entity.AccDeviceTemp;
import org.jeecg.modules.acc.mapper.AccDeviceTempMapper;
import org.jeecg.modules.acc.service.IAccDeviceTempService;
import org.jeecgframework.boot.acc.vo.AccDeviceVO;
import org.springframework.stereotype.Service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 门禁设备临时表 Service 实现
 */
@Slf4j
@Service
public class AccDeviceTempServiceImpl extends ServiceImpl<AccDeviceTempMapper, AccDeviceTemp> implements IAccDeviceTempService {

    @Override
    public void saveFromVO(AccDeviceVO deviceVO) {
        if (deviceVO == null || StringUtils.isBlank(deviceVO.getSn())) {
            log.warn("[AccDeviceTemp] 保存失败：参数为空或SN缺失");
            return;
        }
        AccDeviceTemp temp = new AccDeviceTemp();
        temp.setSn(deviceVO.getSn());
        temp.setDeviceName(deviceVO.getDeviceName());
        // 将 VO 中的 isReset 视为 isReboot（是否重启）
        Boolean reset = deviceVO.getIsReset();
        temp.setIsReset(reset != null ? reset : Boolean.FALSE);
        this.save(temp);
        log.info("[AccDeviceTemp] 已保存临时设备记录 SN={}, name={}, reboot={} ", temp.getSn(), temp.getDeviceName(), temp.getIsReset());
    }
}