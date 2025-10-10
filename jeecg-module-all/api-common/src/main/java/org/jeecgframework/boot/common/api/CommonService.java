package org.jeecgframework.boot.common.api;

import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;

import java.util.Map;

public interface CommonService<Q,R> {
    PageResult<R> list(Q query, PageRequest pageRequest, Map<String,String[]> queryParam);
}
