package com.wallpaper.server.dto;

import lombok.Data;

@Data
public class WallpaperQuery {
    private Integer page = 1;

    private Integer pageSize = 10;

    private Integer type;

    private Long categoryId;

    private Long userId;

    private String keyword;
}