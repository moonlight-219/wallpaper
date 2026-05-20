package com.wallpaper.server.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 20)
    private String role;

    @Column(name = "is_creator")
    private Boolean isCreator = true;

    @Column(name = "wechat_open_id", length = 100)
    private String wechatOpenId;

    @Column(length = 50)
    private String username;

    @Column(length = 100)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(length = 200)
    private String avatar;

    @Column(name = "follower_count")
    private Integer followerCount = 0;

    @Column(name = "work_count")
    private Integer workCount = 0;

    @Column(length = 500)
    private String bio;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(name = "create_time")
    private LocalDateTime createTime;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "is_del")
    private Boolean isDel = false;
}