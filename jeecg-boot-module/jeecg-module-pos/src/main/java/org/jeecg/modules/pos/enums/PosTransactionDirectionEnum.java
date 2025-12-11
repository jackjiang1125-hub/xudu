package org.jeecg.modules.pos.enums;

import java.util.Arrays;

/**
 * 收支方向枚举
 */
public enum PosTransactionDirectionEnum {

    INCOME("income"),
    EXPENSE("expense");

    private final String code;

    PosTransactionDirectionEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static PosTransactionDirectionEnum fromCode(String code) {
        return Arrays.stream(values())
            .filter(item -> item.code.equalsIgnoreCase(code))
            .findFirst()
            .orElse(null);
    }
}
