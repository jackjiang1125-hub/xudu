package org.jeecg.modules.iot.config;


import org.jeecg.modules.iot.device.cache.AccDeviceRedisCache;
import org.jeecg.modules.iot.device.mapstruct.IotDeviceMapstruct;
import org.jeecg.modules.iot.device.protocol.AccDeviceMessageProcessor;
import org.jeecg.modules.iot.device.service.*;

import org.jeecg.modules.iot.server.IotNettyServer;
import org.jeecg.modules.iot.server.IotNettyServerProperties;

import org.jeecg.modules.iot.service.CompositeDeviceMessageProcessor;
import org.jeecg.modules.iot.service.DeviceMessageProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.List;

/**
 * Auto configuration that wires and starts the Netty server for IoT device communication.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(IotNettyServer.class)
@EnableConfigurationProperties(IotNettyServerProperties.class)
@ConditionalOnProperty(prefix = "jeecg.iot.netty", name = "enabled", havingValue = "true", matchIfMissing = true)
public class IotNettyAutoConfiguration {

    /**
     * Netty 服务本身：注入的 DeviceMessageProcessor 会是下面这个 compositeDeviceMessageProcessor。
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    public IotNettyServer iotNettyServer(IotNettyServerProperties properties,
                                         DeviceMessageProcessor deviceMessageProcessor) {
        return new IotNettyServer(properties, deviceMessageProcessor);
    }

    /**
     * 聚合所有 DeviceMessageProcessor（hk + zk + 以后新增的）。
     * - List<DeviceMessageProcessor> 会自动注入容器里所有实现了这个接口的 Bean，
     *   包括 AccDeviceMessageProcessor、HkEventRecordProcessor 等。
     * - 这里返回的就是最终对外使用的总入口。
     */
    @Bean
    @Primary
    public DeviceMessageProcessor compositeDeviceMessageProcessor(List<DeviceMessageProcessor> processors) {
        // 这里的 processors 是所有“叶子” processor，Composite 自己不是 Bean 类，所以不会出现在这个列表里
        return new CompositeDeviceMessageProcessor(processors);
    }
}

