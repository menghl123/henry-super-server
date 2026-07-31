package com.henry.file.infrastructure.repository;

import com.henry.common.exception.BusinessException;
import com.henry.file.domain.model.StoredFile;
import com.henry.file.domain.repository.FileRepository;
import com.henry.file.infrastructure.config.FileProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * 本地磁盘文件仓储实现
 */
@Repository
@RequiredArgsConstructor
public class LocalFileRepository implements FileRepository {

    private final FileProperties properties;

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(root());
    }

    @Override
    public void store(StoredFile file, InputStream content) {
        Path target = safePath(file.getStoredName());
        try {
            Files.copy(content, target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new BusinessException("文件保存失败");
        }
    }

    @Override
    public InputStream load(StoredFile file) {
        Path path = safePath(file.getStoredName());
        if (!Files.exists(path) || !Files.isReadable(path)) {
            throw new BusinessException("文件不存在");
        }
        try {
            return Files.newInputStream(path);
        } catch (IOException e) {
            throw new BusinessException("文件读取失败");
        }
    }

    private Path safePath(String storedName) {
        Path root = root();
        Path path = root.resolve(storedName).normalize();
        if (!path.startsWith(root)) {
            throw new BusinessException("非法文件名");
        }
        return path;
    }

    private Path root() {
        return Paths.get(properties.getStoragePath()).toAbsolutePath().normalize();
    }
}
