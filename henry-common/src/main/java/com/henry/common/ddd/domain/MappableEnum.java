package com.henry.common.ddd.domain;

import java.util.Objects;
import java.util.stream.Stream;

public interface MappableEnum {

    String getMessage();

    Integer getCode();

    static <T extends MappableEnum> T fromCode(final Class<T> enumType, final Integer value) {
        return Stream.of(enumType.getEnumConstants())
                .filter(enumConstant -> Objects.equals(enumConstant.getCode(), value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No enum value" + value + "of" + enumType.getCanonicalName()));
    }

    static <T extends MappableEnum> T fromMessage(final Class<T> enumType, final String message) {
        return Stream.of(enumType.getEnumConstants())
                .filter(enumConstant -> Objects.equals(enumConstant.getMessage(), message))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("No enum value" + message + "of" + enumType.getCanonicalName()));
    }
}
