package org.jeecg.modules.events.acc;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.jeecg.modules.events.acc.vo.AccUserEventVO;

/**
 * 系统用户更新事件（用于解耦 system -> acc 的直接依赖）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccUserUpdatedEvent {

    /**
     * 精简的用户信息载荷（事件本地VO，不依赖 acc-api）
     */
    private AccUserEventVO user;
}