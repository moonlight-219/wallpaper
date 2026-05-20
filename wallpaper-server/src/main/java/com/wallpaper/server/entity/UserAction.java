package com.wallpaper.server.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_actions")
public class UserAction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "work_id")
    private Long workId;

    @Column(name = "wallpaper_id")
    private Long wallpaperId;

    @Column(name = "category_id")
    private Long categoryId;

    @Column(nullable = false, length = 20)
    private String actionType;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "is_del")
    private Boolean isDel = false;
}
