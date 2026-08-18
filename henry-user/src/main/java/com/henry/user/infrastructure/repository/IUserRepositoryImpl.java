package com.henry.user.infrastructure.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.henry.common.ddd.infrasturcture.CurdRepositoryImpl;
import com.henry.common.query.PageQuery;
import com.henry.common.query.Sort;
import com.henry.common.response.StandardPage;
import com.henry.user.domain.model.User;
import com.henry.user.application.repository.UserRepository;
import com.henry.user.infrastructure.converter.UserConverter;
import com.henry.user.infrastructure.mapper.UserMapper;
import com.henry.user.infrastructure.persistence.UserPO;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.Optional;

/**
 * 用户仓储实现：继承框架 CRUD 基类，补充按用户名查询能力
 */
@Repository
public class IUserRepositoryImpl extends CurdRepositoryImpl<Long, User, UserPO, UserMapper, UserConverter>
        implements UserRepository {

    @Override
    public Optional<User> findByName(String username) {
        final UserPO po = this.getOne(new LambdaQueryWrapper<UserPO>().eq(UserPO::getUsername, username), false);
        return Optional.ofNullable(po).map(converter::toEntity);
    }

    @Override
    public boolean existsByName(String username) {
        return this.count(new LambdaQueryWrapper<UserPO>().eq(UserPO::getUsername, username)) > 0;
    }

    @Override
    public StandardPage<User> page(PageQuery query) {
        if (CollectionUtils.isEmpty(query.getSorts())) {
            query.setSorts(Collections.singletonList(Sort.builder().name("id").type(Sort.DESC).build()));
        }
        return super.page(query);
    }
}
