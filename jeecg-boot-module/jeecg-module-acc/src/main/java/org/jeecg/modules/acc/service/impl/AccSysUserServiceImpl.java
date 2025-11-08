package org.jeecg.modules.acc.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.jeecgframework.boot.acc.api.AccSysUserService;
import org.jeecgframework.boot.acc.vo.AccUserLiteVO;
import org.jeecg.modules.acc.service.iot.AccIoTDispatchService;
import org.jeecg.modules.events.acc.AccUserUpdatedEvent;
import org.jeecg.modules.events.acc.vo.AccUserEventVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

/**
 * 系统用户服务实现类
 * @author system
 * @date 2025-01-03
 */
@Slf4j
@Service
public class AccSysUserServiceImpl implements AccSysUserService {

    @Autowired
    private AccIoTDispatchService accIoTDispatchService;

    @Override
    public void updateSysUser(AccUserLiteVO userLiteVO) {
        log.info("ACC接收更新系统用户: {}", userLiteVO);
        if (userLiteVO == null) {
            log.warn("[AccSysUser] updateSysUser 参数为空，跳过处理");
            return;
        }
        try {
            // 调用统一调度服务：根据权限组聚合设备并下发用户更新
            accIoTDispatchService.updateUserInfoOnAuthorizedDevices(userLiteVO);
            log.info("[AccSysUser] 已触发用户更新下发 userId={}", userLiteVO.getId());
        } catch (Exception e) {
            log.warn("[AccSysUser] 用户更新下发失败 userId={}, err={}", userLiteVO.getId(), e.getMessage());
        }
    }

    /**
     * 监听系统用户更新事件，转调用现有更新逻辑
     */
    @EventListener
    public void onSysUserUpdated(AccUserUpdatedEvent event) {
        AccUserEventVO payload = event.getUser();
        if (payload == null) {
            log.warn("[AccSysUser] 事件载荷为空，跳过处理");
            return;
        }
        AccUserLiteVO userLiteVO = new AccUserLiteVO();
        BeanUtils.copyProperties(payload, userLiteVO);
        updateSysUser(userLiteVO);
    }

}
