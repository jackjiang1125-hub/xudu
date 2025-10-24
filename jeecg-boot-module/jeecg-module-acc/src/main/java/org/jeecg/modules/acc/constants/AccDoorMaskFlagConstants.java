package org.jeecg.modules.acc.constants;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import dm.jdbc.b.p;

/**
 * 门锁定标志（DoorMaskFlag）常量与描述映射。
 *
 * 约定：常量值与设备协议中的枚举值一致；提供统一的中文描述查询方法。
 */
public final class AccDoorMaskFlagConstants {

    private AccDoorMaskFlagConstants() {}

    // 数值常量（对应设备协议枚举）
    public static final int NONE = 0;     // 无
    public static final int LOCKED = 1;     // 已锁定
    public static final int UNLOCKED = 2;   // 未锁定

    // 有序不可变描述映射
    private static final Map<Integer, String> DESCRIPTIONS;
    static {
        Map<Integer, String> m = new LinkedHashMap<>();
        m.put(NONE, "无");
        m.put(LOCKED, "已锁定");
        m.put(UNLOCKED, "未锁定");
        DESCRIPTIONS = Collections.unmodifiableMap(m);
    }

    /**
     * 获取门锁定标志的中文描述。
     * @param code 设置代码
     * @return 描述文本，未知返回"未知门锁定标志"
     */
    public static String describe(int code) {
        return DESCRIPTIONS.getOrDefault(code, "未知门锁定标志");
    }

    /**
     * 返回所有门锁定标志的只读映射（按插入顺序）。
     */
    public static Map<Integer, String> all() {
        return DESCRIPTIONS;
    }
}