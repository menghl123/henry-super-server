package com.henry.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

@Slf4j
public class SecretKeyGenerator {
    private static final String KEY_ALGORITHM = "RSA";

    public static void RS256() {
        KeyPairGenerator keyPairGenerator;
        try {
            keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
            keyPairGenerator.initialize(2048);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
            RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

            String priStr = RSASerializeUtil.serializePrivateKey(privateKey);
            String pubStr = RSASerializeUtil.serializePublicKey(publicKey);
            log.info("RSA私钥: " + priStr);
            log.info("RSA公钥: " + pubStr);
        } catch (NoSuchAlgorithmException exception) {
            log.error("unknown algorithm");
        }
    }
}
