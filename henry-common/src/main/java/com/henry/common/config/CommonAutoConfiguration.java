package com.henry.common.config;

import com.henry.common.exception.GlobalExceptionHandler;
import com.henry.common.security.JwtProperties;
import com.henry.common.security.JwtUtils;
import com.henry.common.security.SecurityProperties;
import com.henry.common.security.TokenAuthWebConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * common 模块自动装配：子服务引入依赖即生效，无需额外配置。
 * - 统一异常处理（GlobalExceptionHandler）
 * - JWT 工具（需配置 app.jwt.secret）
 * - 鉴权拦截器（需配置 app.security.enabled=true）
 */
@Configuration
@EnableConfigurationProperties({JwtProperties.class, SecurityProperties.class})
@Import(GlobalExceptionHandler.class)
public class CommonAutoConfiguration {

    @Bean
    @ConditionalOnProperty(prefix = "app.jwt", name = "secret")
    public JwtUtils jwtUtils(JwtProperties properties) {
        return new JwtUtils(properties);
    }

    @Bean
    @ConditionalOnProperty(prefix = "app.security", name = "enabled", havingValue = "true")
    public TokenAuthWebConfig tokenAuthWebConfig(JwtUtils jwtUtils, JwtProperties jwtProperties,
                                                 SecurityProperties securityProperties) {
        return new TokenAuthWebConfig(jwtUtils, jwtProperties, securityProperties);
    }
}
