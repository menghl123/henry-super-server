package com.henry.common.query;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.util.List;

@Data
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class PageQuery {
    @Min(value = 1, message = "pageIndex.is.too.small")
    @Builder.Default
    private Long pageIndex = 1L;

    @Min(value = 1, message = "pageSize.is.too.small")
    @Max(value = 500, message = "pageSize.is.too.large")
    @Builder.Default
    private Long pageSize = 10L;

    private List<Sort> sorts;

}
