package com.henry.common.security;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * JWT 配置，前缀 app.jwt
 */
@Data
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /** HS256 密钥，长度需不少于 32 字节 */
    private String secret;

    /** 过期时间（秒） */
    private long expire = 7200L;

    /** 请求头名称 */
    private String header = "Authorization";

    /** token 前缀 */
    private String tokenPrefix = "Bearer ";
}
