# 壁纸后台管理系统

基于 Vue 3 + Element Plus 的壁纸后台管理系统。

## 功能特性

- 用户登录认证
- 壁纸管理（增删改查、分页、筛选、图片上传）
- 分类管理
- 创作者管理
- 专辑管理
- 数据统计展示

## 技术栈

- Vue 3
- Vue Router
- Pinia（状态管理）
- Element Plus（UI组件库）
- Axios（HTTP请求）
- Vite（构建工具）

## 安装步骤

### 1. 安装依赖

```bash
cd wallpaper-admin
npm install
```

### 2. 配置后端接口

修改 `vite.config.js` 中的代理配置，确保指向正确的后端地址：

```javascript
proxy: {
  '/api': {
    target: 'http://localhost:3000',  // 后端地址
    changeOrigin: true
  }
}
```

### 3. 启动开发服务器

```bash
npm run dev
```

访问 `http://localhost:5173`

### 4. 构建生产版本

```bash
npm run build
```

构建后的文件在 `dist` 目录。

## 默认账号

- 用户名：`admin`
- 密码：`admin123`

## 功能模块

### 1. 首页

- 数据统计展示
- 壁纸总数、创作者数量、专辑数量、总浏览量

### 2. 壁纸管理

- 壁纸列表展示（支持分页）
- 按类型筛选（手机/平板/头像）
- 添加/编辑壁纸
- 图片上传
- 设置推荐/热门标签
- 删除壁纸

### 3. 分类管理

- 分类列表
- 添加/编辑/删除分类
- 设置分类排序

### 4. 创作者管理

- 创作者列表（支持分页）
- 添加/编辑创作者信息
- 头像上传
- 粉丝数和作品数管理

### 5. 专辑管理

- 专辑列表（支持分页）
- 添加/编辑专辑
- 封面图上传
- 壁纸数量统计

## 目录结构

```
wallpaper-admin/
├── src/
│   ├── api/
│   │   └── index.js        # API接口
│   ├── layout/
│   │   └── Index.vue       # 布局组件
│   ├── router/
│   │   └── index.js        # 路由配置
│   ├── stores/
│   │   └── user.js         # 用户状态管理
│   ├── utils/
│   │   └── request.js      # Axios封装
│   ├── views/
│   │   ├── Login.vue       # 登录页
│   │   ├── Dashboard.vue   # 首页
│   │   ├── Wallpapers.vue  # 壁纸管理
│   │   ├── Categories.vue  # 分类管理
│   │   ├── Authors.vue     # 创作者管理
│   │   └── Albums.vue      # 专辑管理
│   ├── App.vue
│   └── main.js
├── index.html
├── vite.config.js
└── package.json
```

## 注意事项

1. 确保后端服务已启动
2. 确保数据库已正确配置
3. 上传的图片会保存在后端的 `uploads` 目录
4. 图片路径需要配置为可访问的URL
