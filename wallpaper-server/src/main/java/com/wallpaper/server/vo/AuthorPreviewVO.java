package com.wallpaper.server.vo;

import lombok.Data;

import java.util.List;

@Data
public class AuthorPreviewVO {
    private Long id;

    private String name;

    private String avatarUrl;

    private String bio;

    private Integer followerCount;

    private Integer workCount;

    private Integer totalLikes;

    private Integer totalCollects;

    private Boolean isCreator;

    private List<WallpaperVO> hotWallpapers;
}