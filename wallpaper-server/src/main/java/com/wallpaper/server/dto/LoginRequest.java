package com.wallpaper.server.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class LoginRequest {
    private String username;

    private String password;

    private String wechatOpenId;

    private String nickname;

    private String avatar;
}
