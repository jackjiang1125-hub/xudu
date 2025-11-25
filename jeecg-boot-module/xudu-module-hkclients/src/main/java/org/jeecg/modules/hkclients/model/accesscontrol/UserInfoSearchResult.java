package org.jeecg.modules.hkclients.model.accesscontrol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;
import org.jeecg.modules.hkclients.model.accesscontrol.UserInfo;
import org.jeecg.modules.hkclients.model.accesscontrol.CardInfo;

import java.util.List;

@Data
@JsonRootName("UserInfoSearch")
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserInfoSearchResult {

    @JsonProperty("searchID")
    private String searchID;

    @JsonProperty("responseStatusStrg")
    private String responseStatusStrg;

    @JsonProperty("numOfMatches")
    private Integer numOfMatches;

    @JsonProperty("totalMatches")
    private Integer totalMatches;

    @JsonProperty("UserInfo")
    private List<UserInfo> userInfoList;
}


