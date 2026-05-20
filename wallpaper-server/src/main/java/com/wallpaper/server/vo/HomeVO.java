package com.wallpaper.server.vo;

import com.wallpaper.server.entity.Wallpaper;
import lombok.Data;

import java.util.List;

@Data
public class HomeVO {
    private List<CategoryVO> carousel;

    private List<AuthorVO> hotAuthors;

    private List<WallpaperVO> hotAvatars;

    private List<WallpaperVO> hotPcs;
}