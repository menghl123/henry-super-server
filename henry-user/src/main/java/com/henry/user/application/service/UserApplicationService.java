package com.henry.user.application.service;

import com.henry.common.auth.AuthenticateService;
import com.henry.common.auth.model.UserToken;
import com.henry.common.query.PageQuery;
import com.henry.common.response.StandardPage;
import com.henry.user.application.assembler.UserAssembler;
import com.henry.user.application.dto.CreateUserRequest;
import com.henry.user.application.dto.LoginRequest;
import com.henry.user.application.dto.LoginResponse;
import com.henry.user.application.dto.UpdateUserRequest;
import com.henry.user.application.dto.UserDTO;
import com.henry.user.application.repository.UserRepository;
import com.henry.user.application.support.PasswordSupport;
import com.henry.user.domain.model.User;
import com.henry.user.domain.model.UserStatus;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户应用服务：编排领域逻辑，事务与安全在此层
 */
@Service
@RequiredArgsConstructor
public class UserApplicationService {

    /** 前端入口标识，签发 token 时使用 */
    private static final String BIZ_ENTRY = "portal";

    private static final int PASSWORD_MIN_LENGTH = 6;
    private static final int PASSWORD_MAX_LENGTH = 32;

    private final AuthenticateService authenticateService;
    private final PasswordSupport passwordSupport;
    private final PasswordEncoder passwordEncoder;
    private final UserAssembler userAssembler;
    private final UserRepository userRepository;

    public LoginResponse login(LoginRequest request) {
        final User user = userRepository.findByName(request.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("用户名或密码错误"));
        // 密码经公钥加密传输，解密后与 BCrypt 加盐哈希比对
        final String rawPassword = passwordSupport.decrypt(request.getPassword());
        if (!user.matchesPassword(rawPassword, passwordEncoder)) {
            throw new IllegalArgumentException("用户名或密码错误");
        }
        if (user.isDisabled()) {
            throw new IllegalArgumentException("账号已被禁用");
        }
        final UserToken userToken = new UserToken();
        userToken.setUserId(user.getId());
        userToken.setUserName(user.getUsername());
        userToken.setUserCode(user.getUsername());
        userToken.setLoginTime(LocalDateTime.now());
        final String token = authenticateService.generateBizToken(userToken, BIZ_ENTRY);
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname());
    }

    public UserDTO getUserById(Long id) {
        final User user = userRepository.find(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        return userAssembler.toDTO(user);
    }

    public StandardPage<UserDTO> pageUsers(PageQuery query) {
        return userRepository.page(query)
                .converter(userAssembler::toDTO);
    }

    @Transactional
    public Long createUser(CreateUserRequest request) {
        if (userRepository.existsByName(request.getUsername())) {
            throw new IllegalArgumentException("用户名已存在");
        }
        // 密码经公钥加密传输，解密后做业务校验并 BCrypt 加盐哈希存储
        final String rawPassword = passwordSupport.decrypt(request.getPassword());
        if (rawPassword.length() < PASSWORD_MIN_LENGTH || rawPassword.length() > PASSWORD_MAX_LENGTH) {
            throw new IllegalArgumentException("密码长度需在6-32位之间");
        }
        final User user = User.create(request.getUsername(), passwordEncoder.encode(rawPassword), request.getNickname());
        userRepository.create(user);
        return user.getId();
    }

    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        if (request.getNickname() == null && request.getStatus() == null) {
            throw new IllegalArgumentException("无可更新的字段");
        }
        final User user = userRepository.find(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        final String nickname = request.getNickname() != null ? request.getNickname() : user.getNickname();
        final UserStatus status = request.getStatus() != null ? request.getStatus() : user.getStatus();
        final User updated = user.toBuilder()
                .nickname(nickname)
                .status(status)
                .build();
        userRepository.update(updated);
        return userAssembler.toDTO(updated);
    }

    public void deleteUser(Long id) {
        userRepository.find(id)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));
        userRepository.remove(id);
    }
}
