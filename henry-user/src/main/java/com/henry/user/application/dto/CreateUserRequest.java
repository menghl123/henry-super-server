package com.henry.user.application.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class CreateUserRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 50, message = "用户名不能超过50个字符")
    private String username;

    /**
     * 密码：先调用 /user/login/public-key 获取公钥，用 RSA 加密后 base64 提交。
     * 长度校验（6-32 位）在服务端解密后执行，见 UserApplicationService.createUser
     */
    @NotBlank(message = "密码不能为空")
    private String password;

    @Size(max = 50, message = "昵称不能超过50个字符")
    private String nickname;
}
