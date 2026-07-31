package com.henry.user.application.dto;

import lombok.Value;

@Value
public class LoginResponse {

    String token;

    Long userId;

    String username;

    String nickname;
}
