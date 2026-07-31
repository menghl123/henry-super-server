package com.henry.file.domain.repository;

import com.henry.file.domain.model.StoredFile;

import java.io.InputStream;

/**
 * 文件仓储接口（domain 定义，infrastructure 实现）
 */
public interface FileRepository {

    void store(StoredFile file, InputStream content);

    InputStream load(StoredFile file);
}
