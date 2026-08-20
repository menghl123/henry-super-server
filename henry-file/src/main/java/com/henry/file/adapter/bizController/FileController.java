package com.henry.file.adapter.bizController;

import com.henry.common.response.StandardResponse;
import com.henry.file.application.dto.FileDTO;
import com.henry.file.application.dto.FileIdsRequest;
import com.henry.file.application.service.FileApplicationService;
import com.henry.file.domain.model.FileObject;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import javax.validation.Valid;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 文件接口：单/批量上传、单文件下载、单/批量删除。
 * <p>
 * 文件 id 含日期路径（如 20260820/xxx.png），下载/删除使用 {*id} 通配捕获整段剩余路径，
 * 因此需要 application.yml 中 spring.mvc.pathmatch.matching-strategy=path_pattern_parser。
 */
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileApplicationService fileApplicationService;

    /** 单文件上传：multipart 字段名 file */
    @PostMapping("/upload")
    public StandardResponse<FileDTO> upload(@RequestParam("file") final MultipartFile file) {
        return StandardResponse.success(fileApplicationService.upload(file));
    }

    /** 批量上传：multipart 中多个同名 files 字段 */
    @PostMapping("/upload/batch")
    public StandardResponse<List<FileDTO>> uploadBatch(@RequestParam("files") final List<MultipartFile> files) {
        return StandardResponse.success(fileApplicationService.uploadBatch(files));
    }

    /** 单文件下载 */
    @GetMapping("/download/{*id}")
    public ResponseEntity<StreamingResponseBody> download(@PathVariable final String id) {
        final FileObject fileObject = fileApplicationService.getFile(id);
        final StreamingResponseBody body = out -> {
            try (final InputStream in = fileApplicationService.openStream(id)) {
                StreamUtils.copy(in, out);
            }
        };
        return ResponseEntity.ok()
                .contentType(safeMediaType(fileObject.getContentType()))
                .contentLength(fileObject.getSize())
                .header(HttpHeaders.CONTENT_DISPOSITION, contentDisposition(fileObject.getOriginalName()))
                .body(body);
    }

    /** 单文件删除 */
    @DeleteMapping("/{*id}")
    public StandardResponse<Void> delete(@PathVariable final String id) {
        fileApplicationService.delete(id);
        return StandardResponse.success();
    }

    /** 批量删除：请求体为 id 列表 */
    @DeleteMapping("/batch")
    public StandardResponse<Void> deleteBatch(@RequestBody @Valid final FileIdsRequest request) {
        fileApplicationService.deleteBatch(request.getIds());
        return StandardResponse.success();
    }

    /** Content-Disposition：兼容中文/特殊字符文件名（RFC 5987 filename*） */
    private String contentDisposition(final String originalName) {
        final String fallback = originalName == null ? "unnamed" : originalName.replace("\"", "_");
        final String encoded = urlEncode(fallback);
        return "attachment; filename=\"" + fallback + "\"; filename*=UTF-8''" + encoded;
    }

    private String urlEncode(final String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.name()).replace("+", "%20");
        } catch (final UnsupportedEncodingException e) {
            throw new IllegalStateException("文件名编码失败", e);
        }
    }

    /** 非法 MIME 类型降级为 octet-stream，避免响应构造失败 */
    private MediaType safeMediaType(final String contentType) {
        try {
            return MediaType.parseMediaType(contentType);
        } catch (final Exception e) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
}
