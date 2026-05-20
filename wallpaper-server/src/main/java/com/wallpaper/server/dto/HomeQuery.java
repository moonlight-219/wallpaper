package com.wallpaper.server.dto;

import lombok.Data;

@Data
public class HomeQuery {
    private Integer page = 1;

    private Integer pageSize = 10;

    private Integer type;

    private Long categoryId;

    private String sortBy;

    private Integer limit;
}