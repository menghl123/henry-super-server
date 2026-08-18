package com.henry.user.infrastructure.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 登录安全配置：密码 BCrypt 加盐哈希 + 登录密码传输加密属性
 */
@Configuration
@EnableConfigurationProperties(LoginSecurityProperties.class)
public class LoginSecurityConfig {

    /** BCrypt 密码加密器：密码加盐哈希存储与校验 */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
