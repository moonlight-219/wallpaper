package com.wallpaper.server.vo;

import lombok.Data;
import java.util.List;

@Data
public class DashboardStatsVO {
    private Long wallpapers;
    private Long authors;
    private Long categories;
    private Double wallpaperGrowth;
    private Double authorGrowth;
    private Double categoryGrowth;
}
