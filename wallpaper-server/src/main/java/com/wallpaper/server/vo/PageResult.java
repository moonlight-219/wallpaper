package com.wallpaper.server.vo;

import lombok.Data;
import org.springframework.data.domain.Page;
import java.util.List;

@Data
public class PageResult<T> {
    private List<T> list;
    private Long total;
    private Integer page;
    private Integer pageSize;

    public static <T> PageResult<T> of(Page<T> page, int pageNum, int pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setList(page.getContent());
        result.setTotal(page.getTotalElements());
        result.setPage(pageNum);
        result.setPageSize(pageSize);
        return result;
    }

    public static <T> PageResult<T> of(List<T> list, long total, int pageNum, int pageSize) {
        PageResult<T> result = new PageResult<>();
        result.setList(list);
        result.setTotal(total);
        result.setPage(pageNum);
        result.setPageSize(pageSize);
        return result;
    }
}
