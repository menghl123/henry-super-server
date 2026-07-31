package com.henry.common.ddd.domain;

import com.henry.common.query.PageQuery;
import com.henry.common.response.StandardPage;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

public interface CurdRepository<ID extends Serializable, Entity> {
    boolean create(final Entity entity);

    boolean create(final List<Entity> entities);

    boolean update(final Entity entity);

    boolean update(final List<Entity> entities);

    boolean remove(final ID id);

    boolean remove(final List<ID> ids);

    Optional<Entity> find(final ID id);

    List<Entity> find(final List<ID> ids);

    List<Entity> list(final PageQuery pageQuery);

    StandardPage<Entity> page(final PageQuery pageQuery);
}
