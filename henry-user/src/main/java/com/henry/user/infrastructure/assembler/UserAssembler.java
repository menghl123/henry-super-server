package com.henry.user.infrastructure.assembler;

import com.henry.user.domain.model.User;
import com.henry.user.infrastructure.persistence.UserPO;
import org.springframework.stereotype.Component;

/**
 * 领域模型 <-> 持久化模型 转换器
 */
@Component
public class UserAssembler {

    public User toDomain(UserPO po) {
        return User.of(po.getId(), po.getUsername(), po.getPassword(), po.getNickname(),
                po.getStatus(), po.getCreateTime(), po.getUpdateTime());
    }

    public UserPO toPO(User user) {
        UserPO po = new UserPO();
        po.setId(user.getId());
        po.setUsername(user.getUsername());
        po.setPassword(user.getPassword());
        po.setNickname(user.getNickname());
        po.setStatus(user.getStatus());
        po.setCreateTime(user.getCreateTime());
        po.setUpdateTime(user.getUpdateTime());
        return po;
    }
}
