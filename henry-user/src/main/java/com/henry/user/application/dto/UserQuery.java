package com.henry.user.application.dto;

import com.henry.common.query.PageQuery;
import com.henry.common.query.Query;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 用户分页查询条件：
 * username / nickname 模糊搜索，status 精准搜索，可任意组合（字段之间为 AND 关系）。
 * 由 common 的 QueryConverter 依据 @Query 注解自动生成查询条件。
 */
@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@EqualsAndHashCode(callSuper = true)
public class UserQuery extends PageQuery {

    /** 用户名模糊搜索 */
    @Query(value = Query.Condition.LIKE)
    private String username;

    /** 用户昵称模糊搜索 */
    @Query(value = Query.Condition.LIKE)
    private String nickname;

    /** 状态精准搜索 1-正常 0-禁用 */
    @Query(value = Query.Condition.EQ)
    private Integer status;
}
