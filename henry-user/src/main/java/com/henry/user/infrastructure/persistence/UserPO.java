package com.henry.user.infrastructure.persistence;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.henry.common.ddd.domain.Identifiable;
import com.henry.common.ddd.infrasturcture.AuditPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * 用户持久化对象（与数据库表一一对应，含 MyBatis-Plus 注解）
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
@Accessors(chain = true)
@AllArgsConstructor
public class UserPO extends AuditPO implements Identifiable<Long> {

    /** 主键：默认 ASSIGN_ID（雪花），与建表 SQL 中 id 无自增保持一致 */
    @TableId(value = "id")
    private Long id;

    @TableField(value = "username")
    private String username;

    @TableField(value = "password")
    private String password;

    @TableField(value = "nickname")
    private String nickname;

    /** 状态：UserStatus.code（0-禁用 1-正常），持久化层保持整型，映射见 UserConverter */
    @TableField(value = "status")
    private Integer status;

    @Override
    public void initId(final Long id) {
        this.id = id;
    }
}
