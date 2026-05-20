package com.wallpaper.server.vo;

import lombok.Data;
import java.util.List;

@Data
public class CategoryDetailVO {
    private Long id;
    private String name;
    private String alias;
    private String iconPath;
    private String coverUrl;
    private Integer likeCount;
    private Integer sortOrder;
    private Long wallpaperCount;
    private List<WallpaperVO> wallpapers;
    private Long total;
    private Integer page;
    private Integer pageSize;
}
