// FaceInfo.java
package org.jeecg.modules.hkclients.model.accesscontrol;
import lombok.Data;

@Data
public class FaceInfo {
    private String employeeNo;  // 必填：绑定人员
    // 二选一：base64 或 URL（不要同时给）
    private String picture;     // base64（不要带 "data:image/jpeg;base64," 前缀）
    private String faceURL;     // 设备可直连的图片 URL

    // 兼容字段（可不传，留着不影响）
    private String name;        // 便于核对
    private String faceLibType = "blackFD";
    private String FDID = "1";
    private String faceID;
}

