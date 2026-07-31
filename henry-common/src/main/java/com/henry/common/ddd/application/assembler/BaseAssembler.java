package com.henry.common.ddd.application.assembler;

import java.util.List;

public interface BaseAssembler<DTO, Entity> {
    DTO toDTO(final Entity entity);

    List<DTO> toDTO(final List<Entity> entities);
}
