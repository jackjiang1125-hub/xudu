package com.xudu.center.video.camera.controller;

import java.util.List;

import org.jeecg.common.api.vo.Result;
import com.xudu.center.video.camera.entity.CameraDevice;
import com.xudu.center.video.camera.service.CameraService;
import com.xudu.center.video.camera.vo.CameraVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ipc/camera")
public class CameraController {

    @Autowired
    private CameraService cameraService;

    @PostMapping("/add")
    public Result<String> register(@RequestBody CameraVO req) {
        List<CameraDevice> devices = cameraService.register(req);
        long streamCount = devices.stream().filter(device -> StringUtils.hasText(device.getStreamId())).count();
        return Result.OK("Registered " + streamCount + " channels successfully");
    }

    @GetMapping("play")
    public Result<CameraDevice> play(String id) {
        CameraDevice detail = cameraService.getDetail(id);
        return Result.OK(detail);
    }

    @GetMapping("/list")
    public Result<List<CameraDevice>> list() {
        List<CameraDevice> topLevel = cameraService.listTopLevel();
        return Result.OK(topLevel);
    }

    @GetMapping("/top-nvr")
    public Result<List<CameraDevice>> listTopNvr() {
        List<CameraDevice> nvrList = cameraService.listTopLevelNvr();
        return Result.OK(nvrList);
    }

    @GetMapping("/children")
    public Result<List<CameraDevice>> listChildren(@RequestParam("id") String id) {
        if (!StringUtils.hasText(id)) {
            return Result.error("id不能为空");
        }
        List<CameraDevice> children = cameraService.listChildren(id);
        return Result.OK(children);
    }

    @DeleteMapping("/deleteById")
    public Result<String> deleteById(@RequestParam("id") String id) {
        boolean removed = cameraService.delete(id);
        if (!removed) {
            return Result.error("删除失败");
        }
        return Result.ok("sucess");
    }
}
