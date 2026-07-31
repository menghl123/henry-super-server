package com.henry.common.ddd.infrasturcture;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.common.collect.ImmutableList;
import com.henry.common.ddd.domain.CurdRepository;
import com.henry.common.ddd.domain.Identifiable;
import com.henry.common.query.PageQuery;
import com.henry.common.query.QueryConverter;
import com.henry.common.response.StandardPage;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class CurdRepositoryImpl<ID extends Serializable, Entity, PO, M extends BaseMapper<PO>, C extends BaseConverter<Entity, PO>>
        extends ServiceImpl<M, PO> implements CurdRepository<ID, Entity> {

    @Autowired
    protected C converter;

    @Override
    public boolean create(Entity entity) {
        final PO po = converter.toPO(entity);
        final boolean ret = super.save(po);
        this.copyId(ImmutableList.of(entity), ImmutableList.of(po));
        return ret;
    }

    @Override
    public boolean create(List<Entity> entities) {
        final List<PO> pos = converter.toPO(entities);
        final boolean ret = super.saveBatch(pos);
        this.copyId(entities, pos);
        return ret;
    }

    @Override
    public boolean update(Entity entity) {
        return super.updateById(converter.toPO(entity));
    }

    @Override
    public boolean update(List<Entity> entities) {
        return super.updateBatchById(converter.toPO(entities));
    }

    @Override
    public boolean remove(ID id) {
        return super.removeById(id);
    }

    @Override
    public boolean remove(List<ID> ids) {
        return super.removeBatchByIds(ids);
    }

    @Override
    public Optional<Entity> find(ID id) {
        final PO po = super.getById(id);
        return (Objects.isNull(po)) ? Optional.empty() : Optional.of(converter.toEntity(po));
    }

    @Override
    public List<Entity> find(List<ID> ids) {
        return converter.toEntity(super.listByIds(ids));
    }

    @Override
    public List<Entity> list(PageQuery query) {
        return this.page(query).getData();
    }

    @Override
    public StandardPage<Entity> page(PageQuery query) {
        final QueryWrapper<PO> queryWrapper = QueryConverter.convert(query);
        final Page<PO> page = new Page<>(query.getPageIndex(), query.getPageSize());
        super.page(page, queryWrapper);
        return StandardPage.converter(page, converter::toEntity);
    }

    @SuppressWarnings("unchecked")
    private void copyId(final List<Entity> entities, final List<PO> pos) {
        for (int i = 0; i < entities.size(); i++) {
            final Entity e = entities.get(i);
            final PO po = pos.get(i);
            if (e instanceof Identifiable && po instanceof Identifiable) {
                ((Identifiable) e).initId(((Identifiable) po).getId());
            }
        }
    }
}