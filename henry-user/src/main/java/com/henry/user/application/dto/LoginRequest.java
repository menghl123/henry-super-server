package com.henry.user.application.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {

    @NotBlank(message = "用户名不能为空")
    private String username;

    /** 密码：先调用 /user/login/public-key 获取公钥，用 RSA 加密后 base64 提交 */
    @NotBlank(message = "密码不能为空")
    private String password;
}
