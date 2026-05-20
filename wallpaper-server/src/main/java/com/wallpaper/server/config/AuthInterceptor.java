package com.wallpaper.server.config;

import com.wallpaper.server.entity.User;
import com.wallpaper.server.repository.UserRepository;
import com.wallpaper.server.common.Result;
import com.wallpaper.server.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 白名单：不需要登录即可访问的公开接口
     * 使用Ant风格路径匹配
     * 注意：context-path 已设置为 /api，所以 requestURI 不包含 /api 前缀
     */
    private static final List<String> EXCLUDE_PATHS = Arrays.asList(
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
            // 首页
            "/home/**",
            // 广场
            "/square/**",
            // 分类浏览（仅GET）
            "/category/list",
            "/category/hot",
            "/category",
            "/category/{id}",
            "/category/*/detail",
            // 作者浏览
            "/author/list",
            "/author/*",
            // 健康检查
            "/health/**",
            // 错误页
            "/error");

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();
        String authorizationHeader = request.getHeader("Authorization");
        log.info("请求路径: {}, 方法: {}, Authorization Header: {}", requestURI, method,
                authorizationHeader != null
                        ? authorizationHeader.substring(0, Math.min(30, authorizationHeader.length())) + "..."
                        : "null");

        String pathWithoutContext = requestURI;
        if (requestURI.startsWith("/api")) {
            pathWithoutContext = requestURI.substring(4);
        }

        if (pathWithoutContext.startsWith("/works") || pathWithoutContext.startsWith("/category")) {
            if ("GET".equals(method)) {
                log.info("公开接口放行: {} {}", method, requestURI);
                return true;
            } else {
                log.info("写操作需要认证: {} {}", method, requestURI);
            }
        }

        for (String excludePath : EXCLUDE_PATHS) {
            if (pathMatcher.match(excludePath, pathWithoutContext)) {
                log.info("白名单放行: {}", requestURI);
                return true;
            }
        }

        String token = request.getHeader("Authorization");
        if (token == null || token.isEmpty()) {
            token = request.getHeader("X-User-Id");
            if (token != null && !token.isEmpty()) {
                return handleLegacyAuth(token, request, response);
            }
            log.warn("缺少认证头 Authorization, 请求路径: {}", requestURI);
            sendErrorResponse(response, "未登录或登录已过期");
            return false;
        }

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        log.info("开始验证JWT Token, 请求路径: {}, Token前20字符: {}", requestURI,
                token.length() > 20 ? token.substring(0, 20) : token);

        if (!jwtUtil.validateToken(token)) {
            log.warn("JWT Token无效或已过期, 请求路径: {}", requestURI);
            sendErrorResponse(response, "未登录或登录已过期");
            return false;
        }

        log.info("JWT Token验证通过, 请求路径: {}", requestURI);

        Long userId = jwtUtil.getUserIdFromToken(token);
        if (userId == null) {
            log.warn("无法从Token中获取用户ID, 请求路径: {}", requestURI);
            sendErrorResponse(response, "无效的Token");
            return false;
        }

        log.info("从Token中获取到用户ID: {}, 请求路径: {}", userId, requestURI);

        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            log.warn("用户不存在, userId: {}", userId);
            sendErrorResponse(response, "用户不存在");
            return false;
        }

        request.setAttribute("currentUser", user);
        request.setAttribute("currentUserId", userId);
        return true;
    }

    private boolean handleLegacyAuth(String userIdStr, HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        try {
            Long userId = Long.parseLong(userIdStr);
            User user = userRepository.findById(userId).orElse(null);

            if (user == null) {
                log.warn("用户不存在, userId: {}", userId);
                sendErrorResponse(response, "用户不存在");
                return false;
            }

            request.setAttribute("currentUser", user);
            request.setAttribute("currentUserId", userId);
            return true;
        } catch (NumberFormatException e) {
            log.warn("无效的用户ID格式: {}", userIdStr);
            sendErrorResponse(response, "无效的用户ID");
            return false;
        }
    }

    private void sendErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        Result<Void> result = Result.error(401, message);
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}