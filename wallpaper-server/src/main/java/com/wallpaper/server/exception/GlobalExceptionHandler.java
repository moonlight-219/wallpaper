package com.wallpaper.server.exception;

import com.wallpaper.server.common.ErrorCode;
import com.wallpaper.server.common.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleException(Exception e) {
        logger.error("系统异常", e);
        return Result.serverError();
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        logger.warn("数据完整性约束违反: {}", e.getMessage());
        String message = e.getMessage();

        if (message != null) {
            if (message.contains("uk_username")) {
                return Result.error(ErrorCode.USER_ALREADY_EXISTS.getCode(), "用户名已存在");
            } else if (message.contains("uk_wechat_open_id")) {
                return Result.error(ErrorCode.USER_ALREADY_EXISTS.getCode(), "微信openId已存在");
            } else if (message.contains("uk_name") && message.contains("authors")) {
                return Result.error(400, "创作者名称已存在");
            } else if (message.contains("uk_name") && message.contains("categories")) {
                return Result.error(400, "分类名称已存在");
            } else if (message.contains("Duplicate entry")) {
                return Result.error(400, "数据已存在，请勿重复添加");
            }
        }

        return Result.databaseError();
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBusinessException(BusinessException e) {
        logger.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getErrorCode());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        logger.warn("参数校验失败: {}", errorMessage);
        return Result.paramError(errorMessage);
    }

    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleBindException(BindException e) {
        String errorMessage = e.getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        logger.warn("参数绑定失败: {}", errorMessage);
        return Result.paramError(errorMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        logger.warn("非法参数: {}", e.getMessage());
        return Result.paramError(e.getMessage());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String paramName = e.getParameterName();
        String paramType = e.getParameterType();
        logger.warn("缺少必填参数: {} (类型: {})", paramName, paramType);
        return Result.paramMissing(paramName);
    }

    @ExceptionHandler(MissingPathVariableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMissingPathVariableException(MissingPathVariableException e) {
        String paramName = e.getVariableName();
        logger.warn("缺少必填路径参数: {}", paramName);
        return Result.paramMissing(paramName);
    }

    /**
     * 路径变量 / 请求参数无法转为目标类型（如 URL 中出现 "null"、非数字字符串）
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Void> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException e) {
        String name = e.getName();
        Object value = e.getValue();
        Class<?> required = e.getRequiredType();
        logger.warn("参数类型不匹配: {} = {} (期望: {})", name, value,
                required != null ? required.getSimpleName() : "unknown");
        return Result.paramError("参数格式错误，请检查 ID 等字段是否为有效数字");
    }

    @ExceptionHandler(NullPointerException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Void> handleNullPointerException(NullPointerException e) {
        logger.error("空指针异常", e);
        return Result.databaseError();
    }
}