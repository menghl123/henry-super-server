package com.henry.common.ddd.infrasturcture;

import java.util.List;

public interface BaseConverter<Entity, PO> {
    PO toPO(final Entity entity);

    Entity toEntity(final PO po);

    List<PO> toPO(final List<Entity> entities);

    List<Entity> toEntity(final List<PO> pos);
}
