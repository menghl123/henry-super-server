package com.henry.user.application.dto;

import lombok.Value;

import java.time.LocalDateTime;

/**
 * 用户输出 DTO（不含密码）
 */
@Value
public class UserDTO {

    Long id;

    String username;

    String nickname;

    Integer status;

    LocalDateTime createTime;
}
