package com.henry.user.infrastructure.converter;

import com.henry.common.ddd.domain.MappableEnum;
import com.henry.common.ddd.infrasturcture.BaseConverter;
import com.henry.user.domain.model.User;
import com.henry.user.domain.model.UserStatus;
import com.henry.user.infrastructure.persistence.UserPO;
import org.mapstruct.Mapper;

/**
 * 用户持久化转换器：领域模型 <-> 持久化模型，MapStruct 编译期自动生成实现（Spring bean）。
 * User 无无参构造/setter，依赖 @SuperBuilder 供 MapStruct 构建器探测构造。
 */
@Mapper(componentModel = "spring")
public interface UserConverter extends BaseConverter<User, UserPO> {

    /** 领域枚举 -> 数据库整型：持久化 UserStatus 的 code */
    default Integer map(UserStatus status) {
        return status == null ? null : status.getCode();
    }

    /** 数据库整型 -> 领域枚举：复用 common 的 MappableEnum.fromCode 解析 */
    default UserStatus map(Integer code) {
        return code == null ? null : MappableEnum.fromCode(UserStatus.class, code);
    }
}
