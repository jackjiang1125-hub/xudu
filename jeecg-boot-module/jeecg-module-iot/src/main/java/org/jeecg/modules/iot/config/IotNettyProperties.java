package org.jeecg.modules.iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Description: IoT Netty配置属性
 * @Author: jeecg-boot
 * @Date: 2024-01-01
 * @Version: V1.0
 */
@Data
@Component
@ConfigurationProperties(prefix = "jeecg.iot.http")
public class IotNettyProperties {

    /**
     * HTTP服务器端口
     */
    private int port = 19080;

    /**
     * Boss线程数
     */
    private int bossThreads = 1;

    /**
     * Worker线程数
     */
    private int workerThreads = 4;

    /**
     * 下行超时时间（毫秒）
     */
    private long timeoutMs = 3000;
}
