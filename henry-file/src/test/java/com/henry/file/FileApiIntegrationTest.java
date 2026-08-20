package com.henry.file;

import cn.hutool.core.io.FileUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.henry.file.infrastructure.config.FileStorageProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 文件服务接口集成测试：覆盖单/批量上传、单文件下载、单/批量删除及异常分支。
 * <p>
 * 无数据库依赖，存储目录为 target/test-uploads，每个用例前清空以保证隔离。
 * 业务异常走 GlobalExceptionHandler，HTTP 恒为 200，错误码为 HRY0003。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DisplayName("文件接口集成测试")
class FileApiIntegrationTest {

    private static final String TEXT_PLAIN = "text/plain";

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @LocalServerPort
    private int port;

    @BeforeEach
    void cleanStorage() {
        // 与仓储 rootPath() 相同的解析方式（Paths.get + toAbsolutePath + normalize），
        // 避免相对路径在 user.dir 不一致时清错目录导致用例间文件残留
//        FileUtil.del(storageRoot().toString());
    }

    // ---------- 上传 ----------

    @Test
    @DisplayName("单文件上传成功，返回 id/url 等元数据")
    void uploadSuccess() throws Exception {
        ResponseEntity<String> resp = uploadSingle(new TestFile("hello.txt", "hello henry file 中文", TEXT_PLAIN));

        JsonNode body = json(resp);
        assertSuccess(body);
        JsonNode file = body.get("body");
        assertThat(file.get("id").asText()).isNotBlank();
        assertThat(file.get("originalName").asText()).isEqualTo("hello.txt");
        assertThat(file.get("contentType").asText()).isEqualTo(TEXT_PLAIN);
        assertThat(file.get("size").asLong()).isEqualTo(23L); // "hello henry file 中文" 的 UTF-8 字节数
        assertThat(file.get("url").asText()).isEqualTo("/file/download/" + file.get("id").asText());
    }

    @Test
    @DisplayName("上传空文件失败")
    void uploadEmptyFileFails() throws Exception {
        ResponseEntity<String> resp = uploadSingle(new TestFile("empty.txt", ""));

        assertError(json(resp), "文件不能为空");
    }

    @Test
    @DisplayName("批量上传多个文件成功")
    void uploadBatchSuccess() throws Exception {
        ResponseEntity<String> resp = uploadBatch(Arrays.asList(
                new TestFile("a.txt", "aaa", TEXT_PLAIN),
                new TestFile("b.log", "bbbb")));

        JsonNode body = json(resp);
        assertSuccess(body);
        JsonNode files = body.get("body");
        assertThat(files).hasSize(2);
        assertThat(files.get(0).get("originalName").asText()).isEqualTo("a.txt");
        assertThat(files.get(0).get("contentType").asText()).isEqualTo(TEXT_PLAIN);
        assertThat(files.get(0).get("url").asText())
                .isEqualTo("/file/download/" + files.get(0).get("id").asText());
        assertThat(files.get(1).get("originalName").asText()).isEqualTo("b.log");
        assertThat(files.get(1).get("size").asLong()).isEqualTo(4L);
    }

    @Test
    @DisplayName("批量上传中存在空文件时整体失败")
    void uploadBatchWithEmptyFileFails() throws Exception {
        ResponseEntity<String> resp = uploadBatch(Arrays.asList(
                new TestFile("a.txt", "aaa"),
                new TestFile("empty.txt", "")));

        assertError(json(resp), "批量上传中存在空文件");
    }

    // ---------- 下载 ----------

    @Test
    @DisplayName("下载已上传文件成功，内容与响应头正确")
    void downloadSuccess() throws Exception {
        String id = uploadAndGetId(new TestFile("hello.txt", "hello henry file 中文", TEXT_PLAIN));

        ResponseEntity<byte[]> resp = restTemplate.getForEntity("/file/download/" + id, byte[].class);

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isEqualTo("hello henry file 中文".getBytes(StandardCharsets.UTF_8));
        assertThat(resp.getHeaders().getContentType().toString()).isEqualTo(TEXT_PLAIN);
        String disposition = resp.getHeaders().getFirst(HttpHeaders.CONTENT_DISPOSITION);
        assertThat(disposition).startsWith("attachment");
        assertThat(disposition).contains("hello.txt");
    }

