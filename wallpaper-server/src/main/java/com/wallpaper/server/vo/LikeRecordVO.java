package com.wallpaper.server.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LikeRecordVO {
    private Long id;

    private Long wallpaperId;

    private String title;

    private String url;

    private String thumbnailUrl;

    private Integer imageWidth;

    private Integer imageHeight;

    private String fileFormat;

    private Long fileSize;

    private Integer type;

    private Long categoryId;

    private String category;

    private Integer likeCount;

    private Integer collectCount;

    private Integer downloadCount;

    private Boolean isLiked;

    private Boolean isCollected;

    private AuthorInfo author;

    private LocalDateTime likeTime;

    @Data
    public static class AuthorInfo {
        private Long id;
        private String name;
        private String avatarUrl;
    }
}
