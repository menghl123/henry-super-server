package com.henry.common.auth.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.henry.common.ddd.domain.Auditable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 登录会话服务
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserToken implements Auditable {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名称
     */
    private String userName;

    /**
     * 登录时ip
     */
    private String ip;

    private Long avatarId;

    private String userCode;

    private Integer orderNum;

    private String orgCode;

    private String orgName;

    /**
     * 登录时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime loginTime;

    @Override
    public Long getCreatorId() {
        return this.userId;
    }

    @Override
    public String getCreatorName() {
        return this.getUserName() + "/" + this.getUserCode();
    }

    @Override
    public LocalDateTime getCreatedTime() {
        return LocalDateTime.now();
    }

    @Override
    public Long getModifierId() {
        return this.userId;
    }

    @Override
    public String getModifierName() {
        return this.getUserName() + "/" + this.getUserCode();
    }

    @Override
    public LocalDateTime getModifiedTime() {
        return LocalDateTime.now();
    }

}
