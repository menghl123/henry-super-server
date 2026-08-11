package com.henry.user.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.henry.common.entity.PageQuery;
import com.henry.common.result.StandardPage;
import com.henry.user.domain.model.User;
import com.henry.user.domain.repository.UserRepository;
import com.henry.user.infrastructure.assembler.UserAssembler;
import com.henry.user.infrastructure.mapper.UserMapper;
import com.henry.user.infrastructure.persistence.UserPO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class UserRepositoryImpl implements UserRepository {

    private final UserMapper userMapper;
    private final UserAssembler userAssembler;

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(userMapper.selectById(id)).map(userAssembler::toDomain);
    }

    @Override
    public Optional<User> findByName(String username) {
        return Optional.ofNullable(userMapper.selectOne(
                        new LambdaQueryWrapper<UserPO>().eq(UserPO::getUsername, username)))
                .map(userAssembler::toDomain);
    }

    @Override
    public boolean existsByName(String username) {
        Long count = userMapper.selectCount(
                new LambdaQueryWrapper<UserPO>().eq(UserPO::getUsername, username));
        return count != null && count > 0;
    }

    @Override
    public StandardPage<User> page(PageQuery query) {
        Page<UserPO> page = userMapper.selectPage(
                new Page<>(query.getPageNum(), query.getPageSize()),
                new LambdaQueryWrapper<UserPO>().orderByDesc(UserPO::getId));
        List<User> users = page.getRecords().stream()
                .map(userAssembler::toDomain)
                .collect(Collectors.toList());
        return StandardPage.of(page.getTotal(), users, query.getPageNum(), query.getPageSize());
    }

    @Override
    public User save(User user) {
        UserPO po = userAssembler.toPO(user);
        userMapper.insert(po);
        return userAssembler.toDomain(po);
    }
}
