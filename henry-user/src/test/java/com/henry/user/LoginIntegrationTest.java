package com.henry.user;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 登录接口集成测试：
 * 使用 H2 内存库（MySQL 兼容模式），走真实 HTTP（随机端口），
 * 完整链路：取公钥 → RSA 加密密码 → 提交登录 → 鉴权过滤器 → 应用服务 → 数据库。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("登录接口集成测试")
class LoginIntegrationTest extends AbstractIntegrationTest {

    /**
     * 造数据：每个用例前清空并重建用户（密码用真实 BCrypt 编码器加盐哈希，保证与生产一致）
     */
    @BeforeEach
    void seedData() {
        jdbcTemplate.update("DELETE FROM sys_user");
        insertUser(1, "admin", "admin123", "管理员", 1);
        insertUser(2, "disabled", "disabled123", "已禁用", 0);
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
}
