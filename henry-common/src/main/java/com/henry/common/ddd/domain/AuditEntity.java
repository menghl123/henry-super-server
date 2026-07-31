package com.henry.common.ddd.domain;

import lombok.Getter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Getter
@ToString
@SuperBuilder(toBuilder = true)
public class AuditEntity implements Auditable {
    private final Long creatorId;
    private final String creatorName;
    private final LocalDateTime createdTime;
    private final Long modifierId;
    private final String modifierName;
    private final LocalDateTime modifiedTime;
    private final Boolean deleted;
}
