package com.isharpever.common.id.snowflake.config;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@EnableDubbo(scanBasePackages={"com.isharpever.common.id.snowflake.provider.impl"})
@PropertySource("classpath:/dubbo/dubbo-provider.properties")
public class DubboProviderConfig {

}