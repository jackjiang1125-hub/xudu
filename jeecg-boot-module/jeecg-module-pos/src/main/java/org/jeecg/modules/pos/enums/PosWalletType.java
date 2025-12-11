package org.jeecg.modules.pos.enums;

import java.util.Arrays;

/**
 * 钱包类型枚举
 */
public enum PosWalletType {

    CASH("cash"),
    SUBSIDY("subsidy"),
    GIFT("gift");

    private final String code;

    PosWalletType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static PosWalletType fromCode(String code) {
        return Arrays.stream(values())
            .filter(item -> item.code.equalsIgnoreCase(code))
            .findFirst()
            .orElse(null);
    }
}
