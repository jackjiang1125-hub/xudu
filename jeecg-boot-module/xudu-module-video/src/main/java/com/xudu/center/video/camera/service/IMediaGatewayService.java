// IZ
package com.xudu.center.video.camera.service;

import org.jeecg.common.system.base.service.JeecgService;
import com.xudu.center.video.camera.entity.MediaGateway;

public interface IMediaGatewayService extends JeecgService<MediaGateway> {
    void saveOrUpdateByMediaServerId(MediaGateway gw);
}
