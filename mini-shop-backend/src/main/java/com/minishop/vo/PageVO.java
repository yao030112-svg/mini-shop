package com.minishop.vo;

import lombok.Data;

import java.util.List;

/**
 * 分页结果 VO
 */
@Data
public class PageVO<T> {

    /** 数据列表 */
    private List<T> list;

    /** 总数 */
    private int total;

    /** 当前页码 */
    private int page;

    /** 每页大小 */
    private int pageSize;

    public PageVO() {
    }

    public PageVO(List<T> list, int total, int page, int pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }

    public static <T> PageVO<T> of(List<T> list, int total, int page, int pageSize) {
        return new PageVO<>(list, total, page, pageSize);
    }
}
