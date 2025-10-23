package com.xudu.center.video.camera.service;

import com.xudu.center.video.camera.vo.CameraVO;
import com.xudu.center.video.camera.vo.PlayUrlVO;

public interface IZlmService {


    PlayUrlVO addStreamProxy(CameraVO req);

    void removeStreamProxy(String streamId);
}
