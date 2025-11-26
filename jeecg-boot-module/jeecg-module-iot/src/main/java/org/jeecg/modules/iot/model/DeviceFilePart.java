package org.jeecg.modules.iot.model;

import lombok.Builder;
import lombok.Data;

/**
 * 表示一次设备上报中的一个文件字段（例如海康上报的 Picture）。
 */
@Data
@Builder
public class DeviceFilePart {

    /** 表单字段名，例如 "Picture" */
    private String name;

    /** 客户端上传时的原始文件名 */
    private String filename;

    /** Content-Type，例如 image/jpeg */
    private String contentType;

    /** 文件二进制内容 */
    private byte[] bytes;

    /** 文件大小（字节） */
    private long size;
}
