package com.henry.user;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import javax.crypto.Cipher;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 登录接口集成测试：
 * 使用 H2 内存库（MySQL 兼容模式），走真实 HTTP（随机端口），
 * 完整链路：取公钥 → RSA 加密密码 → 提交登录 → 鉴权过滤器 → 应用服务 → 数据库。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("登录接口集成测试")
class LoginIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 造数据：每个用例前清空并重建用户（密码用真实 BCrypt 编码器加盐哈希，保证与生产一致）
     */
    @BeforeEach
    void seedData() {
        jdbcTemplate.update("DELETE FROM sys_user");
        insertUser("admin", "admin123", "管理员", 1);
        insertUser("disabled", "disabled123", "已禁用", 0);
    }

    private void insertUser(String username, String rawPassword, String nickname, int status) {
        jdbcTemplate.update(
                "INSERT INTO sys_user (id, username, password, nickname, status, creator_id, creator_name, created_time) "
                        + "VALUES (?, ?, ?, ?, ?, 0, 'system', CURRENT_TIMESTAMP)",
                System.nanoTime(), username, passwordEncoder.encode(rawPassword), nickname, status);
    }

    // ---------- 用例 ----------

    @Test
    @DisplayName("公钥接口免鉴权返回 RSA 公钥")
    void publicKeyIsReturned() throws Exception {
        ResponseEntity<String> resp = restTemplate.getForEntity("/user/login/public-key", String.class);
        assertThat(resp.getStatusCode().is2xxSuccessful()).isTrue();

        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("SUC0000");
        // X509 公钥 base64 以 MIIB 开头，可用于加密 6-32 位密码
        assertThat(body.get("body").asText()).startsWith("MIIB");
    }

    @Test
    @DisplayName("正确密码登录成功，返回 token 与用户信息")
    void loginSuccess() throws Exception {
        ResponseEntity<String> resp = login("admin", encryptWithPublicKey("admin123"));

        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("SUC0000");
        assertThat(body.get("body").get("token").asText()).isNotBlank();
        assertThat(body.get("body").get("userId").asLong()).isPositive();
        assertThat(body.get("body").get("username").asText()).isEqualTo("admin");
        assertThat(body.get("body").get("nickname").asText()).isEqualTo("管理员");
    }

    @Test
    @DisplayName("密码错误登录失败，返回统一文案")
    void loginWrongPassword() throws Exception {
        ResponseEntity<String> resp = login("admin", encryptWithPublicKey("wrong-pass"));

        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("HRY0003");
        assertThat(body.get("errorMsg").asText()).contains("用户名或密码错误");
    }

    @Test
    @DisplayName("用户不存在与密码错误返回同一文案（防账号枚举）")
    void loginUnknownUser() throws Exception {
        ResponseEntity<String> resp = login("not-exist", encryptWithPublicKey("whatever"));

        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("HRY0003");
        assertThat(body.get("errorMsg").asText()).contains("用户名或密码错误");
    }

    @Test
    @DisplayName("禁用账号登录失败")
    void loginDisabledUser() throws Exception {
        ResponseEntity<String> resp = login("disabled", encryptWithPublicKey("disabled123"));

        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("HRY0003");
        assertThat(body.get("errorMsg").asText()).contains("账号已被禁用");
    }

    @Test
    @DisplayName("提交未加密密码，服务端解密失败")
    void loginWithPlainPassword() throws Exception {
        ResponseEntity<String> resp = login("admin", "plain-password");

        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("HRY0003");
        assertThat(body.get("errorMsg").asText()).contains("密码解密失败");
    }

    @Test
    @DisplayName("数据库中的密码为 BCrypt 加盐哈希而非明文")
    void storedPasswordIsBcryptHashed() throws Exception {
        String stored = jdbcTemplate.queryForObject(
                "SELECT password FROM sys_user WHERE username = ?", String.class, "admin");

        assertThat(stored).isNotNull();
        assertThat(stored).startsWith("$2a$"); // BCrypt 哈希前缀
        assertThat(stored).isNotEqualTo("admin123");
        assertThat(passwordEncoder.matches("admin123", stored)).isTrue();
        assertThat(passwordEncoder.matches("admin123x", stored)).isFalse();
    }

    // ---------- 工具 ----------

    private ResponseEntity<String> login(String username, String encryptedPassword) {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", username);
        payload.put("password", encryptedPassword);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.postForEntity("/user/login", new HttpEntity<>(payload, headers), String.class);
    }

    private String fetchPublicKey() throws Exception {
        ResponseEntity<String> resp = restTemplate.getForEntity("/user/login/public-key", String.class);
        JsonNode body = objectMapper.readTree(resp.getBody());
        return body.get("body").asText();
    }

    /** 模拟客户端：用服务端下发的公钥 RSA 加密明文密码（PKCS1Padding + base64） */
    private String encryptWithPublicKey(String plaintext) throws Exception {
        String publicKeyBase64 = fetchPublicKey();
        byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(keyBytes));

        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return Base64.getEncoder().encodeToString(cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8)));
    }
}
