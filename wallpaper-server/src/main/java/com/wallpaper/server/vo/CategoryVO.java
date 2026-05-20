package com.wallpaper.server.vo;

import lombok.Data;

@Data
public class CategoryVO {
    private Long id;
    private String name;
    private String alias;
    private String iconPath;
    private String coverUrl;
    private Integer likeCount;
    private Integer sortOrder;
    private Integer wallpaperCount; // 当前分类的壁纸数量
}

