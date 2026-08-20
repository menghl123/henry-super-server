package com.henry.file.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * 文件领域对象：描述一个已存储的文件（纯净，无任何框架注解）。
 * <p>
 * id 为相对存储根目录的路径（如 20260820/3f2c...ab.png），由基础设施层在保存时生成，
 * 可作为下载/删除接口的唯一标识。
 */
@Getter
@Builder
@ToString
public class FileObject {

    /** 相对存储根目录的路径，作为文件唯一标识，如 20260820/3f2c...ab.png */
    private final String id;

    /** 原始文件名（含扩展名） */
    private final String originalName;

    /** MIME 类型，下载时用于响应头 */
    private final String contentType;

    /** 文件字节大小 */
    private final long size;

    /** 存储时间 */
    private final LocalDateTime storedAt;
}
