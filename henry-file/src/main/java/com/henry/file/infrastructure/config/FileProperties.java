package com.henry.file.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 文件存储配置，前缀 app.file
 */
@Data
@ConfigurationProperties(prefix = "app.file")
public class FileProperties {

    /** 存储根目录 */
    private String storagePath = "uploads";
}
