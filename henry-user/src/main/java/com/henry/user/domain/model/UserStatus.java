package com.henry.user.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.henry.common.ddd.domain.MappableEnum;

/**
 * 用户状态枚举，继承 common 的基础枚举 {@link MappableEnum}。
 * 对外/落库均使用 code（0-禁用 1-正常），序列化与反序列化由 Jackson 注解保证格式不变。
 */
public enum UserStatus implements MappableEnum {

    /** 禁用 */
    DISABLED(0, "禁用"),
    /** 正常 */
    NORMAL(1, "正常");

    private final Integer code;
    private final String message;

    UserStatus(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    /** 序列化：枚举 -> 0/1，保证对外接口与数据库整型字段格式不变 */
    @JsonValue
    public Integer value() {
        return code;
    }

    /** 反序列化：status: 0/1 -> 枚举，复用 common 的枚举解析 */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static UserStatus fromCode(final Integer code) {
        return code == null ? null : MappableEnum.fromCode(UserStatus.class, code);
    }
}
