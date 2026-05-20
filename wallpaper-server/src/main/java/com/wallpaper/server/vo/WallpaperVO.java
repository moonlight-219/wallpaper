package com.wallpaper.server.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WallpaperVO {
    private Long id;

    private String title;

    private String url;

    private String thumbnailUrl;

    private Integer imageWidth;

    private Integer imageHeight;

    private String fileFormat;

    private Long fileSize;

    private Integer type;

    private String category;

    private String description;

    private AuthorInfo author;

    private Integer viewCount;

    private Integer downloadCount;

    private Integer likeCount;

    private Integer collectCount;

    private Boolean isLiked;

    private Boolean isCollected;

    private LocalDateTime createTime;

    @Data
    public static class AuthorInfo {
        private Long id;

        private String name;

        private String avatarUrl;
    }
}