package com.henry.file.infrastructure.storage;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.henry.file.application.repository.FileRepository;
import com.henry.file.domain.model.FileObject;
import com.henry.file.infrastructure.config.FileStorageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

/**
 * 本地磁盘文件仓储实现：将文件直接写入 yml 配置的绝对路径目录（app.file.storage-path）下。
 * <p>
 * 存储结构：{storagePath}/{yyyyMMdd}/{uuid}{ext}，id 即为相对根目录的路径。
 * 文件元数据（原始文件名、MIME、大小、存储时间）以 {文件名}.meta.json 旁路文件保存，
 * 因此服务无数据库依赖，重启后仍可通过 id 还原文件信息。
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class LocalFileRepository implements FileRepository {

    private static final String META_SUFFIX = ".meta.json";
    private static final DateTimeFormatter DATE_DIR_FORMAT = DateTimeFormatter.BASIC_ISO_DATE;

    private final FileStorageProperties fileStorageProperties;

    @PostConstruct
    public void init() {
        if (!StringUtils.hasText(fileStorageProperties.getStoragePath())) {
            throw new IllegalStateException("app.file.storage-path 未配置");
        }
        try {
            Files.createDirectories(rootPath());
        } catch (final IOException e) {
            throw new IllegalStateException("无法创建文件存储目录: " + rootPath(), e);
        }
    }

    @Override
    public FileObject save(final InputStream content, final String originalName, final String contentType) {
        final String safeName = safeOriginalName(originalName);
        final String normalizedType = normalizeContentType(contentType);
        final String dateDir = LocalDate.now().format(DATE_DIR_FORMAT);
        final String storedName = UUID.randomUUID().toString().replace("-", "") + extensionOf(safeName);
        final String id = dateDir + "/" + storedName;

        final Path root = rootPath();
        final Path targetDir = root.resolve(dateDir);
        final Path target = targetDir.resolve(storedName);
        try {
            Files.createDirectories(targetDir);
            final long size = Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
            final LocalDateTime storedAt = LocalDateTime.now();
            writeMeta(target, safeName, normalizedType, size, storedAt);
            log.info("文件保存成功: id={}, size={}", id, size);
            return toFileObject(target, id, safeName, normalizedType, size, storedAt);
        } catch (final IOException e) {
            throw new IllegalStateException("文件保存失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<FileObject> find(final String id) {
        final Path target = resolveUnderRoot(id);
        if (target == null || !Files.exists(target) || Files.isDirectory(target)) {
            return Optional.empty();
        }
        try {
            return Optional.of(readMeta(target, id));
        } catch (final IOException e) {
            throw new IllegalStateException("读取文件元数据失败: " + e.getMessage(), e);
        }
    }

    @Override
    public Optional<InputStream> openStream(final String id) {
        final Path target = resolveUnderRoot(id);
        if (target == null || !Files.exists(target) || Files.isDirectory(target)) {
            return Optional.empty();
        }
        try {
            return Optional.of(Files.newInputStream(target));
        } catch (final IOException e) {
            throw new IllegalStateException("打开文件失败: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean remove(final String id) {
        final Path target = resolveUnderRoot(id);
        if (target == null || !Files.exists(target) || Files.isDirectory(target)) {
            return false;
        }
        try {
            Files.deleteIfExists(target);
            Files.deleteIfExists(metaPath(target));
            log.info("文件删除成功: id={}", id);
            return true;
        } catch (final IOException e) {
            throw new IllegalStateException("文件删除失败: " + e.getMessage(), e);
        }
    }

    /** 存储根目录（绝对路径） */
    private Path rootPath() {
        return Paths.get(fileStorageProperties.getStoragePath()).toAbsolutePath().normalize();
    }

    /** 元数据旁路文件路径 */
    private Path metaPath(final Path target) {
        return Paths.get(target.toString() + META_SUFFIX);
    }

    /**
     * 将调用方传入的 id 解析为根目录内的绝对路径。
     * 拒绝空串、含 ".." 的路径穿越片段；归一化前导斜杠后解析，结果必须仍位于根目录内。
     */
    private Path resolveUnderRoot(final String id) {
        if (StrUtil.isBlank(id) || id.contains("..")) {
            return null;
        }
        // {*id} 捕获的路径可能带前导斜杠，若直接 resolve 会被当作绝对路径替换根目录，先去掉
        final String normalizedId = id.replace('\\', '/').replaceAll("^/+", "");
        if (StrUtil.isBlank(normalizedId)) {
            return null;
        }
        final Path root = rootPath();
        final Path resolved = root.resolve(normalizedId).normalize();
        if (!resolved.startsWith(root)) {
            return null;
        }
        return resolved;
    }

    /** 写入元数据旁路文件 */
    private void writeMeta(final Path target, final String originalName, final String contentType,
                           final long size, final LocalDateTime storedAt) throws IOException {
        final JSONObject meta = JSONUtil.createObj()
                .set("originalName", originalName)
                .set("contentType", contentType)
                .set("size", size)
                .set("storedAt", storedAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        Files.write(metaPath(target), JSONUtil.toJsonStr(meta).getBytes(StandardCharsets.UTF_8));
    }

    /** 从磁盘还原文件对象：读取旁路元数据，元数据缺失时降级为从文件名/文件属性推断 */
    private FileObject readMeta(final Path target, final String id) throws IOException {
        String originalName = target.getFileName().toString();
        String contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        long size = Files.size(target);
        LocalDateTime storedAt = LocalDateTime.ofInstant(
                Files.getLastModifiedTime(target).toInstant(), ZoneId.systemDefault());

        final Path meta = metaPath(target);
        if (Files.exists(meta)) {
            final JSONObject metaObj = JSONUtil.readJSONObject(meta.toFile(), StandardCharsets.UTF_8);
            if (metaObj.containsKey("originalName")) {
                originalName = metaObj.getStr("originalName");
            }
            if (metaObj.containsKey("contentType")) {
                contentType = metaObj.getStr("contentType");
            }
            if (metaObj.containsKey("size")) {
                size = metaObj.getLong("size", size);
            }
            if (metaObj.containsKey("storedAt")) {
                storedAt = LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(metaObj.getLong("storedAt")), ZoneId.systemDefault());
            }
        }
        return toFileObject(target, id, originalName, contentType, size, storedAt);
    }

    private FileObject toFileObject(final Path target, final String id, final String originalName,
                                    final String contentType, final long size, final LocalDateTime storedAt) {
        return FileObject.builder()
                .id(id)
                .originalName(originalName)
                .contentType(contentType)
                .size(size)
                .storedAt(storedAt)
                .build();
    }

    /** 只保留文件名部分，去除客户端可能带入的目录路径 */
    private String safeOriginalName(final String originalName) {
        if (StrUtil.isBlank(originalName)) {
            return "unnamed";
        }
        final String name = originalName.replace('\\', '/');
        final int idx = name.lastIndexOf('/');
        return idx >= 0 ? name.substring(idx + 1) : name;
    }

    /** 提取扩展名（含点），无扩展名或超长扩展名返回空串 */
    private String extensionOf(final String originalName) {
        final int idx = originalName.lastIndexOf('.');
        if (idx < 0) {
            return "";
        }
        final String ext = originalName.substring(idx);
        return ext.length() <= 10 ? ext : "";
    }

    private String normalizeContentType(final String contentType) {
        return StrUtil.isBlank(contentType) ? MediaType.APPLICATION_OCTET_STREAM_VALUE : contentType;
    }
}
