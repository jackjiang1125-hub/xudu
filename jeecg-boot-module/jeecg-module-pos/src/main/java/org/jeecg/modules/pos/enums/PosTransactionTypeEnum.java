package org.jeecg.modules.pos.enums;

import java.util.Arrays;

/**
 * 账户交易类型
 */
public enum PosTransactionTypeEnum {

    RECHARGE("recharge"),
    CONSUMPTION("consumption"),
    REFUND("refund"),
    SUBSIDY("subsidy"),
    ADJUSTMENT("adjustment"),
    FREEZE("freeze"),
    UNFREEZE("unfreeze");

    private final String code;

    PosTransactionTypeEnum(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public static PosTransactionTypeEnum fromCode(String code) {
        return Arrays.stream(values())
            .filter(item -> item.code.equalsIgnoreCase(code))
            .findFirst()
            .orElse(null);
    }
}
