package com.henry.common.ddd.domain;

import java.time.LocalDateTime;

public interface Auditable {
    default Long getCreatorId() {
        return null;
    }

    default String getCreatorName() {
        return null;
    }

    default LocalDateTime getCreatedTime() {
        return null;
    }

    default Long getModifierId() {
        return null;
    }

    default String getModifierName() {
        return null;
    }

    default LocalDateTime getModifiedTime() {
        return null;
    }

    default Boolean getDeleted() {
        return Boolean.FALSE;
    }
}
