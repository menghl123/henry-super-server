package com.henry.file.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储配置：本地存储根目录（绝对路径），由 application.yml 的 app.file 前缀绑定。
 */
@Data
@ConfigurationProperties(prefix = "app.file")
public class FileStorageProperties {

    /** 文件存储根目录（绝对路径），infra 层直接读写该目录 */
    private String storagePath;
}
