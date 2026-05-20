package com.wallpaper.server.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 50)
    private String alias;

    @Column(length = 100)
    private String iconPath;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(name = "like_count")
    private Integer likeCount = 0;

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "is_del")
    private Boolean isDel = false;
}