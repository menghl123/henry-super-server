package com.henry.file.application.repository;

import com.henry.file.domain.model.FileObject;

import java.io.InputStream;
import java.util.Optional;

/**
 * 文件仓储接口（application 定义，infrastructure 实现）。
 * <p>
 * 存储实现直接面向本地磁盘目录，文件元数据以旁路 meta 文件保存在磁盘上，服务无数据库依赖。
 */
public interface FileRepository {

    /**
     * 保存文件内容到存储根目录，返回文件对象（含新生成的 id）
     */
    FileObject save(InputStream content, String originalName, String contentType);

    /**
     * 按 id 查询文件元数据，文件不存在返回 empty
     */
    Optional<FileObject> find(String id);

    /**
     * 打开文件内容流（调用方负责关闭），文件不存在返回 empty
     */
    Optional<InputStream> openStream(String id);

    /**
     * 删除文件（含元数据旁路文件），文件不存在返回 false
     */
    boolean remove(String id);
}
