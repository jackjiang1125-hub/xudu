package com.xudu.center.video.camera.controller;

import com.xudu.center.video.camera.service.IVideoService;
import com.xudu.center.video.camera.vo.VideoQuery;
import com.xudu.center.video.camera.vo.VideoVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.jeecg.common.api.vo.Result;
import org.jeecgframework.boot.common.vo.PageRequest;
import org.jeecgframework.boot.common.vo.PageResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Tag(name = "视频流管理")
@RestController
@RequestMapping("/video")
public class VideoController {

    @Autowired
    private IVideoService videoService;

    @PostMapping("/add")
    @Operation(summary = "添加视频流")
    public Result<String> add(@RequestBody VideoVO videoVO) {
        videoService.addVideo(videoVO);
        return Result.OK("视频流添加成功");
    }

    @GetMapping("/list")
    @Operation(summary = "分页查询视频流列表")
    public Result<PageResult<VideoVO>> list(VideoQuery videoQuery,
                                           PageRequest pageRequest,
                                           HttpServletRequest req) {
        PageResult<VideoVO> result = videoService.list(videoQuery, pageRequest, req.getParameterMap());
        return Result.OK(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID查询视频流")
    public Result<VideoVO> getById(@PathVariable String id) {
        VideoVO videoVO = videoService.getById(id);
        if (videoVO == null) {
            return Result.error("视频流不存在");
        }
        return Result.OK(videoVO);
    }

    @PutMapping("/update")
    @Operation(summary = "更新视频流")
    public Result<String> update(@RequestBody VideoVO videoVO) {
        videoService.updateVideo(videoVO);
        return Result.OK("视频流更新成功");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除视频流")
    public Result<String> delete(@PathVariable String id) {
        videoService.deleteVideo(id);
        return Result.OK("视频流删除成功");
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除视频流")
    public Result<String> deleteBatch(@RequestBody List<String> ids) {
        videoService.deleteBatchVideo(ids);
        return Result.OK("批量删除成功");
    }
}
