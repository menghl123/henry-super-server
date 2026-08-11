package com.henry.common.auth.model;

import com.henry.common.ddd.application.dto.response.AuditDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

/**
 * 标准的会话token
 */
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
@Data
@EqualsAndHashCode(callSuper = true)
public class StandardToken extends AuditDTO {
    private Object data;
}
