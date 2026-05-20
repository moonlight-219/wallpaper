package com.wallpaper.server.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "reports")
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "wallpaper_id", nullable = false)
    private Long wallpaperId;

    @Column(name = "report_type", nullable = false, length = 20)
    private String reportType;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "is_del")
    private Boolean isDel = false;
}