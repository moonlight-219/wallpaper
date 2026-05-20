package com.wallpaper.server.vo;

import lombok.Data;

@Data
public class UserStatsVO {
    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    private String role;

    private Boolean isCreator;

    private Integer followerCount;

    private Integer workCount;

    private Integer totalLikes;

    private Integer totalCollects;

    private Integer myLikeCount;

    private Integer myCollectCount;

    private String bio;
}