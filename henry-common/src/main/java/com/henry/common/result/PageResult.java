package com.henry.common.result;

import lombok.Getter;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 分页结果
 */
@Getter
public class PageResult<T> implements Serializable {

    private final long total;
    private final List<T> records;
    private final long pageNum;
    private final long pageSize;

    private PageResult(long total, List<T> records, long pageNum, long pageSize) {
        this.total = total;
        this.records = records == null ? Collections.emptyList() : records;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
    }

    public static <T> PageResult<T> of(long total, List<T> records, long pageNum, long pageSize) {
        return new PageResult<>(total, records, pageNum, pageSize);
    }
}
