// UserRightPlanTemplate.java
package org.jeecg.modules.hkclients.model.accesscontrol;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

@Data
@JsonRootName("UserRightPlanTemplate")
@JsonIgnoreProperties(ignoreUnknown = true)
// org.jeecg.modules.hkclients.model.accesscontrol.UserRightPlanTemplate
public class UserRightPlanTemplate {
    private Integer templateNo;
    private Boolean enable = true;
    private String templateName;
    private Integer weekPlanNo;
    private String holidayGroupNo = "1";  // ★★ 常见必填：字符串 "1"
}

