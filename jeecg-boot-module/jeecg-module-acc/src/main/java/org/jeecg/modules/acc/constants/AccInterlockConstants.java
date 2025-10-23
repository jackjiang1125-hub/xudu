package org.jeecg.modules.acc.constants;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 附录9：互锁值常量与描述映射。
 *
 * 定义设备互锁模式的数值常量，并提供统一的中文描述查询方法。
 */
public final class AccInterlockConstants {

    private AccInterlockConstants() {}

    // 数值常量（对应设备协议枚举）
    public static final int NONE = 0;               // 无
    public static final int DOOR_1_2 = 1;           // 1与2号门互锁
    public static final int DOOR_3_4 = 2;           // 3与4号门互锁
    public static final int DOOR_1_2_3 = 3;         // 1与2与3号门互锁
    public static final int DOOR_12_OR_34 = 4;      // 1与2号门间互锁，或 3与4号门间互锁
    public static final int DOOR_1_2_3_4 = 5;       // 1与2与3与4门互锁

    // 有序不可变描述映射
    private static final Map<Integer, String> DESCRIPTIONS;
    static {
        Map<Integer, String> m = new LinkedHashMap<>();
        m.put(NONE, "无");
        m.put(DOOR_1_2, "1与2号门互锁");
        m.put(DOOR_3_4, "3与4号门互锁");
        m.put(DOOR_1_2_3, "1与2与3号门互锁");
        m.put(DOOR_12_OR_34, "1与2号门间互锁，或 3与4号门间互锁");
        m.put(DOOR_1_2_3_4, "1与2与3与4门互锁");
        DESCRIPTIONS = Collections.unmodifiableMap(m);
    }

    /**
     * 获取互锁模式的中文描述。
     * @param code 互锁模式代码
     * @return 描述文本，未知返回"未知互锁模式"
     */
    public static String describe(int code) {
        return DESCRIPTIONS.getOrDefault(code, "未知互锁模式");
    }

    /**
     * 返回所有互锁模式的只读映射（按插入顺序）。
     */
    public static Map<Integer, String> all() {
        return DESCRIPTIONS;
    }
}