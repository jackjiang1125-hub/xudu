package org.jeecg.modules.iot.protocol.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.jeecg.modules.iot.protocol.ProtocolAdapter;
import org.springframework.stereotype.Component;

/**
 * @Description: MQTT协议适配器（占位实现）
 * @Author: jeecg-boot
 * @Date: 2024-01-01
 * @Version: V1.0
 */
@Slf4j
@Component
public class MqttAdapterStub implements ProtocolAdapter {

    @Override
    public String name() {
        return "MQTT协议适配器（占位实现）";
    }

    @Override
    public boolean supports(String protocol) {
        return "MQTT".equalsIgnoreCase(protocol);
    }

    @Override
    public void start() throws Exception {
        log.info("MQTT协议适配器启动（占位实现）");
        // TODO: 实现MQTT协议适配器启动逻辑
    }

    @Override
    public void stop() throws Exception {
        log.info("MQTT协议适配器停止（占位实现）");
        // TODO: 实现MQTT协议适配器停止逻辑
    }

    @Override
    public boolean sendDownlink(String deviceSn, byte[] payload) {
        log.info("MQTT下行数据（占位实现）: deviceSn={}, payload={}", deviceSn, new String(payload));
        // TODO: 实现MQTT下行数据发送逻辑
        return true;
    }

    @Override
    public boolean testConnect(String deviceSn) {
        log.info("MQTT连接测试（占位实现）: deviceSn={}", deviceSn);
        // TODO: 实现MQTT连接测试逻辑
        return true;
    }
}
