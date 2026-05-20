# 完整启动指南

## 📋 前置要求

1. **Node.js** (v14 或更高版本)
2. **MySQL** (v5.7 或更高版本)
3. **npm** 或 **yarn**

---

## 🚀 第一步：启动后端服务

### 1. 安装后端依赖

打开终端，执行：

```bash
cd wallpaper-backend
npm install
```

### 2. 配置数据库

#### 2.1 创建数据库

打开 MySQL 命令行或者使用 Navicat/phpMyAdmin 等工具，执行：

```sql
CREATE DATABASE wallpaper_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

或者直接导入 SQL 文件：

```bash
mysql -u root -p < database.sql
```

#### 2.2 配置环境变量

编辑 `wallpaper-backend/.env` 文件，修改数据库配置：

```env
PORT=3000
DB_HOST=localhost
DB_USER=root
DB_PASSWORD=你的数据库密码
DB_NAME=wallpaper_db
JWT_SECRET=your_jwt_secret_key_here_change_this
```

### 3. 生成管理员密码

```bash
node scripts/generate-password.js
```

复制生成的哈希值，然后编辑 `database.sql` 文件，找到这一行：

```sql
INSERT INTO admins (username, password, nickname) VALUES 
('admin', '$2a$10$YourHashedPasswordHere', '超级管理员');
```

将 `$2a$10$YourHashedPasswordHere` 替换为刚才生成的哈希值。

然后重新执行 SQL 文件或手动插入管理员数据。

### 4. 启动后端服务

```bash
# 开发模式（自动重启）
npm run dev

# 或者生产模式
npm start
```

看到以下信息表示启动成功：

```
服务器运行在 http://localhost:3000
```

测试接口：访问 `http://localhost:3000` 应该看到：

```json
{"message":"壁纸后端API服务"}
```

---

## 🎨 第二步：启动后台管理系统

### 1. 安装前端依赖

打开**新的终端窗口**，执行：

```bash
cd wallpaper-admin
npm install
```

### 2. 启动开发服务器

```bash
npm run dev
```

看到以下信息表示启动成功：

```
VITE v4.x.x  ready in xxx ms

➜  Local:   http://localhost:5173/
➜  Network: use --host to expose
```

### 3. 访问后台管理系统

浏览器打开：`http://localhost:5173`

**默认登录账号：**
- 用户名：`admin`
- 密码：`admin123`

---

## 📱 第三步：启动小程序（可选）

### 1. 安装小程序依赖

```bash
cd my_wallpaper
npm install
```

### 2. 配置后端接口地址

编辑 `my_wallpaper/src/utils/request.js`，修改 baseURL：

```javascript
const request = axios.create({
  baseURL: 'http://localhost:3000/api',  // 改为你的后端地址
  timeout: 10000
})
```

### 3. 运行小程序

```bash
# 微信小程序
npm run dev:mp-weixin

# H5
npm run dev:h5
```

---

## ⚠️ 常见问题

### 问题1：数据库连接失败

**错误信息：** `ER_ACCESS_DENIED_ERROR` 或 `ECONNREFUSED`

**解决方案：**
1. 检查 MySQL 服务是否启动
2. 检查 `.env` 文件中的数据库配置是否正确
3. 确认数据库用户名和密码是否正确

### 问题2：端口被占用

**错误信息：** `EADDRINUSE: address already in use`

**解决方案：**
```bash
# Windows 查找占用端口的进程
netstat -ano | findstr :3000

# 杀死进程（替换 PID）
taskkill /PID <进程ID> /F
```

### 问题3：Sass 导入警告

**错误信息：** `@import is deprecated`

这是 Sass 的警告，不影响运行。如果要解决，将 `@import` 改为 `@use`。

### 问题4：登录失败

**解决方案：**
1. 确保后端服务正常运行
2. 检查数据库中是否有管理员数据
3. 确认密码哈希值是否正确生成
4. 查看浏览器控制台和后端日志

---

## 📝 快速测试流程

### 1. 测试后端接口

```bash
# 测试根路径
curl http://localhost:3000

# 测试登录接口
curl -X POST http://localhost:3000/api/users/login \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"admin\",\"password\":\"admin123\"}"
```

### 2. 测试后台管理

1. 访问 `http://localhost:5173`
2. 使用 `admin` / `admin123` 登录
3. 进入"壁纸管理"页面
4. 点击"添加壁纸"测试上传功能

### 3. 添加测试数据

登录后台管理系统后：

1. **添加分类**：进入"分类管理" → 点击"添加分类"
2. **添加创作者**：进入"创作者管理" → 点击"添加创作者"
3. **添加专辑**：进入"专辑管理" → 点击"添加专辑"
4. **添加壁纸**：进入"壁纸管理" → 点击"添加壁纸"

---

## 🔧 开发建议

### 后端开发

- 使用 `npm run dev` 启动，代码修改会自动重启
- 查看日志排查问题
- 使用 Postman 测试 API

### 前端开发

- 使用浏览器开发者工具调试
- 检查 Network 面板查看 API 请求
- 使用 Vue DevTools 调试组件

### 数据库管理

- 推荐使用 Navicat、DBeaver 或 phpMyAdmin
- 定期备份数据库
- 查看数据库日志排查问题

---

## 📦 生产部署

### 后端部署

```bash
cd wallpaper-backend
npm install --production
npm start
```

建议使用 PM2：

```bash
npm install -g pm2
pm2 start app.js --name wallpaper-api
```

### 前端部署

```bash
cd wallpaper-admin
npm run build
```

将 `dist` 目录部署到 Nginx 或其他 Web 服务器。

---

## 📞 需要帮助？

如果遇到问题：

1. 检查终端的错误信息
2. 查看浏览器控制台
3. 检查数据库连接
4. 确认所有服务都在运行
5. 查看 README.md 文档

祝你使用愉快！🎉
