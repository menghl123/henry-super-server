package com.henry.common.entity;

import lombok.Data;

/**
 * 分页查询入参
 */
@Data
public class PageQuery {

    private long pageNum = 1;

    private long pageSize = 10;
}
