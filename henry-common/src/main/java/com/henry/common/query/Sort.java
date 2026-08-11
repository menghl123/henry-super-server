package com.henry.common.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
public class Sort {
    public static final String ASC = "ASC";
    public static final String DESC = "DESC";

    private String name;

    @Builder.Default
    private String type = "asc";
}
