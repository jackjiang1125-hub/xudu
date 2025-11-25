// UserInfo.java
package org.jeecg.modules.hkclients.model.accesscontrol;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import java.util.List;

// org.jeecg.modules.hkclients.model.accesscontrol.UserInfo
@Data
@JsonRootName("UserInfo")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfo {
    private String employeeNo;
    private String name;
    private String userType = "normal";  // ★ 必带
    private String doorRight = "1";      // ★ 一体机通常为 "1"
    private List<RightPlan> RightPlan;
    private Valid Valid;

    @Data
    public static class RightPlan {
        private Integer doorNo = 1;
        private String  planTemplateNo;    // ★ 改成 String
    }

    @Data
    public static class Valid {
        private Boolean enable = true;
        private String beginTime;
        private String endTime;
        private String timeType = "local"; // ★ 建议带上
    }
}

