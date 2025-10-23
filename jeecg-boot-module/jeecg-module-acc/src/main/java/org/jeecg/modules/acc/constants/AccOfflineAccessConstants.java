package org.jeecg.modules.acc.constants;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 离线通行（OfflineRefuse）设置常量与描述映射。
 *
 * 约定：常量值与设备协议中的枚举值一致；提供统一的中文描述查询方法。
 */
public final class AccOfflineAccessConstants {

    private AccOfflineAccessConstants() {}

    // 数值常量（对应设备协议枚举）
    public static final int NORMAL = 0;   // 正常通行
    public static final int REFUSE = 1;   // 拒绝通行

    // 有序不可变描述映射
    private static final Map<Integer, String> DESCRIPTIONS;
    static {
        Map<Integer, String> m = new LinkedHashMap<>();
        m.put(NORMAL, "正常通行");
        m.put(REFUSE, "拒绝通行");
        DESCRIPTIONS = Collections.unmodifiableMap(m);
    }

    /**
     * 获取离线通行设置的中文描述。
     * @param code 设置代码
     * @return 描述文本，未知返回"未知离线通行设置"
     */
    public static String describe(int code) {
        return DESCRIPTIONS.getOrDefault(code, "未知离线通行设置");
    }

    /**
     * 返回所有离线通行设置的只读映射（按插入顺序）。
     */
    public static Map<Integer, String> all() {
        return DESCRIPTIONS;
    }
}