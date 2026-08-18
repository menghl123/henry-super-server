package com.henry.user.application.dto;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

/**
 * 修改用户请求：昵称 / 状态（均为可选项，缺省表示不修改）
 */
@Data
public class UpdateUserRequest {

    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;

    /** 状态 1-正常 0-禁用 */
    @Min(value = 0, message = "状态只能为0或1")
    @Max(value = 1, message = "状态只能为0或1")
    private Integer status;
}
