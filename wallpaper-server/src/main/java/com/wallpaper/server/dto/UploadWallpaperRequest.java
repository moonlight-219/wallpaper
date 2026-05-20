package com.wallpaper.server.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UploadWallpaperRequest {
    @NotBlank(message = "壁纸地址不能为空")
    private String url;

    private String thumbnailUrl;

    private Integer imageWidth;

    private Integer imageHeight;

    private String fileFormat;

    private Long fileSize;

    @NotNull(message = "壁纸类型不能为空")
    private Integer type;

    @NotBlank(message = "壁纸标题不能为空")
    private String title;

    @NotNull(message = "用户ID不能为空")
    private Long userId;

    private Long categoryId;

    private String description;
}