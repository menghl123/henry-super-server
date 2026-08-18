package com.henry.user.infrastructure.converter;

import com.henry.common.ddd.infrasturcture.BaseConverter;
import com.henry.user.domain.model.User;
import com.henry.user.infrastructure.persistence.UserPO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 领域模型 <-> 持久化模型 转换器
 */
@Component
public class UserConverter implements BaseConverter<User, UserPO> {

    @Override
    public UserPO toPO(final User user) {
        return UserPO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .password(user.getPassword())
                .nickname(user.getNickname())
                .status(user.getStatus())
                .creatorId(user.getCreatorId())
                .creatorName(user.getCreatorName())
                .createdTime(user.getCreatedTime())
                .modifierId(user.getModifierId())
                .modifierName(user.getModifierName())
                .modifiedTime(user.getModifiedTime())
                .deleted(user.getDeleted())
                .build();
    }

    @Override
    public User toEntity(final UserPO po) {
        return User.builder()
                .id(po.getId())
                .username(po.getUsername())
                .password(po.getPassword())
                .nickname(po.getNickname())
                .status(po.getStatus())
                .creatorId(po.getCreatorId())
                .creatorName(po.getCreatorName())
                .createdTime(po.getCreatedTime())
                .modifierId(po.getModifierId())
                .modifierName(po.getModifierName())
                .modifiedTime(po.getModifiedTime())
                .deleted(po.getDeleted())
                .build();
    }

    @Override
    public List<UserPO> toPO(final List<User> users) {
        return users.stream().map(this::toPO).collect(Collectors.toList());
    }

    @Override
    public List<User> toEntity(final List<UserPO> pos) {
        return pos.stream().map(this::toEntity).collect(Collectors.toList());
    }
}
