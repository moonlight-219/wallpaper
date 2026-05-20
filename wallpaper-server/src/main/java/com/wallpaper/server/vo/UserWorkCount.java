package com.wallpaper.server.vo;

import lombok.Data;

@Data
public class UserWorkCount {
    private Long userId;
    private Long count;

    public UserWorkCount(Long userId, Long count) {
        this.userId = userId;
        this.count = count;
    }
}
