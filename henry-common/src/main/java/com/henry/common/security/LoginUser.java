package com.henry.common.security;

import lombok.Data;

import java.io.Serializable;

/**
 * 登录用户信息（写入 JWT 与 UserContext）
 */
@Data
public class LoginUser implements Serializable {

    private Long id;

    private String username;
}
