// ZlmApiResp.java  (接收 ZLM /index/api 返回)
package com.xudu.center.video.camera.vo;
import lombok.Data;
import java.util.Map;

@Data
public class ZlmApiResp {
    private int code;          // 0 表示成功
    private String msg;
    private Map<String, Object> data;
}
