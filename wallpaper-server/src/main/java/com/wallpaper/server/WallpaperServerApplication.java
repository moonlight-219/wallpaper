package com.wallpaper.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WallpaperServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WallpaperServerApplication.class, args);
    }
}