package com.henry.file.application.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import java.util.List;

/**
 * 批量操作请求：文件 id 列表（用于批量删除）
 */
@Data
public class FileIdsRequest {

    @NotEmpty(message = "文件id列表不能为空")
    private List<String> ids;
}
