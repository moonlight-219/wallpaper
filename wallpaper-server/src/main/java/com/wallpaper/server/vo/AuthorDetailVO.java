package com.wallpaper.server.vo;

import lombok.Data;

import java.util.List;

@Data
public class AuthorDetailVO {
    private Long id;

    private String name;

    private String avatarUrl;

    private String bio;

    private Integer wallpaperCount;

    private Integer followerCount;

    private Integer workCount;

    private Integer totalLikes;

    private Integer totalCollects;

    private List<WallpaperVO> wallpapers;
}