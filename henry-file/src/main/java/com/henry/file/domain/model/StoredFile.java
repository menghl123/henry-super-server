package com.henry.file.domain.model;

import com.henry.common.exception.BusinessException;

import java.util.UUID;

/**
 * 已存储文件值对象：负责文件名清洗与存储名生成
 */
public final class StoredFile {

    private static final String SEPARATOR = "_";

    private final String storedName;
    private final String originalName;

    private StoredFile(String storedName, String originalName) {
        this.storedName = storedName;
        this.originalName = originalName;
    }

    /** 新建存储文件：生成存储名（UUID_原名） */
    public static StoredFile create(String originalName) {
        String cleaned = clean(originalName);
        String storedName = UUID.randomUUID().toString().replace("-", "") + SEPARATOR + cleaned;
        return new StoredFile(storedName, cleaned);
    }

    /** 从存储名反解析（用于下载） */
    public static StoredFile fromStoredName(String storedName) {
        if (storedName == null || storedName.trim().isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }
        int index = storedName.indexOf(SEPARATOR);
        String originalName = index >= 0 ? storedName.substring(index + 1) : storedName;
        return new StoredFile(storedName, originalName);
    }

    private static String clean(String originalName) {
        if (originalName == null || originalName.trim().isEmpty()) {
            throw new BusinessException("文件名不能为空");
        }
        String name = originalName.trim().replace("\\", "/");
        if (name.contains("..")) {
            throw new BusinessException("非法文件名");
        }
        return name;
    }

    public String getStoredName() {
        return storedName;
    }

    public String getOriginalName() {
        return originalName;
    }
}
