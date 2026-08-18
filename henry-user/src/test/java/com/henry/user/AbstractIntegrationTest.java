package com.henry.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * 集成测试基类：提供登录、取公钥/RSA 加密、造数据、鉴权请求头等公共工具
 */
public abstract class AbstractIntegrationTest {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected ObjectMapper objectMapper;

    /** 造数据：插入一个用户，密码用真实 BCrypt 编码器加盐哈希 */
    protected void insertUser(long id, String username, String rawPassword, String nickname, int status) {
        jdbcTemplate.update(
                "INSERT INTO sys_user (id, username, password, nickname, status, creator_id, creator_name, created_time) "
                        + "VALUES (?, ?, ?, ?, ?, 0, 'system', CURRENT_TIMESTAMP)",
                id, username, passwordEncoder.encode(rawPassword), nickname, status);
    }

    /** 提交登录请求（password 为已加密的 base64） */
    protected ResponseEntity<String> login(String username, String encryptedPassword) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("password", encryptedPassword);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity("/user/login", new HttpEntity<>(payload, headers), String.class);
    }

    /** 用明文密码登录并返回 token（模拟客户端完整流程） */
    protected String loginToken(String username, String rawPassword) throws Exception {
        JsonNode body = objectMapper.readTree(login(username, encryptWithPublicKey(rawPassword)).getBody());
        return body.get("body").get("token").asText();
    }

    /** 携带 bearer token 的 JSON 请求头 */
    protected HttpHeaders authHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(token);
        return headers;
    }

    /** 获取服务端下发的公钥（X509 base64） */
    protected String fetchPublicKey() throws Exception {
        ResponseEntity<String> resp = restTemplate.getForEntity("/user/login/public-key", String.class);
        JsonNode body = objectMapper.readTree(resp.getBody());
        return body.get("body").asText();
    }

    /** 模拟客户端：用服务端下发的公钥 RSA 加密明文密码（PKCS1Padding + base64） */
    protected String encryptWithPublicKey(String plaintext) throws Exception {
        String publicKeyBase64 = fetchPublicKey();
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8)));
    }
}
