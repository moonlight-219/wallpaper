package com.wallpaper.server.dto;

import lombok.Data;

@Data
public class UpdateUserInfoRequest {
    private String nickname;
    private String avatar;
    private String bio;
}
