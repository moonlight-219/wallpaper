package com.wallpaper.server.model;

import lombok.Data;

@Data
public class ImageInfo {
    private String originalUrl;
    private String thumbnailUrl;
    private String thumbnailSmallUrl;
    private String thumbnailMediumUrl;
    private String thumbnailLargeUrl;
    private Integer width;
    private Integer height;
    private String format;
    private Long size;
}
