package com.henry.user.domain.model;

import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户领域实体（纯净，无任何框架注解）
 */
public class User {

    private final Long id;
    private final String username;
    private final String password;
    private final String nickname;
    private final Integer status;
    private final LocalDateTime createTime;
    private final LocalDateTime updateTime;

    private User(Long id, String username, String password, String nickname,
                 Integer status, LocalDateTime createTime, LocalDateTime updateTime) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.status = status;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    /** 从持久化数据重建 */
    public static User of(Long id, String username, String password, String nickname,
                          Integer status, LocalDateTime createTime, LocalDateTime updateTime) {
        return new User(id, username, password, nickname, status, createTime, updateTime);
    }

    /** 新建用户，默认状态为正常 */
    public static User create(String username, String password, String nickname) {
        return new User(null, username, password, nickname, 1, null, null);
    }

    public boolean matchesPassword(String rawPassword) {
        return password != null && password.equals(rawPassword);
    }

    public boolean isDisabled() {
        return status != null && status == 0;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public Integer getStatus() {
        return status;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }
}
