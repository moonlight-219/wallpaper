package com.wallpaper.server.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DownloadRecordVO {
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

    private LocalDateTime downloadTime;
}