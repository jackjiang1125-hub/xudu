package org.jeecgframework.boot.common.vo;

import lombok.Data;

import java.util.List;

// common
@Data
public class PageResult<T> {
    private long total, pageNo, pageSize;
    private List<T> records;
    public static <T> PageResult<T> of(long total,long pageNo,long pageSize,List<T> records){
        PageResult<T> pageResult = new PageResult<T>();
        pageResult.setTotal(total);
        pageResult.setPageNo(pageNo);
        pageResult.setPageSize(pageSize);
        pageResult.setRecords(records);
        return pageResult;
    }
}
