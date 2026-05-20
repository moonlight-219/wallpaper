# 阿里云OSS图片存储使用说明

## 功能概述

本系统集成了阿里云OSS对象存储服务，用于存储壁纸、头像、专辑封面等图片文件，并自动生成缩略图。

## 核心功能

### 1. 自动生成缩略图
- 上传原图时自动生成300x300的缩略图
- 缩略图质量为原图的80%
- 缩略图存储在`thumbnails`子目录中

### 2. 图片信息提取
- 自动提取图片尺寸（宽度、高度）
- 自动获取图片格式（JPG、PNG等）
- 自动获取文件大小

### 3. 文件组织结构
```
bucket/
├── wallpapers/              # 壁纸原图
│   ├── {uuid}.jpg
│   └── thumbnails/         # 壁纸缩略图
│       └── {uuid}.jpg
├── avatars/               # 用户头像
│   └── {uuid}.jpg
└── album-covers/          # 专辑封面
    └── {uuid}.jpg
```

## 配置步骤

### 1. 获取阿里云OSS凭证
1. 登录阿里云控制台
2. 进入"对象存储OSS"服务
3. 创建Bucket（存储空间）
4. 获取以下信息：
   - Endpoint（地域节点）
   - AccessKey ID
   - AccessKey Secret
   - Bucket名称

### 2. 配置application.yml
```yaml
aliyun:
  oss:
    endpoint: oss-cn-hangzhou.aliyuncs.com        # 你的地域节点
    access-key-id: your-access-key-id             # 你的AccessKey ID
    access-key-secret: your-access-key-secret       # 你的AccessKey Secret
    bucket-name: your-bucket-name                 # 你的Bucket名称
    url-prefix: https://your-bucket-name.oss-cn-hangzhou.aliyuncs.com  # 访问域名

spring:
  servlet:
    multipart:
      max-file-size: 10MB    # 单文件最大大小
      max-request-size: 10MB  # 请求最大大小
```

### 3. 配置Bucket权限
1. 进入Bucket设置
2. 设置"读写权限"为"公共读"
3. 设置"跨域设置"（CORS）：
   - 来源：`*`
   - 允许Methods：`GET`, `POST`, `PUT`, `DELETE`, `HEAD`
   - 允许Headers：`*`
   - 暴露Headers：`ETag`, `x-oss-request-id`

## API接口

### 1. 上传壁纸（含缩略图）
**接口**: `POST /api/upload/image`
**参数**:
- `file`: 图片文件（MultipartFile）

**返回示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "https://bucket.oss-cn-hangzhou.aliyuncs.com/wallpapers/abc123.jpg",
    "thumbnailUrl": "https://bucket.oss-cn-hangzhou.aliyuncs.com/wallpapers/thumbnails/abc123.jpg",
    "width": 1920,
    "height": 1080,
    "format": "JPG",
    "size": 2048000
  }
}
```

### 2. 上传头像
**接口**: `POST /api/upload/avatar`
**参数**:
- `file`: 图片文件（MultipartFile）

**返回示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "https://bucket.oss-cn-hangzhou.aliyuncs.com/avatars/abc123.jpg"
  }
}
```

### 3. 上传专辑封面
**接口**: `POST /api/upload/album-cover`
**参数**:
- `file`: 图片文件（MultipartFile）

**返回示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "url": "https://bucket.oss-cn-hangzhou.aliyuncs.com/album-covers/abc123.jpg"
  }
}
```

## 使用流程

### 上传壁纸完整流程

1. **前端上传图片**
   ```javascript
   const formData = new FormData();
   formData.append('file', imageFile);

   const response = await fetch('/api/upload/image', {
     method: 'POST',
     body: formData
   });

   const result = await response.json();
   const { url, thumbnailUrl, width, height, format, size } = result.data;
   ```

2. **提交壁纸信息**
   ```javascript
   const wallpaperData = {
     url: url,                    // 原图URL
     thumbnailUrl: thumbnailUrl,    // 缩略图URL
     imageWidth: width,            // 图片宽度
     imageHeight: height,          // 图片高度
     fileFormat: format,           // 图片格式
     fileSize: size,              // 文件大小
     type: 'phone',              // 壁纸类型
     title: '壁纸标题',          // 壁纸标题
     authorId: 1,                // 创作者ID
     categoryId: 1,               // 分类ID
     description: '描述'          // 描述
   };

   await fetch('/api/wallpaper/upload', {
     method: 'POST',
     headers: { 'Content-Type': 'application/json' },
     body: JSON.stringify(wallpaperData)
   });
   ```

## 数据库字段说明

Wallpaper表中的图片相关字段：

| 字段名 | 类型 | 说明 | 示例 |
|--------|------|------|------|
| url | VARCHAR(500) | 原图URL（高清原图） | https://bucket.oss.../wallpapers/abc.jpg |
| thumbnail_url | VARCHAR(500) | 缩略图URL（列表展示用） | https://bucket.oss.../thumbnails/abc.jpg |
| image_width | INT | 图片宽度（像素） | 1920 |
| image_height | INT | 图片高度（像素） | 1080 |
| file_format | VARCHAR(10) | 图片格式 | JPG |
| file_size | BIGINT | 文件大小（字节） | 2048000 |

## 优势

1. **自动缩略图生成**：无需前端处理，后端自动生成
2. **CDN加速**：阿里云OSS自带CDN，访问速度快
3. **成本优化**：缩略图减少带宽消耗
4. **高可用性**：阿里云OSS提供99.9999999999%的数据可靠性
5. **扩展性强**：支持海量文件存储

## 注意事项

1. **AccessKey安全**：不要将AccessKey硬编码在代码中，建议使用环境变量
2. **文件大小限制**：默认限制10MB，可根据需要调整
3. **图片格式**：支持JPG、PNG、GIF等常见格式
4. **缩略图尺寸**：默认300x300，可在OssService中调整
5. **Bucket权限**：确保Bucket设置为"公共读"，否则无法访问图片

## 成本估算

以阿里云OSS标准存储为例（华东1区域）：
- 存储费用：¥0.12/GB/月
- 流量费用：¥0.50/GB（外网下行流量）
- 请求费用：¥0.01/万次

示例：1000张壁纸，每张2MB
- 存储费用：2GB × ¥0.12 = ¥0.24/月
- 流量费用：假设每天1000次下载，每次2MB = 60GB/月 × ¥0.50 = ¥30/月
- 请求费用：3万次/月 × ¥0.01/万次 = ¥0.03/月
- **总计约¥30.27/月**
