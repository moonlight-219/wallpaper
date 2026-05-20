package com.wallpaper.server.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "authors")
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 200)
    private String avatar;

    @Column(length = 500)
    private String bio;

    @Column(name = "wallpaper_count")
    private Integer wallpaperCount = 0;

    @Column(name = "follower_count")
    private Integer followerCount = 0;

    @Column(name = "work_count")
    private Integer workCount = 0;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "is_del")
    private Boolean isDel = false;
}