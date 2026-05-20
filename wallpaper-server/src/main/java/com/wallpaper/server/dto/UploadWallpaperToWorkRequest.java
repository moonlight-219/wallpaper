package com.wallpaper.server.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UploadWallpaperToWorkRequest {
    @NotNull(message = "作品集ID不能为空")
    private Long workId;

    @NotBlank(message = "图片URL不能为空")
    private String url;

    private String thumbnailUrl;

    private Integer imageWidth;

    private Integer imageHeight;

    private String fileFormat;

    private Long fileSize;
}
