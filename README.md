# 壁纸小程序完整项目

这是一个完整的壁纸小程序项目，包含前端小程序、后台管理系统和后端接口。

## 项目结构

```
.
├── my_wallpaper/          # 前端小程序（uni-app）
├── wallpaper-admin/       # 后台管理系统（Vue 3 + Element Plus）
└── wallpaper-backend/     # 后端接口（Node.js + Express + MySQL）
```

## 快速开始

### 1. 后端接口

```bash
# 进入后端目录
cd wallpaper-backend

# 安装依赖
npm install

# 配置数据库（修改 .env 文件）
# 执行 database.sql 创建数据库

# 启动服务
npm run dev
```

后端服务运行在 `http://localhost:3000`

详细说明请查看 [wallpaper-backend/README.md](wallpaper-backend/README.md)

### 2. 后台管理系统

```bash
# 进入管理后台目录
cd wallpaper-admin

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

管理后台运行在 `http://localhost:5173`

默认账号：
- 用户名：`admin`
- 密码：`admin123`

详细说明请查看 [wallpaper-admin/README.md](wallpaper-admin/README.md)

### 3. 前端小程序

```bash
# 进入小程序目录
cd my_wallpaper

# 安装依赖
npm install

# 运行微信小程序
npm run dev:mp-weixin

# 运行H5
npm run dev:h5
```

## 功能特性

### 前端小程序
- 首页推荐（轮播图、推荐创作者、每日主题等）
- 分类浏览（手机壁纸、平板壁纸、头像）
- 创作者主页
- 专辑精选
- 壁纸预览和下载
- 搜索功能
- 排行榜

### 后台管理系统
- 用户登录认证
- 壁纸管理（增删改查、上传、推荐设置）
- 分类管理
- 创作者管理
- 专辑管理
- 数据统计

### 后端接口
- RESTful API
- JWT 认证
- 文件上传
- 数据库操作
- 分页查询
- 数据统计

## 技术栈

### 前端小程序
- uni-app
- Vue 3
- Sass

### 后台管理
- Vue 3
- Element Plus
- Vue Router
- Pinia
- Axios
- Vite

### 后端
- Node.js
- Express
- MySQL
- JWT
- Multer
- bcryptjs

## 数据库设计

主要数据表：
- `wallpapers` - 壁纸表
- `categories` - 分类表
- `authors` - 创作者表
- `albums` - 专辑表
- `admins` - 管理员表

详细的数据库结构请查看 `wallpaper-backend/database.sql`

## API 接口文档

### 基础URL
```
http://localhost:3000/api
```

### 主要接口
- `/users/login` - 用户登录
- `/wallpapers` - 壁纸管理
- `/categories` - 分类管理
- `/authors` - 创作者管理
- `/albums` - 专辑管理
- `/upload/single` - 文件上传

完整的API文档请查看后端README。

## 开发说明

1. 先启动后端服务
2. 再启动后台管理系统
3. 最后运行前端小程序
4. 通过后台管理系统添加数据
5. 在小程序中查看效果

## 注意事项

1. 确保 MySQL 数据库已安装并运行
2. 修改 `.env` 文件配置数据库连接
3. 执行 `database.sql` 创建数据库表
4. 生成管理员密码哈希值
5. 配置文件上传路径
6. 小程序需要配置后端接口地址

## License

MIT
