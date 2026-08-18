package com.henry.user.application.dto;

import com.henry.user.domain.model.UserStatus;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 修改用户请求：昵称 / 状态（均为可选项，缺省表示不修改）。
 * 状态字段为 UserStatus 枚举，非法 code 由反序列化直接拒绝。
 */
@Data
public class UpdateUserRequest {

    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;

    /** 用户状态（code：0-禁用 1-正常） */
    private UserStatus status;
}
