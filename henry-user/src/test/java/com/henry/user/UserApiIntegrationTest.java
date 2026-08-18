package com.henry.user;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 用户 CRUD 接口集成测试：
 * 这些接口均需登录后携带 token 访问（不在 ignorePatterns 中），
 * 每个用例前重新造数并登录获取新 token，保证用例间隔离。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("用户 CRUD 接口集成测试")
class UserApiIntegrationTest extends AbstractIntegrationTest {

    private String token;

    @BeforeEach
    void setUp() throws Exception {
        jdbcTemplate.update("DELETE FROM sys_user");
        insertUser(1, "admin", "admin123", "管理员", 1);
        insertUser(2, "toupdate", "pwd123", "待修改", 1);
        insertUser(3, "todelete", "pwd123", "待删除", 1);
        token = loginToken("admin", "admin123");
    }

    // ---------- 查询 ----------

    @Test
    @DisplayName("根据ID查询用户成功")
    void getUserSuccess() throws Exception {
        ResponseEntity<String> resp = exchange(HttpMethod.GET, "/user/1", null);

        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("SUC0000");
        assertThat(body.get("body").get("id").asLong()).isEqualTo(1L);
        assertThat(body.get("body").get("username").asText()).isEqualTo("admin");
        assertThat(body.get("body").get("nickname").asText()).isEqualTo("管理员");
        assertThat(body.get("body").has("password")).isFalse(); // 不暴露密码
    }

    @Test
    @DisplayName("查询不存在的用户返回异常")
    void getUserNotFound() throws Exception {
        ResponseEntity<String> resp = exchange(HttpMethod.GET, "/user/999999", null);

        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("HRY0003");
        assertThat(body.get("errorMsg").asText()).contains("用户不存在");
    }

    @Test
    @DisplayName("未携带 token 访问受保护接口被鉴权拦截")
    void getUserWithoutToken() throws Exception {
        ResponseEntity<String> resp = restTemplate.getForEntity("/user/1", String.class);

        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("HRY0002");
    }

    @Test
    @DisplayName("分页查询用户")
    void pageUsers() throws Exception {
        ResponseEntity<String> resp = exchange(HttpMethod.GET, "/user/page?pageIndex=1&pageSize=10", null);

        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("SUC0000");
        assertThat(body.get("body").get("data").size()).isEqualTo(3);
        assertThat(body.get("body").get("total").asLong()).isEqualTo(3L);
        assertThat(body.get("body").get("pageIndex").asLong()).isEqualTo(1L);
    }

    // ---------- 创建 ----------

    @Test
    @DisplayName("创建用户成功，返回新id且密码BCrypt存储")
    void createUserSuccess() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", "newuser");
        payload.put("password", encryptWithPublicKey("newpass123"));
        payload.put("nickname", "新用户");

        ResponseEntity<String> resp = exchange(HttpMethod.POST, "/user", payload);
        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("SUC0000");
        long newId = body.get("body").asLong();
        assertThat(newId).isPositive();

        // 新用户可查询
        JsonNode getBody = objectMapper.readTree(exchange(HttpMethod.GET, "/user/" + newId, null).getBody());
        assertThat(getBody.get("returnCode").asText()).isEqualTo("SUC0000");
        assertThat(getBody.get("body").get("username").asText()).isEqualTo("newuser");
        assertThat(getBody.get("body").get("nickname").asText()).isEqualTo("新用户");

