package org.jeecgframework.boot.system.api.autoconfig;

import org.jeecgframework.boot.system.api.SystemUserService;
import org.jeecgframework.boot.system.api.impl.SystemUserServiceLocalImpl;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Import;

/**
 * 自动配置：单体/本地场景下注册 SystemUserService 的本地实现
 * 无需修改入口类，通过 AutoConfiguration 自动生效
 */
@AutoConfiguration
@ConditionalOnClass(name = "org.jeecg.modules.system.service.impl.SysBaseApiImpl")
@ConditionalOnMissingBean(SystemUserService.class)
@Import(SystemUserServiceLocalImpl.class)
public class SystemUserServiceLocalAutoConfiguration {
}