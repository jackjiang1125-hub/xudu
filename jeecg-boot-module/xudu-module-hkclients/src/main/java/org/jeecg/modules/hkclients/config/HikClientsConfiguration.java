package org.jeecg.modules.hkclients.config;

import org.jeecg.modules.hkclients.clients.HkAccessControlClient;
import org.jeecg.modules.hkclients.clients.HkNvrClient;
import org.jeecg.modules.hkclients.http.HikPooledClientManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HikClientsConfiguration {

    @Bean(initMethod = "start", destroyMethod = "shutdown")
    public HikPooledClientManager hikPooledClientManager() {
        return new HikPooledClientManager();
    }

    @Bean
    public HkNvrClient hkNvrClients(HikPooledClientManager manager) {
        return new HkNvrClient(manager);
    }

    @Bean
    public HkAccessControlClient hkAccessControlClients(HikPooledClientManager manager) {
        return new HkAccessControlClient(manager);
    }
}
