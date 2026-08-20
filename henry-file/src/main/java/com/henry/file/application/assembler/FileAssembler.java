package com.henry.file.application.assembler;

import com.henry.file.application.dto.FileDTO;
import com.henry.file.domain.model.FileObject;
import org.springframework.stereotype.Component;

/**
 * 文件组装器：领域模型 -> 输出 DTO。
 * <p>
 * 字段简单且仅此一个映射方向，手动装配即可，无需引入 MapStruct 注解处理器。
 */
@Component
public class FileAssembler {

    /** 下载地址前缀，与 {@code FileController} 单文件下载接口保持一致 */
    private static final String DOWNLOAD_URL_PREFIX = "/file/download/";

    public FileDTO toDTO(final FileObject file) {
        return FileDTO.builder()
                .id(file.getId())
                .originalName(file.getOriginalName())
                .contentType(file.getContentType())
                .size(file.getSize())
                .storedAt(file.getStoredAt())
                .url(DOWNLOAD_URL_PREFIX + file.getId())
                .build();
    }
}
