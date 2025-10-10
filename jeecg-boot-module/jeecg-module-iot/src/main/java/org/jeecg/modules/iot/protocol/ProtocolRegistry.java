package org.jeecg.modules.iot.protocol;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Description: 协议注册中心
 * @Author: jeecg-boot
 * @Date: 2024-01-01
 * @Version: V1.0
 */
@Slf4j
@Component
public class ProtocolRegistry implements CommandLineRunner {

    @Autowired
    private List<ProtocolAdapter> adapters;

    private final Map<String, ProtocolAdapter> adapterMap = new HashMap<>();



    /**
     * 注册协议适配器
     * @param adapter 适配器
     */
    public void register(ProtocolAdapter adapter) {
        if (adapter.supports("HTTP")) {
            adapterMap.put("HTTP", adapter);
            log.info("注册HTTP协议适配器: {}", adapter.name());
        }
        if (adapter.supports("MQTT")) {
            adapterMap.put("MQTT", adapter);
            log.info("注册MQTT协议适配器: {}", adapter.name());
        }
    }

    /**
     * 获取协议适配器
     * @param protocol 协议类型
     * @return 适配器
     */
    public ProtocolAdapter adapterFor(String protocol) {
        return adapterMap.get(protocol);
    }

    /**
     * 发送下行数据
     * @param protocol 协议类型
     * @param deviceSn 设备序列号
     * @param payload 下行数据
     * @return 是否成功
     */
    public boolean sendDownlink(String protocol, String deviceSn, byte[] payload) {
        ProtocolAdapter adapter = adapterFor(protocol);
        if (adapter == null) {
            log.warn("未找到协议适配器: {}", protocol);
            return false;
        }
        return adapter.sendDownlink(deviceSn, payload);
    }

    /**
     * 测试设备连接
     * @param protocol 协议类型
     * @param deviceSn 设备序列号
     * @return 是否成功
     */
    public boolean testConnect(String protocol, String deviceSn) {
        ProtocolAdapter adapter = adapterFor(protocol);
        if (adapter == null) {
            log.warn("未找到协议适配器: {}", protocol);
            return false;
        }
        return adapter.testConnect(deviceSn);
    }

    @Override
    public void run(String... args) throws Exception {
        for (ProtocolAdapter adapter : adapters) {
            register(adapter);
        }
        log.info("协议注册中心初始化完成，注册适配器数量: {}", adapterMap.size());
    }
}
