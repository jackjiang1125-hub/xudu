package org.jeecg.modules.pos.enums;

import java.util.Arrays;

/**
 * POS账户状态
 */
public enum PosAccountStatusEnum {

    ACTIVE("active"),
    SUSPENDED("suspended"),
    CANCELLED("cancelled");

    private final String code;

    PosAccountStatusEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static PosAccountStatusEnum fromCode(String code) {
        return Arrays.stream(values())
            .filter(item -> item.code.equalsIgnoreCase(code))
            .findFirst()
            .orElse(null);
    }
}
