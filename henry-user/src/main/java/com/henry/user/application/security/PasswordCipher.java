package com.henry.user.application.security;

import com.henry.common.util.RSASerializeUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

/**
 * 登录密码传输加解密：前端用服务端下发的公钥 RSA 加密密码，服务端用私钥解密
 */
@Component
@RequiredArgsConstructor
public class PasswordCipher {

    private static final String RSA_TRANSFORMATION = "RSA/ECB/PKCS1Padding";

    private final LoginSecurityProperties properties;

    /**
     * 服务端解密前端提交的加密密码（base64）
     *
     * @param encryptedBase64 前端用公钥加密后的密码（base64）
     * @return 明文密码
     */
    public String decrypt(String encryptedBase64) {
        try {
            final RSAPrivateKey privateKey = RSASerializeUtil.deSerializePrivateKey(properties.getPrivateKey());
            final Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            final byte[] plain = cipher.doFinal(Base64.getDecoder().decode(encryptedBase64));
            return new String(plain, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("密码解密失败，请使用服务端下发的公钥加密后再提交", e);
        }
    }

    /**
     * 客户端/测试用：用公钥加密明文密码
     */
    public String encrypt(String plaintext) {
        try {
            final RSAPublicKey publicKey = RSASerializeUtil.deSerializePublicKey(properties.getPublicKey());
            final Cipher cipher = Cipher.getInstance(RSA_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            final byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new IllegalArgumentException("密码加密失败", e);
        }
    }
}
