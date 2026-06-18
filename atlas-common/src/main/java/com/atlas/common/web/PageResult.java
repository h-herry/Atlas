package com.atlas.common.web;

import com.atlas.common.core.enums.ErrorCode;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页统一响应体 — 用于 MyBatis-Plus 分页查询 /
 * Paginated unified response body — used for MyBatis-Plus pagination queries
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    private long total;
    private long page;
    private long size;
    private List<T> records;

    public static <T> PageResult<T> of(long total, long page, long size, List<T> records) {
        return new PageResult<>(total, page, size, records);
    }

    public static <T> PageResult<T> of(Page<T> mybatisPage) {
        return new PageResult<>(mybatisPage.getTotal(), mybatisPage.getCurrent(),
                mybatisPage.getSize(), mybatisPage.getRecords());
    }

    public static <T> PageResult<T> of(IPage<T> iPage) {
        return new PageResult<>(iPage.getTotal(), iPage.getCurrent(),
                iPage.getSize(), iPage.getRecords());
    }

    public static <T> Result<PageResult<T>> ok(long total, long page, long size, List<T> records) {
        PageResult<T> pageResult = new PageResult<>(total, page, size, records);
        return Result.ok(pageResult);
    }
}
