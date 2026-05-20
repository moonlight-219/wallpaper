package com.wallpaper.server.vo;

import lombok.Data;

import java.util.List;

@Data
public class WallpaperDetailVO {
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

    private Integer viewCount;

    private Integer downloadCount;

    private Integer likeCount;

    private Integer collectCount;

    private Boolean isLiked;

    private Boolean isCollected;

    private AuthorInfo author;

    @Data
    public static class AuthorInfo {
        private Long id;

        private String name;

        private String avatarUrl;

        private String bio;

        private Integer followerCount;

        private Integer workCount;
    }
}