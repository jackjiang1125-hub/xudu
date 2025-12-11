package org.jeecg.modules.pos.vo;



import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import java.util.List;

/**
 * POS账户详情VO
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class PosAccountDetailVO extends PosAccountListVO {
    private String idCardNo;
    private String badgeNo;
    private PosAccountWalletVO wallet;
    private PosAccountLimitVO limits;
    private List<String> bindDevices;
    private List<String> associatedCards;
    private Boolean lossReported;

    private Date lastLossReportTime;


    private Date lastPasswordResetTime;

    private Date lastCardReissueTime;
}
