package org.jeecg.modules.acc.constants;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 门禁反潜（Anti-passback）模式常量与描述映射。
 *
 * 约定：常量值与设备协议中的枚举值一致；提供统一的描述查询方法。
 */
public final class AccAntiPassbackConstants {

    private AccAntiPassbackConstants() {}

    // 数值常量（根据提供的映射）
    public static final int NONE = 0;                  // 无反潜
    public static final int DOOR_1_2 = 1;              // 门1与2反潜
    public static final int DOOR_3_4 = 2;              // 门3与4反潜
    public static final int DOOR_1_2_AND_3_4 = 3;      // 门1与2反潜，3与4反潜
    public static final int DOOR_12_OR_34 = 4;         // 门1或2与3或4反潜
    public static final int DOOR_1_2_OR_3 = 5;         // 门1与2或3反潜
    public static final int DOOR_1_2_OR_3_OR_4 = 6;    // 门1与2或3或4反潜

    public static final int READER_1 = 16;             // 读头1间反潜
    public static final int READER_2 = 32;             // 读头2间反潜
    public static final int READER_1_2 = 48;           // 读头1，2间各自同时反潜
    public static final int READER_3 = 64;             // 读头3间反潜
    public static final int READER_1_3 = 80;           // 读头1，3间各自同时反潜
    public static final int READER_2_3 = 96;           // 读头2，3间各自同时反潜
    public static final int READER_1_2_3 = 112;        // 读头1，2，3间各自同时反潜
    public static final int READER_4 = 128;            // 读头4间反潜
    public static final int READER_1_4 = 144;          // 读头1，4间各自同时反潜
    public static final int READER_2_4 = 160;          // 读头2，4间各自同时反潜
    public static final int READER_1_2_4 = 176;        // 读头1，2，4间各自同时反潜
    public static final int READER_3_4 = 196;          // 读头3，4间各自同时反潜
    public static final int READER_1_3_4 = 208;        // 读头1，3，4间各自同时反潜
    public static final int READER_2_3_4 = 224;        // 读头2，3，4间各自同时反潜
    public static final int READER_1_2_3_4 = 240;      // 读头1，2，3，4间各自同时反潜

    // 有序不可变描述映射
    private static final Map<Integer, String> DESCRIPTIONS;
    static {
        Map<Integer, String> m = new LinkedHashMap<>();
        m.put(NONE, "无反潜");
        m.put(DOOR_1_2, "门1与2反潜");
        m.put(DOOR_3_4, "门3与4反潜");
        m.put(DOOR_1_2_AND_3_4, "门1与2反潜，3与4反潜");
        m.put(DOOR_12_OR_34, "门1或2与3或4反潜");
        m.put(DOOR_1_2_OR_3, "门1与2或3反潜");
        m.put(DOOR_1_2_OR_3_OR_4, "门1与2或3或4反潜");
        m.put(READER_1, "读头1间反潜");
        m.put(READER_2, "读头2间反潜");
        m.put(READER_1_2, "读头1，2间各自同时反潜");
        m.put(READER_3, "读头3间反潜");
        m.put(READER_1_3, "读头1，3间各自同时反潜");
        m.put(READER_2_3, "读头2，3间各自同时反潜");
        m.put(READER_1_2_3, "读头1，2，3间各自同时反潜");
        m.put(READER_4, "读头4间反潜");
        m.put(READER_1_4, "读头1，4间各自同时反潜");
        m.put(READER_2_4, "读头2，4间各自同时反潜");
        m.put(READER_1_2_4, "读头1，2，4间各自同时反潜");
        m.put(READER_3_4, "读头3，4间各自同时反潜");
        m.put(READER_1_3_4, "读头1，3，4间各自同时反潜");
        m.put(READER_2_3_4, "读头2，3，4间各自同时反潜");
        m.put(READER_1_2_3_4, "读头1，2，3，4间各自同时反潜");
        DESCRIPTIONS = Collections.unmodifiableMap(m);
    }

    /**
     * 获取反潜模式的中文描述。
     * @param code 反潜模式代码
     * @return 描述文本，未知返回"未知反潜模式"
     */
    public static String describe(int code) {
        return DESCRIPTIONS.getOrDefault(code, "未知反潜模式");
    }

    /**
     * 返回所有反潜模式的只读映射（按插入顺序）。
     */
    public static Map<Integer, String> all() {
        return DESCRIPTIONS;
    }
}