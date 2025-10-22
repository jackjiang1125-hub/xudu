package com.xudu.center.zlm.model;

import lombok.Data;

/** ZLM 统一响应：{code:int,msg:string,data:any} */
@Data
public class ZlmResponse<T> {
    private int code;
    private String msg;
    private T data;

    public boolean ok() { return code == 0; }
}
