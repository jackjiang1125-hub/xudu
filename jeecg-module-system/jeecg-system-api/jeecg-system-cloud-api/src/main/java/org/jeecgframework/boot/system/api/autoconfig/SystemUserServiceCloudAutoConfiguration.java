package org.jeecgframework.boot.system.api.autoconfig;

import org.jeecg.common.system.api.ISysBaseAPI;
import org.jeecgframework.boot.system.api.SystemUserService;
import org.jeecgframework.boot.system.api.impl.SystemUserServiceFeignImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.context.annotation.Import;

/**
 * 自动配置：Cloud 场景下注册 SystemUserService 的 Feign 实现
 * 无需修改入口类，通过 AutoConfiguration 自动生效
 */
@AutoConfiguration
@ConditionalOnClass(ISysBaseAPI.class)
@ConditionalOnMissingClass("org.jeecg.modules.system.service.impl.SysBaseApiImpl")
@ConditionalOnMissingBean(SystemUserService.class)
@Import(SystemUserServiceFeignImpl.class)
public class SystemUserServiceCloudAutoConfiguration {
}