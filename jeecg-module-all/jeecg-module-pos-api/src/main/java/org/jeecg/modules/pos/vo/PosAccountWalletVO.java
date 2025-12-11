package org.jeecg.modules.pos.vo;


import lombok.Data;

import java.math.BigDecimal;

/**
 * 账户钱包摘要
 */
@Data
public class PosAccountWalletVO {
    private BigDecimal cashWallet;
    private BigDecimal subsidyWallet;
    private BigDecimal giftWallet;
    private BigDecimal frozenAmount;
    private BigDecimal creditLimit;
    private BigDecimal arrearsAmount;
    private BigDecimal totalBalance;
}
