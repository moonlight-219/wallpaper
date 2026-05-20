# 错误码说明文档

## 错误码分类

### 1. 成功 (200)
- **200**: 操作成功

### 2. 用户相关错误 (1000-1099)
- **1001**: 用户不存在
- **1002**: 用户已存在
- **1003**: 用户名或密码错误
- **1004**: 微信未绑定
- **1005**: 用户无权限
- **1006**: 用户暂无创作权限

### 3. 壁纸相关错误 (2000-2099)
- **2001**: 壁纸不存在
- **2002**: 壁纸上传失败
- **2003**: 壁纸删除失败
- **2004**: 壁纸状态无效

### 4. 创作者相关错误 (3000-3099)
- **3001**: 创作者不存在

### 5. 分类相关错误 (4000-4099)
- **4001**: 分类不存在
- **4002**: 分类已存在

### 6. 专辑相关错误 (5000-5099)
- **5001**: 专辑不存在
- **5002**: 专辑创建失败
- **5003**: 专辑删除失败

### 7. 用户行为相关错误 (6000-6099)
- **6001**: 已操作过
- **6002**: 操作记录不存在

### 8. 举报相关错误 (7000-7099)
- **7001**: 已举报过
- **7002**: 举报失败

### 9. 参数相关错误 (8000-8099)
- **8001**: 参数错误
- **8002**: 缺少必要参数
- **8003**: 参数格式错误

### 10. 系统相关错误 (9000-9099)
- **9001**: 数据库操作失败
- **9002**: 网络错误
- **9003**: 系统错误
- **9004**: 服务器内部错误

### 11. 文件相关错误 (10000-10099)
- **10001**: 文件上传失败
- **10002**: 文件类型错误
- **10003**: 文件大小超出限制
- **10004**: 文件不存在

## 使用方式

### 1. 在Controller中使用

```java
// 使用预定义的错误方法
return Result.userNotFound();
return Result.userLoginFailed();
return Result.userNoPermission();
return Result.userNotCreator();
return Result.wallpaperNotFound();
return Result.authorNotFound();
return Result.categoryNotFound();
return Result.albumNotFound();
return Result.actionAlreadyExists();
return Result.paramError();
return Result.paramMissing("userId");
return Result.databaseError();
return Result.systemError();
return Result.serverError();

// 使用ErrorCode枚举
return Result.error(ErrorCode.USER_NOT_FOUND);
return Result.error(ErrorCode.USER_ALREADY_EXISTS);
return Result.error(ErrorCode.WALLPAPER_NOT_FOUND);

// 自定义错误码和消息
return Result.error(1001, "自定义错误消息");
```

### 2. 在Service中抛出异常

```java
// 抛出业务异常
throw new BusinessException(ErrorCode.USER_NOT_FOUND);
throw new BusinessException(ErrorCode.WALLPAPER_NOT_FOUND, "壁纸ID: " + id);

// 抛出通用异常
throw new BusinessException("自定义错误消息");
```

### 3. 全局异常处理

系统已配置全局异常处理器，会自动捕获并处理以下异常：

- **Exception**: 系统异常，返回500错误
- **BusinessException**: 业务异常，返回对应的错误码
- **MethodArgumentNotValidException**: 参数校验失败，返回400错误
- **BindException**: 参数绑定失败，返回400错误
- **IllegalArgumentException**: 非法参数，返回400错误
- **NullPointerException**: 空指针异常，返回500错误

## 错误响应格式

所有错误响应都遵循统一格式：

```json
{
  "code": 1001,
  "message": "用户不存在",
  "data": null
}
```

## 最佳实践

1. **使用预定义的错误方法**：优先使用Result类中预定义的错误方法，如`Result.userNotFound()`

2. **使用ErrorCode枚举**：对于预定义的错误码，使用`ErrorCode`枚举而不是硬编码数字

3. **自定义错误消息**：在需要更详细错误信息时，可以自定义错误消息

4. **在Service层抛出异常**：业务逻辑中的错误应该在Service层抛出异常，由全局异常处理器统一处理

5. **记录日志**：全局异常处理器会自动记录异常日志，便于问题排查

## 示例

### Controller示例

```java
@GetMapping("/{id}")
public Result<User> getUserById(@PathVariable Long id) {
    User user = userService.findById(id);
    if (user == null) {
        return Result.userNotFound();
    }
    return Result.success(user);
}

@PostMapping("/upload")
public Result<Void> upload(@RequestBody UploadWallpaperRequest request) {
    User user = userService.findById(request.getAuthorId());
    if (user == null) {
        return Result.userNotFound();
    }
    if (!user.getIsCreator()) {
        return Result.userNotCreator();
    }
    wallpaperService.upload(request);
    return Result.success();
}
```

### Service示例

```java
public User findById(Long id) {
    User user = userRepository.findById(id).orElse(null);
    if (user == null) {
        throw new BusinessException(ErrorCode.USER_NOT_FOUND);
    }
    return user;
}

public void upload(UploadWallpaperRequest request) {
    User user = findById(request.getAuthorId());
    if (!user.getIsCreator()) {
        throw new BusinessException(ErrorCode.USER_NOT_CREATOR);
    }
    // 上传逻辑...
}
```

## 错误码扩展

如需添加新的错误码，请按以下步骤操作：

1. 在`ErrorCode`枚举中添加新的错误码
2. 在`Result`类中添加对应的便捷方法（可选）
3. 更新本文档

```java
// 在ErrorCode枚举中添加
NEW_ERROR(1100, "新错误描述"),

// 在Result类中添加便捷方法（可选）
public static <T> Result<T> newError() {
    return error(ErrorCode.NEW_ERROR);
}
```