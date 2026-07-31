package com.henry.common.security;

import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 注册鉴权拦截器，放行 excludePaths 中配置的路径
 */
public class TokenAuthWebConfig implements WebMvcConfigurer {

    private final JwtUtils jwtUtils;
    private final JwtProperties jwtProperties;
    private final SecurityProperties securityProperties;

    public TokenAuthWebConfig(JwtUtils jwtUtils, JwtProperties jwtProperties, SecurityProperties securityProperties) {
        this.jwtUtils = jwtUtils;
        this.jwtProperties = jwtProperties;
        this.securityProperties = securityProperties;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthInterceptor(jwtUtils, jwtProperties))
                .addPathPatterns("/**")
                .excludePathPatterns(securityProperties.getExcludePaths().toArray(new String[0]));
    }
}
