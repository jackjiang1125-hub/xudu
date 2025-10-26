package org.jeecg.modules.hkclients.config;

import org.jeecg.modules.hkclients.HKClients;
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
    public HKClients hkClients(HikPooledClientManager manager) {
        return new HKClients(manager);
    }
}
