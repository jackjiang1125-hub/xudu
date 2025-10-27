package org.jeecg.modules.acc.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import org.jeecg.modules.acc.entity.AccDoor;
import org.jeecg.modules.acc.mapper.AccDoorMapper;
import org.jeecg.modules.acc.mapstruct.AccDoorMapstruct;
import org.jeecg.modules.acc.service.IAccDoorService;
import org.jeecg.modules.acc.vo.AccDoorVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 门列表 ServiceImpl
 */
@Service
public class AccDoorServiceImpl extends JeecgServiceImpl<AccDoorMapper, AccDoor> implements IAccDoorService {

    @Autowired
    private AccDoorMapstruct accDoorMapstruct;

    @Override
    public IPage<AccDoor> pageDoors(String deviceName, String doorName, String ipAddress, Integer pageNo, Integer pageSize) {
        LambdaQueryWrapper<AccDoor> qw = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(deviceName)) {
            qw.like(AccDoor::getDeviceName, deviceName);
        }
        if (StringUtils.isNotBlank(doorName)) {
            qw.like(AccDoor::getDoorName, doorName);
        }
        if (StringUtils.isNotBlank(ipAddress)) {
            qw.eq(AccDoor::getIpAddress, ipAddress);
        }
        Page<AccDoor> page = new Page<>(pageNo == null ? 1 : pageNo, pageSize == null ? 10 : pageSize);
        return this.page(page, qw);
    }

    @Override
    public AccDoor saveFromVO(AccDoorVO vo) {
        AccDoor entity = accDoorMapstruct.toEntity(vo);
        if (entity.getId() != null) {
            this.updateById(entity);
        } else {
            this.save(entity);
        }
        return entity;
    }

    @Override
    public void removeByDeviceSn(String deviceSn) {
        if (deviceSn == null) {
            return;
        }
        // 使用 Lambda 与列名两种方式删除，提升兼容性
        this.remove(new LambdaQueryWrapper<AccDoor>().eq(AccDoor::getDeviceSn, deviceSn));
    }
}