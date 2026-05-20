package com.wallpaper.server.common;

public enum ErrorCode {
    SUCCESS(200, "成功"),

    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "无权限"),
    NOT_FOUND(404, "资源不存在"),

    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户已存在"),
    USER_NOT_CREATOR(1003, "用户不是创作者"),
    USER_PASSWORD_ERROR(1004, "密码错误"),
    USER_TOKEN_INVALID(1005, "Token无效"),
    USER_LOGIN_FAILED(1006, "登录失败"),
    USER_NO_PERMISSION(1007, "无权限"),

    AUTHOR_NOT_FOUND(1101, "作者不存在"),

    CATEGORY_NOT_FOUND(2001, "分类不存在"),
    CATEGORY_ALREADY_EXISTS(2002, "分类已存在"),

    WALLPAPER_NOT_FOUND(3001, "壁纸不存在"),
    WALLPAPER_UPLOAD_FAILED(3002, "壁纸上传失败"),

    WORK_NOT_FOUND(8001, "作品集不存在"),
    WORK_CREATE_FAILED(8002, "作品集创建失败"),
    WORK_NO_PERMISSION(8003, "无权操作此作品集"),

    REPORT_NOT_FOUND(4001, "举报不存在"),
    REPORT_ALREADY_EXISTS(4002, "举报已存在"),

    ACTION_ALREADY_EXISTS(4003, "操作已存在"),

    PARAM_ERROR(4000, "参数错误"),
    PARAM_MISSING(4001, "缺少必要参数"),

    DATABASE_ERROR(5001, "数据库错误"),
    SYSTEM_ERROR(5002, "系统错误"),
    SERVER_ERROR(5003, "服务器错误"),
    INTERNAL_ERROR(5000, "服务器内部错误");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
