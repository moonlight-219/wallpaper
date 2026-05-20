package com.wallpaper.server.vo;

import lombok.Data;

@Data
public class AuthorVO {
    private Long id;

    private String name;

    private String avatarUrl;

    private String bio;

    private Integer followerCount;

    private Integer workCount;
}