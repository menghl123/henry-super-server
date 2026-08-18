package com.henry.user.domain.model;

import com.henry.common.ddd.domain.AuditEntity;
import com.henry.common.ddd.domain.Identifiable;
import lombok.EqualsAndHashCode;
import org.springframework.security.crypto.password.PasswordEncoder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

/**
 * 用户领域实体（纯净，无任何框架注解）
 */
@Getter
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class User extends AuditEntity implements Identifiable<Long> {

    private Long id;

    private final String username;

    private final String password;

    private final String nickname;

    private final UserStatus status;

    /** 新建用户，默认状态为正常 */
    public static User create(String username, String password, String nickname) {
        return User.builder()
                .username(username)
                .password(password)
                .nickname(nickname)
                .status(UserStatus.NORMAL)
                .build();
    }

    /** 密码校验：存储值为 BCrypt 加盐哈希，明文只在此方法中与编码器比对 */
    public boolean matchesPassword(String rawPassword, PasswordEncoder passwordEncoder) {
        return passwordEncoder.matches(rawPassword, password);
    }

    public boolean isDisabled() {
        return status != null && status == UserStatus.DISABLED;
    }

    @Override
    public void initId(final Long id) {
        this.id = id;
    }
}
