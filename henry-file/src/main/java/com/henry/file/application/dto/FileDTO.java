package com.henry.file.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文件输出 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileDTO {

    /** 文件唯一标识（相对存储路径），可用于下载/删除 */
    private String id;

    /** 原始文件名 */
    private String originalName;

    /** MIME 类型 */
    private String contentType;

    /** 字节大小 */
    private Long size;

    /** 存储时间 */
    private LocalDateTime storedAt;

    /** 下载地址（相对路径） */
    private String url;
}
