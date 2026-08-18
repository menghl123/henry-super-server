package com.henry.user.adapter.controller;

import com.henry.common.response.StandardResponse;
import com.henry.user.application.support.PasswordSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录公钥下发：前端在调用登录/注册前先获取公钥，用其 RSA 加密密码后再提交
 */
@RestController
@RequestMapping("/user/login")
@RequiredArgsConstructor
public class LoginPublicKeyController {

    private final PasswordSupport passwordSupport;

    @GetMapping("/public-key")
    public StandardResponse<String> publicKey() {
        return StandardResponse.success(passwordSupport.publicKey());
    }
}