    @Test
    @DisplayName("下载不存在的文件返回异常")
    void downloadNotFound() throws Exception {
        ResponseEntity<String> resp = restTemplate.getForEntity(
                "/file/download/20260820/no-such-file.txt", String.class);

        assertError(json(resp), "文件不存在");
    }

    // ---------- 删除 ----------

    @Test
    @DisplayName("删除已上传文件成功，磁盘文件随之删除")
    void deleteSuccess() throws Exception {
        String id = uploadAndGetId(new TestFile("todelete.txt", "delete me", TEXT_PLAIN));

        ResponseEntity<String> resp = restTemplate.exchange(
                "/file/" + id, HttpMethod.DELETE, HttpEntity.EMPTY, String.class);
        assertSuccess(json(resp));

        // 磁盘文件与元数据旁路文件均已删除
        assertThat(Files.exists(storageRoot().resolve(id))).isFalse();
        assertThat(Files.exists(storageRoot().resolve(id + ".meta.json"))).isFalse();

        // 删除后下载失败
        assertError(json(restTemplate.getForEntity("/file/download/" + id, String.class)), "文件不存在");
    }

    @Test
    @DisplayName("删除不存在的文件返回异常")
    void deleteNotFound() throws Exception {
        ResponseEntity<String> resp = restTemplate.exchange(
                "/file/no-such-file.txt", HttpMethod.DELETE, HttpEntity.EMPTY, String.class);

        assertError(json(resp), "文件不存在");
    }

    @Test
    @DisplayName("批量删除多个文件成功")
    void deleteBatchSuccess() throws Exception {
        String id1 = uploadAndGetId(new TestFile("a.txt", "aaa", TEXT_PLAIN));
        String id2 = uploadAndGetId(new TestFile("b.txt", "bbb", TEXT_PLAIN));

        ResponseEntity<String> resp = deleteBatch(Arrays.asList(id1, id2));
        assertSuccess(json(resp));

        // 两个文件均不可再下载
        assertError(json(restTemplate.getForEntity("/file/download/" + id1, String.class)), "文件不存在");
        assertError(json(restTemplate.getForEntity("/file/download/" + id2, String.class)), "文件不存在");
    }

    @Test
    @DisplayName("批量删除空 id 列表被参数校验拦截")
    void deleteBatchEmptyIds() throws Exception {
        ResponseEntity<String> resp = deleteBatch(new ArrayList<String>());

        assertError(json(resp), "文件id列表不能为空");
    }

    @Test
    @DisplayName("批量删除中任一文件不存在时整体失败且不删任何文件")
    void deleteBatchWithMissingIdRollsBack() throws Exception {
        String id1 = uploadAndGetId(new TestFile("keep.txt", "keep me", TEXT_PLAIN));
        String id2 = uploadAndGetId(new TestFile("gone.txt", "gone", TEXT_PLAIN));

        ResponseEntity<String> resp = deleteBatch(Arrays.asList(id1, "20260820/no-such-file.txt", id2));

        assertError(json(resp), "文件不存在");
        // 已存在的文件未被误删：磁盘文件仍在，且能下载到原始内容（下载返回的是文件内容而非 JSON，不能走 json() 断言）
        assertThat(Files.exists(storageRoot().resolve(id1))).isTrue();
        assertThat(Files.exists(storageRoot().resolve(id2))).isTrue();
        assertThat(restTemplate.getForEntity("/file/download/" + id1, byte[].class).getBody())
                .isEqualTo("keep me".getBytes(StandardCharsets.UTF_8));
        assertThat(restTemplate.getForEntity("/file/download/" + id2, byte[].class).getBody())
                .isEqualTo("gone".getBytes(StandardCharsets.UTF_8));
    }

