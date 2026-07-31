package com.henry.user.application;

import com.henry.common.entity.PageQuery;
import com.henry.common.exception.BusinessException;
import com.henry.common.result.PageResult;
import com.henry.common.result.ResultCode;
import com.henry.common.security.JwtUtils;
import com.henry.common.security.LoginUser;
import com.henry.user.application.dto.CreateUserRequest;
import com.henry.user.application.dto.LoginRequest;
import com.henry.user.application.dto.LoginResponse;
import com.henry.user.application.dto.UserDTO;
import com.henry.user.domain.model.User;
import com.henry.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户应用服务：编排领域逻辑，事务与安全在此层
 */
@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByName(request.getUsername())
                .orElseThrow(() -> new BusinessException("用户名或密码错误"));
        if (!user.matchesPassword(request.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }
        if (user.isDisabled()) {
            throw new BusinessException("账号已被禁用");
        }
        LoginUser loginUser = new LoginUser();
        loginUser.setId(user.getId());
        loginUser.setUsername(user.getUsername());
        String token = jwtUtils.createToken(loginUser);
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getNickname());
    }

    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND.getCode(), "用户不存在"));
        return toDTO(user);
    }

    public PageResult<UserDTO> pageUsers(PageQuery query) {
        PageResult<User> page = userRepository.page(query);
        List<UserDTO> dtos = page.getRecords().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
        return PageResult.of(page.getTotal(), dtos, page.getPageNum(), page.getPageSize());
    }

    @Transactional
    public Long createUser(CreateUserRequest request) {
        if (userRepository.existsByName(request.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        User user = User.create(request.getUsername(), request.getPassword(), request.getNickname());
        return userRepository.save(user).getId();
    }

    private UserDTO toDTO(User user) {
        return new UserDTO(user.getId(), user.getUsername(), user.getNickname(),
                user.getStatus(), user.getCreateTime());
    }
}
