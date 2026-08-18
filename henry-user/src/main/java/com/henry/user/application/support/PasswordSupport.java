package com.henry.user.application.support;

/**
 * 密码支持：应用层使用的密码能力抽象。
 * 具体技术实现（RSA 加解密、密钥管理）位于 infrastructure 层，应用层不依赖实现细节。
 */
public interface PasswordSupport {

    /**
     * 解密前端用公钥加密后提交的密码（base64），返回明文
     */
    String decrypt(String encryptedBase64);

    /**
     * 获取登录密码加密公钥（X509 base64），供前端加密使用
     */
    String publicKey();
}