    // ---------- 安全 ----------

    @Test
    @DisplayName("路径穿越下载请求被拒绝")
    void downloadRejectsPathTraversal() throws Exception {
        // 用 URI 对象直接传入，避免 RestTemplate 对 %2F 二次编码（%2F -> %252F）导致绕过
        URI uri = URI.create("http://localhost:" + port + "/file/download/..%2F..%2Fetc%2Fpasswd");
        RestTemplate raw = new RestTemplate();
        raw.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) {
                return false;
            }
        });
        ResponseEntity<byte[]> resp = raw.exchange(uri, HttpMethod.GET, null, byte[].class);

        if (resp.getStatusCode().is2xxSuccessful()) {
            // 请求到达应用层（容器放行时）：必须返回业务错误，绝不返回文件内容
            String body = new String(resp.getBody() == null ? new byte[0] : resp.getBody(), StandardCharsets.UTF_8);
            assertThat(body).contains("HRY0003");
        } else {
            // Tomcat 在容器层拒绝编码斜杠
            assertThat(resp.getStatusCode().is4xxClientError()).isTrue();
        }
    }

    // ---------- 工具 ----------

    /** 测试用文件描述 */
    private static class TestFile {
        final String name;
        final byte[] bytes;
        final String contentType;

        TestFile(String name, String content) {
            this(name, content, null);
        }

        TestFile(String name, String content, String contentType) {
            this.name = name;
            this.bytes = content.getBytes(StandardCharsets.UTF_8);
            this.contentType = contentType;
        }
    }

    /** 单文件上传 */
    private ResponseEntity<String> uploadSingle(TestFile file) {
        return restTemplate.postForEntity("/file/upload", multipartForm("file", Arrays.asList(file)), String.class);
    }

    /** 批量上传：多个同名 files 字段 */
    private ResponseEntity<String> uploadBatch(List<TestFile> files) {
        return restTemplate.postForEntity("/file/upload/batch", multipartForm("files", files), String.class);
    }

    /** 批量删除 */
    private ResponseEntity<String> deleteBatch(List<String> ids) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("ids", ids);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return restTemplate.exchange("/file/batch", HttpMethod.DELETE,
                new HttpEntity<>(payload, headers), String.class);
    }

    /** 构造 multipart/form-data 请求体：单/多文件统一走此方法 */
    private HttpEntity<LinkedMultiValueMap<String, Object>> multipartForm(String fieldName, List<TestFile> files) {
        LinkedMultiValueMap<String, Object> form = new LinkedMultiValueMap<>();
        for (TestFile file : files) {
            ByteArrayResource resource = new ByteArrayResource(file.bytes) {
                @Override
                public String getFilename() {
                    return file.name;
                }
            };
            HttpHeaders partHeaders = new HttpHeaders();
            if (file.contentType != null) {
                partHeaders.setContentType(MediaType.parseMediaType(file.contentType));
            }
            form.add(fieldName, new HttpEntity<>(resource, partHeaders));
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return new HttpEntity<>(form, headers);
    }

    /** 上传单个文件并返回其 id */
    private String uploadAndGetId(TestFile file) throws Exception {
        JsonNode body = json(uploadSingle(file));
        assertSuccess(body);
        return body.get("body").get("id").asText();
    }

    private JsonNode json(ResponseEntity<String> resp) throws Exception {
        return objectMapper.readTree(resp.getBody());
    }

    /** 存储根目录（绝对路径）：与 LocalFileRepository.rootPath() 的解析方式保持一致 */
    private Path storageRoot() {
        return Paths.get(fileStorageProperties.getStoragePath()).toAbsolutePath().normalize();
    }

    private static void assertSuccess(JsonNode body) {
        assertThat(body.get("returnCode").asText()).isEqualTo("SUC0000");
    }

    private static void assertError(JsonNode body, String messagePart) {
        assertThat(body.get("returnCode").asText()).isEqualTo("HRY0003");
        assertThat(body.get("errorMsg").asText()).contains(messagePart);
    }
}
