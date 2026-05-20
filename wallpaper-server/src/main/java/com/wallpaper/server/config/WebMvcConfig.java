package com.wallpaper.server.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private AuthInterceptor authInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        // 认证相关
                        "/user/login",
                        "/user/register",
                        // 上传（登录时上传头像）
                        "/upload",
                        // 壁纸浏览（公开）
                        "/wallpaper/list",
                        "/wallpaper/hot",
                        "/wallpaper/popular",
                        "/wallpaper/most-downloaded",
                        "/wallpaper/most-collected",
                        "/wallpaper/search",
                        "/wallpaper/detail/**",
                        "/wallpaper/{id}",
                        // 首页
                        "/home/**",
                        // 广场
                        "/square/**",
                        // 分类浏览（GET 公开，POST/PUT/DELETE 需要认证，由拦截器处理）
                        "/category",
                        "/category/list",
                        "/category/hot",
                        "/category/*/detail",
                        // 作品集浏览（仅 GET 公开，写操作需要认证，由 AuthInterceptor 处理）
                        // 注意：不要排除 /works，让拦截器处理认证逻辑
                        // 作者浏览
                        "/author/list",
                        "/author/**",
                        // 健康检查
                        "/health/**",
                        "/error");
    }
}