        // 存储为 BCrypt 加盐哈希
        String stored = jdbcTemplate.queryForObject(
                "SELECT password FROM sys_user WHERE username = ?", String.class, "newuser");
        assertThat(stored).startsWith("$2a$");
        assertThat(stored).isNotEqualTo("newpass123");
        assertThat(passwordEncoder.matches("newpass123", stored)).isTrue();
    }

    @Test
    @DisplayName("创建已存在的用户名失败")
    void createUserDuplicate() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", "admin");
        payload.put("password", encryptWithPublicKey("newpass123"));

        ResponseEntity<String> resp = exchange(HttpMethod.POST, "/user", payload);
        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("HRY0003");
        assertThat(body.get("errorMsg").asText()).contains("用户名已存在");
    }

    @Test
    @DisplayName("未携带 token 创建用户被鉴权拦截")
    void createUserWithoutToken() throws Exception {
        Map<String, String> payload = new HashMap<>();
        payload.put("username", "newuser");
        payload.put("password", encryptWithPublicKey("newpass123"));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        ResponseEntity<String> resp = restTemplate.postForEntity(
                "/user", new HttpEntity<>(payload, headers), String.class);

        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("HRY0002");
    }

    // ---------- 修改 ----------

    @Test
    @DisplayName("修改用户昵称成功")
    void updateUserSuccess() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("nickname", "已改名");

        ResponseEntity<String> resp = exchange(HttpMethod.PUT, "/user/2", payload);
        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("SUC0000");
        assertThat(body.get("body").get("id").asLong()).isEqualTo(2L);
        assertThat(body.get("body").get("nickname").asText()).isEqualTo("已改名");

        // 重新查询确认落库
        JsonNode getBody = objectMapper.readTree(exchange(HttpMethod.GET, "/user/2", null).getBody());
        assertThat(getBody.get("body").get("nickname").asText()).isEqualTo("已改名");
    }

    @Test
    @DisplayName("禁用用户后该用户无法登录")
    void updateUserDisable() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("status", 0);

        ResponseEntity<String> resp = exchange(HttpMethod.PUT, "/user/2", payload);
        assertThat(objectMapper.readTree(resp.getBody()).get("returnCode").asText()).isEqualTo("SUC0000");

        // 状态改为禁用后，用原密码登录应失败
        ResponseEntity<String> loginResp = login("toupdate", encryptWithPublicKey("pwd123"));
        JsonNode loginBody = objectMapper.readTree(loginResp.getBody());
        assertThat(loginBody.get("returnCode").asText()).isEqualTo("HRY0003");
        assertThat(loginBody.get("errorMsg").asText()).contains("账号已被禁用");
    }

    @Test
    @DisplayName("修改不存在的用户返回异常")
    void updateUserNotFound() throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("nickname", "x");

        ResponseEntity<String> resp = exchange(HttpMethod.PUT, "/user/999999", payload);
        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("HRY0003");
        assertThat(body.get("errorMsg").asText()).contains("用户不存在");
    }

    @Test
    @DisplayName("无可更新字段时返回异常")
    void updateUserEmptyPayload() throws Exception {
        ResponseEntity<String> resp = exchange(HttpMethod.PUT, "/user/2", new HashMap<>());

        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("HRY0003");
        assertThat(body.get("errorMsg").asText()).contains("无可更新的字段");
    }

    // ---------- 删除 ----------

    @Test
    @DisplayName("删除用户后查询不到（逻辑删除）")
    void deleteUserSuccess() throws Exception {
        ResponseEntity<String> resp = exchange(HttpMethod.DELETE, "/user/3", null);
        assertThat(objectMapper.readTree(resp.getBody()).get("returnCode").asText()).isEqualTo("SUC0000");

        // 删除后按ID查询不到
        JsonNode getBody = objectMapper.readTree(exchange(HttpMethod.GET, "/user/3", null).getBody());
        assertThat(getBody.get("returnCode").asText()).isEqualTo("HRY0003");
        assertThat(getBody.get("errorMsg").asText()).contains("用户不存在");

        // 数据库为逻辑删除（deleted=1）
        Integer deleted = jdbcTemplate.queryForObject(
                "SELECT deleted FROM sys_user WHERE id = 3", Integer.class);
        assertThat(deleted).isEqualTo(1);
    }

    @Test
    @DisplayName("删除不存在的用户返回异常")
    void deleteUserNotFound() throws Exception {
        ResponseEntity<String> resp = exchange(HttpMethod.DELETE, "/user/999999", null);

        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("HRY0003");
        assertThat(body.get("errorMsg").asText()).contains("用户不存在");
    }

    @Test
    @DisplayName("未携带 token 删除用户被鉴权拦截")
    void deleteUserWithoutToken() throws Exception {
        ResponseEntity<String> resp = restTemplate.exchange(
                "/user/3", HttpMethod.DELETE, new HttpEntity<>(new HttpHeaders()), String.class);

        JsonNode body = objectMapper.readTree(resp.getBody());
        assertThat(body.get("returnCode").asText()).isEqualTo("HRY0002");
    }

    // ---------- 工具 ----------

    /** 携带 token 发起请求：GET/DELETE 传 null，POST/PUT 传 body（Object 可被 Jackson 序列化） */
    private ResponseEntity<String> exchange(HttpMethod method, String url, Object body) {
        HttpHeaders headers = authHeaders(token);
        HttpEntity<Object> request = new HttpEntity<>(body, headers);
        return restTemplate.exchange(url, method, request, String.class);
    }
}
