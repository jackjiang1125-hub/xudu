package org.jeecgframework.boot.common.vo;

import lombok.Data;

@Data
public class PageRequest {
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
