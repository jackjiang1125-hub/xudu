// CardInfo.java
package org.jeecg.modules.hkclients.model.accesscontrol;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

// org.jeecg.modules.hkclients.model.accesscontrol.CardInfo
@Data
@JsonRootName("CardInfo")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardInfo {
    private String employeeNo;
    private String cardNo;                  // ★ 用 String，保留前导0
    private String cardType = "normalCard"; // "normalCard" 等
    private Boolean belongPassTime = true;  // ★ 受用户时间模板约束

    private CardValid cardValid = defaultCardValid(); // ★ 建议带上

    @Data
    public static class CardValid {
        private Boolean enable = true;
        private String beginTime;           // "2025-01-01T00:00:00"
        private String endTime;             // "2035-12-31T23:59:59"
    }

    private static CardValid defaultCardValid() {
        CardValid v = new CardValid();
        v.setBeginTime("2025-01-01T00:00:00");
        v.setEndTime("2035-12-31T23:59:59");
        return v;
    }


}

