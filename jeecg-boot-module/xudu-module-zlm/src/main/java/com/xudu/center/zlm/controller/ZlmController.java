// controller/ZlmController.java
package com.xudu.center.zlm.controller;

import com.xudu.center.zlm.config.ZlmProperties;
import com.xudu.center.zlm.dto.NormalizeReq;
import com.xudu.center.zlm.dto.NormalizeResult;
import com.xudu.center.zlm.dto.PlayUrls;
import com.xudu.center.zlm.dto.ProxyAndNormalizeReq;
import com.xudu.center.zlm.service.ZlmService;
import com.xudu.center.zlm.util.PlayUrlBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/zlm")
@RequiredArgsConstructor
public class ZlmController {

    private final ZlmService service;
    private final ZlmProperties zlmProperties;

    /** 添加代理流 */
    @PostMapping("/proxy")
    public Object addProxy(@RequestParam String schema,
                           @RequestParam String app,
                           @RequestParam String stream,
                           @RequestParam String url,
                           @RequestParam(required=false) Integer rtpType,
                           @RequestParam(required=false) Boolean closeWhenNoConsumer) {
        return service.addProxy(schema, app, stream, url, rtpType, closeWhenNoConsumer);
    }

    /** 查询编解码（视频/音频） */
    @GetMapping("/streams/{app}/{stream}/codec")
    public Object codec(@PathVariable String app, @PathVariable String stream) {
        return service.probeCodec(app, stream);
    }

    /** 仅生成播放 URL（不触发转码） */
    @GetMapping("/streams/{app}/{stream}/urls")
    public PlayUrls urls(@PathVariable String app, @PathVariable String stream) {
        return PlayUrlBuilder.of(zlmProperties, app, stream);
    }

    /** 规范化：若友好→直接返回源；否则 addFFmpegSource 产出“转码流”并返回 */
    // controller/ZlmController.java（normalize 保持原样，接收 playMode 字段即可）
    @PostMapping("/normalize")
    public NormalizeResult normalize(@Validated @RequestBody NormalizeReq req) {
        return service.normalizeForBrowser(req);
    }

    // controller/ZlmController.java（新增一个端点）
    @PostMapping("/proxy-and-normalize")
    public NormalizeResult proxyAndNormalize(@Validated @RequestBody ProxyAndNormalizeReq req) {
        return service.addProxyAndNormalize(req);
    }


}
