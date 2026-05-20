package com.wallpaper.server.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class CreateWorkRequest {
    private String title;

    @NotNull(message = "作品类型不能为空")
    private Integer type;

    private Long categoryId;

    private String description;

    @NotEmpty(message = "至少需要上传一张壁纸")
    @Valid
    private List<WallpaperInfo> wallpapers;

    @Data
    public static class WallpaperInfo {
        private String url;

        private String thumbnailUrl;

        private Integer imageWidth;

        private Integer imageHeight;

        private String fileFormat;

        private Long fileSize;
    }
}

