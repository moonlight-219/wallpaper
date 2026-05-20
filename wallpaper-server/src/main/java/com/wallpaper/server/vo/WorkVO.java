package com.wallpaper.server.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class WorkVO {
    private Long id;

    private String title;

    private String coverUrl;

    private String thumbnailUrl;

    private Integer imageWidth;

    private Integer imageHeight;

    private String fileFormat;

    private Long fileSize;

    private Integer type;

    private Integer imageCount;

    private AuthorInfo author;

    private List<WallpaperVO> wallpapers;

    private Integer likes;

    private Integer collects;

    private Integer views;

    private LocalDateTime publishTime;

    private Integer status;

    private Boolean isLiked;

    private Boolean isCollected;

    @Data
    public static class AuthorInfo {
        private Long id;

        private String name;

        private String avatar;
    }
}