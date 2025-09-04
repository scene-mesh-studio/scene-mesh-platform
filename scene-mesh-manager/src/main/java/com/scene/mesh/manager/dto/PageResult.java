package com.scene.mesh.manager.dto;

import lombok.Data;

import java.util.List;

@Data
public class PageResult<T> {
    private List<T> data;
    private long total;
    private int page;
    private int size;
    private int totalPages;

    public PageResult(List<T> data, long total, int page, int size) {
        this.data = data;
        this.total = total;
        this.page = page;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) total / size);
    }

}

