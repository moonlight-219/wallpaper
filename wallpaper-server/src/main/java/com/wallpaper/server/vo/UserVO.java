package com.wallpaper.server.vo;

import lombok.Data;

@Data
public class UserVO {
    private Long id;

    private String role;

    private Boolean isCreator;

    private String wechatOpenId;

    private String username;

    private String nickname;

    private String avatar;

    private Integer followerCount;

    private Integer workCount;

    private String bio;

    private String phone;

    private String email;

    private Integer totalLikes;

    private Integer totalCollects;
}
