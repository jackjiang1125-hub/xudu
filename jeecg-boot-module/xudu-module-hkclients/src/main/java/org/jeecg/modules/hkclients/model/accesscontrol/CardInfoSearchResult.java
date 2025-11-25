package org.jeecg.modules.hkclients.model.accesscontrol;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

import java.util.List;

@Data
@JsonRootName("CardInfoSearch")
@JsonIgnoreProperties(ignoreUnknown = true)
public class CardInfoSearchResult {

    @JsonProperty("searchID")
    private String searchID;

    @JsonProperty("responseStatusStrg")
    private String responseStatusStrg;

    @JsonProperty("numOfMatches")
    private Integer numOfMatches;

    @JsonProperty("totalMatches")
    private Integer totalMatches;

    @JsonProperty("CardInfo")
    private List<CardInfo> cardInfoList;
}