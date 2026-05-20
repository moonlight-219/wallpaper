package com.wallpaper.server.util;

import com.wallpaper.server.entity.User;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class UserContext {

    private UserContext() {
    }

    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest();
    }

    public static User getCurrentUser() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        return (User) request.getAttribute("currentUser");
    }

    public static Long getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }

        Long userId = (Long) request.getAttribute("currentUserId");
        if (userId != null) {
            return userId;
        }

        User user = getCurrentUser();
        return user != null ? user.getId() : null;
    }

    public static boolean isAdmin() {
        User user = getCurrentUser();
        return user != null && "admin".equals(user.getRole());
    }

    public static boolean isLoggedIn() {
        return getCurrentUser() != null;
    }
}
