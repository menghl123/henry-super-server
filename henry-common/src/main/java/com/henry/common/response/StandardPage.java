package com.henry.common.response;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class StandardPage<T> {
    private List<T> data;
    private Long pageSize;
    private Long pageIndex;
    private Long total;

    public <R> StandardPage<R> converter(Function<List<T>, List<R>> mapping) {
        return StandardPage.<R>builder()
                .data(new ArrayList<>(mapping.apply(this.getData())))
                .pageIndex(this.getPageIndex())
                .pageSize(this.getPageSize())
                .total(this.getTotal())
                .build();
    }

    public static <R, T> StandardPage<R> converter(Page<T> page, Function<List<T>, List<R>> mapping) {
        return StandardPage.<R>builder()
                .data(mapping.apply(page.getRecords()))
                .pageIndex(page.getCurrent())
                .pageSize(page.getSize())
                .total(page.getTotal())
                .build();
    }

}
