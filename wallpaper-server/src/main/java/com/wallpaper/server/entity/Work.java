package com.wallpaper.server.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "works")
public class Work {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Integer version;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 500)
    private String coverUrl;

    @Column(name = "image_width")
    private Integer imageWidth;

    @Column(name = "image_height")
    private Integer imageHeight;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "file_format", length = 10)
    private String fileFormat;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(nullable = false)
    private Integer type;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(length = 1000)
    private String description;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wallpaper_count", nullable = false)
    private Integer wallpaperCount = 0;

    @Column(name = "like_count", nullable = false)
    private Integer likeCount = 0;

    @Column(name = "collect_count", nullable = false)
    private Integer collectCount = 0;

    @Column(name = "view_count", nullable = false)
    private Integer viewCount = 0;

    @Column(name = "download_count", nullable = false)
    private Integer downloadCount = 0;

    @Column(nullable = false)
    private Integer status = 0;

    @Column(name = "reject_reason", length = 500)
    private String rejectReason;

    @Column(name = "create_time", nullable = false)
    private LocalDateTime createTime;

    @Column(name = "publish_time")
    private LocalDateTime publishTime;

    @Column(name = "update_time", nullable = false)
    private LocalDateTime updateTime;

    @Column(name = "is_del", nullable = false)
    private Boolean isDel = false;
}
