package com.henry.common.util;

import cn.hutool.core.exceptions.ValidateException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.tomcat.util.codec.binary.Base64;

import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class RSASerializeUtil {
    private static final String KEY_ALGORITHM = "RSA";

    /**
     * 公钥序列化为base64字符串
     */
    public static String serializePublicKey(final RSAPublicKey key) {
        return Base64.encodeBase64String(key.getEncoded());
    }

    /**
     * 私钥序列化为base64字符串
     */
    public static String serializePrivateKey(final RSAPrivateKey key) {
        return Base64.encodeBase64String(key.getEncoded());
    }

    /**
     * base64字符串反序列化为公钥
     */
    public static RSAPublicKey deSerializePublicKey(String publicKeyStr) {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decodeBase64(publicKeyStr));

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSASerializeUtil.KEY_ALGORITHM);
            return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        } catch (Exception var4) {
            throw new ValidateException("get rsa public key failed");
        }
    }

    /**
     * base64字符串反序列化为私钥
     */
    public static RSAPrivateKey deSerializePrivateKey(String privateKeyStr) {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(Base64.decodeBase64(privateKeyStr));

        try {
            KeyFactory keyFactory = KeyFactory.getInstance(RSASerializeUtil.KEY_ALGORITHM);
            return (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
        } catch (Exception var4) {
            throw new ValidateException("get rsa private key failed");
        }
    }
}
