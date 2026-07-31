package com.henry.common.ddd.domain;

public interface Identifiable<I> {
    I getId();

    void initId(final I id);
}
