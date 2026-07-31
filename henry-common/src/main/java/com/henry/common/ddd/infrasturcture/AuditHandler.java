package com.henry.common.ddd.infrasturcture;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.henry.common.ddd.domain.Auditable;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;

public class AuditHandler implements MetaObjectHandler {

    @Override
    public void insertFill(final MetaObject metaObject) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Auditable auditable = (Auditable) authentication.getPrincipal();

        this.setFieldValByName("createdTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("modifiedTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("creatorId", auditable.getCreatorId(), metaObject);
        this.setFieldValByName("creatorName", auditable.getCreatorName(), metaObject);
        this.setFieldValByName("modifierName", auditable.getModifierName(), metaObject);
        this.setFieldValByName("modifierId", auditable.getModifierId(), metaObject);
    }

    @Override
    public void updateFill(final MetaObject metaObject) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final Auditable auditable = (Auditable) authentication.getPrincipal();

        this.setFieldValByName("modifierId", auditable.getModifierId(), metaObject);
        this.setFieldValByName("modifiedTime", LocalDateTime.now(), metaObject);
        this.setFieldValByName("modifierName", auditable.getModifierName(), metaObject);
    }
}

