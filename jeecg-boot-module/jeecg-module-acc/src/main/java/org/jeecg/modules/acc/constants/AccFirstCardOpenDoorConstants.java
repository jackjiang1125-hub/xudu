package org.jeecg.modules.acc.constants;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 首卡开门（FirstCardOpenDoor）设置常量与描述映射。
 *
 * 约定：常量值与设备协议中的枚举值一致；提供统一的中文描述查询方法。
 */
public final class AccFirstCardOpenDoorConstants {

    private AccFirstCardOpenDoorConstants() {}

    // 数值常量（对应设备协议枚举）
    public static final int NONE = 0;            // 不启用
    public static final int ALWAYS_OPEN = 1;     // 首卡常开
    public static final int ACTIVATED = 2;       // 首卡激活

    // 有序不可变描述映射
    private static final Map<Integer, String> DESCRIPTIONS;
    static {
        Map<Integer, String> m = new LinkedHashMap<>();
        m.put(NONE, "不启用");
        m.put(ALWAYS_OPEN, "首卡常开");
        m.put(ACTIVATED, "首卡激活");
        DESCRIPTIONS = Collections.unmodifiableMap(m);
    }

    /**
     * 获取首卡开门设置的中文描述。
     * @param code 设置代码
     * @return 描述文本，未知返回"未知首卡开门设置"
     */
    public static String describe(int code) {
        return DESCRIPTIONS.getOrDefault(code, "未知首卡开门设置");
    }

    /**
     * 返回所有首卡开门设置的只读映射（按插入顺序）。
     */
    public static Map<Integer, String> all() {
        return DESCRIPTIONS;
    }
}