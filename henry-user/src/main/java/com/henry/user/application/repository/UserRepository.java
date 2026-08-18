package com.henry.user.application.repository;

import com.henry.common.ddd.domain.CurdRepository;
import com.henry.user.domain.model.User;

import java.util.Optional;

/**
 * 用户仓储接口（domain 定义，infrastructure 实现）
 */
public interface UserRepository extends CurdRepository<Long, User> {

    Optional<User> findByName(String username);

    boolean existsByName(String username);
}
