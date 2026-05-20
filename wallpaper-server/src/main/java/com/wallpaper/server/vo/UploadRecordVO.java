package com.wallpaper.server.vo;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class UploadRecordVO {
    private Integer totalCount;

    private Integer approvedCount;

    private Integer pendingCount;

    private Integer rejectedCount;

    private List<UploadWallpaperVO> wallpapers;

    private Integer page;

    private Integer pageSize;

    private Long total;

    @Data
    public static class UploadWallpaperVO {
        private Long id;

        private String url;

        private String thumbnailUrl;

        private Integer imageWidth;

        private Integer imageHeight;

        private String fileFormat;

        private Long fileSize;

        private Integer type;

        private Long categoryId;

        private String category;

        private LocalDateTime uploadTime;

        private Integer status;

        private String rejectReason;
    }
}