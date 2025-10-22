package com.xudu.center.video.camera.controller;


import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.config.shiro.IgnoreAuth;
import com.xudu.center.video.camera.entity.MediaGateway;
import com.xudu.center.video.camera.service.CameraService;
import com.xudu.center.video.camera.service.IMediaGatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/zlm")
@Slf4j
public class ZLMController {

    @Autowired
    private IMediaGatewayService  mediaGatewayService;

    @Autowired
    private CameraService cameraService;

    @IgnoreAuth
    @RequestMapping("/init")
    public Result<String> list(@RequestBody MediaGateway mediaGateway){
        log.info("zlm..上报信息");
        mediaGatewayService.saveOrUpdateByMediaServerId(mediaGateway);
        cameraService.addAll2ZLM();
        return Result.OK("ok");
    }
}
