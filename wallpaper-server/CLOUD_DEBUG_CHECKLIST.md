# 🚨 云托管 INVALID_HOST 错误 - 排查清单

## 当前状态
- **域名**: `springboot-7903-244472-7-14207603295.sh.run.tcloudbase.com`
- **错误**: 404 INVALID_HOST
- **原因**: 服务未正常启动 或 配置不匹配

---

## ✅ 已完成的修复（本地代码）

### 1. 端口统一为 9999
```yaml
# application-prod.yml
server:
  port: 9999  # ✅ 与本地一致
```

### 2. 前端 baseURL 已更新
```javascript
// request.js (小程序端)
return 'https://springboot-7903-244472-7-14207603295.sh.run.tcloudbase.com/api'
```

---

## 🔧 你需要在云托管控制台执行的操作

### ⚠️ 第一步：检查实例配置（最关键！）

**位置**: 云托管控制台 → 你的服务 → 「基本信息」或「实例配置」

| 配置项 | 正确值 | 常见错误 |
|--------|--------|----------|
| **最小副本数** | `1` | ❌ `0`（会自动关机！） |
| **最大副本数** | `5` | - |
| **监听端口** | `9999` | ❌ `8080`（与代码不一致） |
| **启动命令** | 见下方 | ❌ 路径/端口错误 |

**启动命令**（必须完全一致）：
```bash
java -Xms512m -Xmx1024m -jar /app/wallpaper-server-1.0.0.jar --server.port=9999 --spring.profiles.active=prod
```

---

### ⚠️ 第二步：配置环境变量（数据库连接）

**位置**: 服务设置 → 「环境变量」

**开启 JSON 开关**，填入：

```json
{
  "MYSQL_HOST": "10.26.106.211",
  "MYSQL_PORT": "3306",
  "MYSQL_DATABASE": "wallpaper",
  "MYSQL_USERNAME": "root",
  "MYSQL_PASSWORD": "你的云托管MySQL密码",

  "REDIS_HOST": "你的Redis地址",
  "REDIS_PORT": "6379",

  "JWT_SECRET": "随机生成一个长字符串（至少256位）",

  "OSS_ACCESS_KEY_ID": "你的阿里云AK",
  "OSS_ACCESS_KEY_SECRET": "你的阿里云SK",

  "WECHAT_APP_ID": "你的小程序AppID",
  "WECHAT_APP_SECRET": "你的小程序AppSecret"
}
```

⚠️ **关键**: MYSQL_HOST 必须是云托管的内网地址，不是 localhost！

---

### ⚠️ 第三步：重新打包并上传

#### 本地执行：
```bash
cd d:\my\project\wallpaper\wallpaper-server

# 清理并打包（跳过测试）
mvn clean package -DskipTests

# 验证 JAR 文件
dir target\wallpaper-server-1.0.0.jar
```

#### 上传到云托管：
1. 控制台 → 「版本管理」→「新建版本」
2. 选择「上传代码包」或「上传 JAR 包」
3. 上传 `target/wallpaper-server-1.0.0.jar`
4. 确认启动命令和端口设置
5. 点击「提交部署」

---

## 🔍 第四步：验证服务状态

### 4.1 检查运行日志
**位置**: 控制板 → 「运行日志」

**查找关键词**：
- ✅ `Started WallpaperServerApplication in X seconds` → 启动成功
- ❌ `Connection refused: localhost:3306` → 数据库地址错误
- ❌ `Port 9999 already in use` → 端口冲突
- ❌ `Invalid or corrupt jarfile` → JAR包损坏

### 4.2 使用云端调试
**位置**: 控制板 → 「云端调试」

1. 选择「公网访问」标签
2. 输入路径：`/api/home/data`
3. 点击「调试」
4. 查看返回结果：
   - **200 + 数据** = ✅ 成功！
   - **500** = 业务错误，看日志
   - **502/504** = 服务未启动
   - **404** = 路径错误

---

## 📱 第五步：小程序端配置

### 5.1 添加合法域名
**位置**: 微信公众平台 → 开发管理 → 服务器域名

添加 request 合法域名：
```
https://springboot-7903-244472-7-14207603295.sh.run.tcloudbase.com
```

### 5.2 重新编译小程序
```bash
cd d:\my\project\wallpaper\my_wallpaper

# 编译小程序
npm run dev:mp-weixin

# 在微信开发者工具中刷新项目
```

---

## 🎯 常见问题速查

### Q1: 还是显示 INVALID_HOST？
**A**: 
1. 检查最小副本数是否 ≥ 1
2. 查看「操作记录」，确认部署状态是「成功」
3. 等待 2-3 分钟让服务完全启动

### Q2: 数据库连不上？
**A**:
1. 确认 MYSQL_HOST 是内网地址（如 10.x.x.x）
2. 确认 MySQL 实例已启动
3. 确认账号密码正确

### Q3: 如何查看真实 IP？
**A**: 在云托管 MySQL 页面复制「内网地址」，格式通常是 `10.x.x.x:3306`

### Q4: 端口应该填什么？
**A**: 
- 代码里写的端口（application.yml 的 server.port）
- 云托管设置的监听端口
- 启动命令里的 --server.port
- **三者必须一致！** 建议都用 9999

---

## ✅ 成功标志

当你看到以下输出时，说明部署成功：

```json
// 访问 https://xxx.sh.run.tcloudbase.com/api/home/data
{
  "code": 200,
  "message": "成功",
  "data": {
    // ... 有数据返回
  }
}
```

小程序能正常加载数据，不报错！

---

## 📞 如果还是不行

请提供以下信息：
1. 运行日志截图（最近 50 行）
2. 操作记录截图（部署状态）
3. 实例配置截图（端口、副本数、启动命令）
4. 环境变量配置截图（脱敏后）

---

**最后更新时间**: 2026-04-14
**适用版本**: Spring Boot 2.7.18 + 微信云托管
