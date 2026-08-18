package com.henry.user.application.dto;

import com.henry.common.ddd.application.dto.response.AuditDTO;
import com.henry.user.domain.model.UserStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户输出 DTO（不含密码）
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class UserDTO extends AuditDTO {

    private Long id;

    private String username;

    private String nickname;

    /** 用户状态（序列化为 code：0-禁用 1-正常） */
    private UserStatus status;
}
