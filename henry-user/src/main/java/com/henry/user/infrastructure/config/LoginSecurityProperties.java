package com.henry.user.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 登录密码传输加密配置：RSA 密钥对（base64，PKCS8 私钥 / X509 公钥）
 */
@Data
@ConfigurationProperties(prefix = "henry-user.login-security")
public class LoginSecurityProperties {

    /** 公钥（X509 base64），通过 /user/login/public-key 下发给前端用于加密密码 */
    private String publicKey;

    /** 私钥（PKCS8 base64），仅服务端持有，用于解密前端提交的加密密码 */
    private String privateKey;
}
