package org.jeecg.modules.acc.vo;

import lombok.Data;
import java.util.List;

/**
 * 批量绑定/解绑请求体
 */
@Data
public class BindIdsRequest {
    private String groupId;
    private List<String> ids;
}