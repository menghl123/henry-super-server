package com.henry.user.domain.repository;

import com.henry.common.entity.PageQuery;
import com.henry.common.result.StandardPage;
import com.henry.user.domain.model.User;

import java.util.Optional;

/**
 * 用户仓储接口（domain 定义，infrastructure 实现）
 */
public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByName(String username);

    boolean existsByName(String username);

    StandardPage<User> page(PageQuery query);

    User save(User user);
}
