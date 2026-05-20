package com.wallpaper.server.dto;

import lombok.Data;

@Data
public class AuditRequest {
    private Integer status;
    private String reason;
}
