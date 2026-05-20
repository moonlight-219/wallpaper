package com.wallpaper.server.common;

import lombok.Data;

@Data
public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.SUCCESS.getCode());
        result.setMessage(ErrorCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    public static <T> Result<T> success() {
        return success(null);
    }

    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.SYSTEM_ERROR.getCode());
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> error(ErrorCode errorCode) {
        Result<T> result = new Result<>();
        result.setCode(errorCode.getCode());
        result.setMessage(errorCode.getMessage());
        return result;
    }

    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> userNotFound() {
        return error(ErrorCode.USER_NOT_FOUND);
    }

    public static <T> Result<T> userAlreadyExists() {
        return error(ErrorCode.USER_ALREADY_EXISTS);
    }

    public static <T> Result<T> userLoginFailed() {
        return error(ErrorCode.USER_LOGIN_FAILED);
    }

    public static <T> Result<T> userNoPermission() {
        return error(ErrorCode.USER_NO_PERMISSION);
    }

    public static <T> Result<T> userNoPermission(String message) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.USER_NO_PERMISSION.getCode());
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> userNotCreator() {
        return error(ErrorCode.USER_NOT_CREATOR);
    }

    public static <T> Result<T> wallpaperNotFound() {
        return error(ErrorCode.WALLPAPER_NOT_FOUND);
    }

    public static <T> Result<T> wallpaperUploadFailed() {
        return error(ErrorCode.WALLPAPER_UPLOAD_FAILED);
    }

    public static <T> Result<T> authorNotFound() {
        return error(ErrorCode.AUTHOR_NOT_FOUND);
    }

    public static <T> Result<T> categoryNotFound() {
        return error(ErrorCode.CATEGORY_NOT_FOUND);
    }

    public static <T> Result<T> actionAlreadyExists() {
        return error(ErrorCode.ACTION_ALREADY_EXISTS);
    }

    public static <T> Result<T> paramError() {
        return error(ErrorCode.PARAM_ERROR);
    }

    public static <T> Result<T> paramError(String message) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.PARAM_ERROR.getCode());
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> paramMissing(String paramName) {
        Result<T> result = new Result<>();
        result.setCode(ErrorCode.PARAM_MISSING.getCode());
        result.setMessage("缺少必要参数: " + paramName);
        return result;
    }

    public static <T> Result<T> databaseError() {
        return error(ErrorCode.DATABASE_ERROR);
    }

    public static <T> Result<T> systemError() {
        return error(ErrorCode.SYSTEM_ERROR);
    }

    public static <T> Result<T> serverError() {
        return error(ErrorCode.SERVER_ERROR);
    }
}