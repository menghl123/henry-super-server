package com.henry.user.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.TableName;
import com.henry.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户持久化对象（与数据库表一一对应，含 MyBatis-Plus 注解）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class UserPO extends BaseEntity {

    private String username;

    private String password;

    private String nickname;

    private Integer status;
}
