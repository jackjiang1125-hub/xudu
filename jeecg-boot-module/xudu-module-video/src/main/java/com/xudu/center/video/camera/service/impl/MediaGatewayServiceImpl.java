// Impl
package com.xudu.center.video.camera.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.service.impl.JeecgServiceImpl;
import com.xudu.center.video.camera.entity.MediaGateway;
import com.xudu.center.video.camera.mapper.MediaGatewayMapper;
import com.xudu.center.video.camera.service.IMediaGatewayService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MediaGatewayServiceImpl
        extends JeecgServiceImpl<MediaGatewayMapper, MediaGateway>
        implements IMediaGatewayService {

    private final ObjectMapper om = new ObjectMapper();

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveOrUpdateByMediaServerId(MediaGateway gw) {

        if (gw.getMediaServerId() == null || gw.getMediaServerId().isEmpty()) {
            throw new IllegalArgumentException("mediaServerId 不能为空");
        }
        // 聚合未映射项
        try {
            if (gw.get_extraCollector() != null && !gw.get_extraCollector().isEmpty()) {
                gw.setExtraJson(om.writeValueAsString(gw.get_extraCollector()));
            }
        } catch (Exception e) {
            log.warn("序列化 extraJson 失败: {}", e.getMessage());
        }

        MediaGateway exist = this.getOne(new LambdaQueryWrapper<MediaGateway>()
                .eq(MediaGateway::getMediaServerId, gw.getMediaServerId()), false);
        if (exist == null) {
            this.save(gw);
        } else {
            gw.setId(exist.getId());
            this.updateById(gw);
        }
    }
}
