# 壁纸系统数据库结构说明

## 1. 用户表 (users)

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|----------|------|
| id | BIGINT | - | 是 | 自增 | 主键ID |
| role | VARCHAR | 20 | 否 | - | 角色（user普通用户、admin管理员） |
| is_creator | BOOLEAN | - | 否 | true | 是否支持创作 |
| login_type | VARCHAR | 20 | 否 | - | 登录方式（wechat微信、password账号密码） |
| wechat_open_id | VARCHAR | 100 | 否 | - | 微信openId |
| username | VARCHAR | 50 | 否 | - | 用户名 |
| password | VARCHAR | 100 | 否 | - | 密码 |
| nickname | VARCHAR | 50 | 否 | - | 昵称 |
| avatar | VARCHAR | 200 | 否 | - | 头像URL |
| follower_count | INT | - | 否 | 0 | 粉丝数 |
| work_count | INT | - | 否 | 0 | 作品数 |
| bio | VARCHAR | 500 | 否 | - | 个人简介 |
| phone | VARCHAR | 20 | 否 | - | 手机号 |
| email | VARCHAR | 100 | 否 | - | 邮箱 |
| create_time | DATETIME | - | 否 | - | 创建时间 |
| update_time | DATETIME | - | 否 | - | 更新时间 |
| is_del | BOOLEAN | - | 否 | false | 是否删除 |

## 2. 壁纸表 (wallpapers)

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|----------|------|
| id | BIGINT | - | 是 | 自增 | 主键ID |
| title | VARCHAR | 200 | 是 | - | 标题 |
| url | VARCHAR | 500 | 是 | - | 图片地址 |
| type | VARCHAR | 20 | 否 | - | 类型（phone手机、tablet平板、avatar头像） |
| category_id | BIGINT | - | 否 | - | 二级分类ID |
| category | VARCHAR | 100 | 否 | - | 二级分类名称 |
| description | VARCHAR | 1000 | 否 | - | 描述 |
| author_id | BIGINT | - | 否 | - | 创作者ID |
| album_id | BIGINT | - | 否 | - | 专辑ID |
| view_count | INT | - | 否 | 0 | 浏览量 |
| download_count | INT | - | 否 | 0 | 下载量 |
| like_count | INT | - | 否 | 0 | 点赞量 |
| collect_count | INT | - | 否 | 0 | 收藏量 |
| tags | VARCHAR | 500 | 否 | - | 标签 |
| status | VARCHAR | 20 | 否 | - | 状态（approved已通过、pending审核中、rejected未通过） |
| create_time | DATETIME | - | 否 | - | 创建时间 |
| update_time | DATETIME | - | 否 | - | 更新时间 |
| is_del | BOOLEAN | - | 否 | false | 是否删除 |

## 3. 分类表 (categories)

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|----------|------|
| id | BIGINT | - | 是 | 自增 | 主键ID |
| name | VARCHAR | 50 | 是 | - | 分类名称 |
| alias | VARCHAR | 50 | 否 | - | 别名 |
| type | VARCHAR | 20 | 是 | - | 类型（phone手机、tablet平板、avatar头像） |
| icon_path | VARCHAR | 100 | 否 | - | 图标路径 |
| sort_order | INT | - | 否 | 0 | 排序 |
| create_time | DATETIME | - | 否 | - | 创建时间 |
| update_time | DATETIME | - | 否 | - | 更新时间 |
| is_del | BOOLEAN | - | 否 | false | 是否删除 |

## 4. 专辑表 (albums)

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|----------|------|
| id | BIGINT | - | 是 | 自增 | 主键ID |
| title | VARCHAR | 100 | 是 | - | 专辑标题 |
| cover | VARCHAR | 500 | 否 | - | 封面图 |
| description | VARCHAR | 1000 | 否 | - | 描述 |
| author_id | BIGINT | - | 否 | - | 创作者ID |
| category_id | BIGINT | - | 否 | - | 分类ID |
| wallpaper_count | INT | - | 否 | 0 | 壁纸数量 |
| view_count | INT | - | 否 | 0 | 浏览量 |
| collect_count | INT | - | 否 | 0 | 收藏量 |
| create_time | DATETIME | - | 否 | - | 创建时间 |
| update_time | DATETIME | - | 否 | - | 更新时间 |
| is_del | BOOLEAN | - | 否 | false | 是否删除 |

## 5. 用户行为表 (user_actions)

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|----------|------|
| id | BIGINT | - | 是 | 自增 | 主键ID |
| user_id | BIGINT | - | 是 | - | 用户ID |
| wallpaper_id | BIGINT | - | 否 | - | 壁纸ID |
| album_id | BIGINT | - | 否 | - | 专辑ID |
| author_id | BIGINT | - | 否 | - | 创作者ID |
| action_type | VARCHAR | 20 | 是 | - | 行为类型（like点赞、collect收藏、download下载） |
| create_time | DATETIME | - | 否 | - | 创建时间 |
| is_del | BOOLEAN | - | 否 | false | 是否删除 |

## 6. 举报表 (reports)

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|----------|------|
| id | BIGINT | - | 是 | 自增 | 主键ID |
| user_id | BIGINT | - | 是 | - | 用户ID |
| wallpaper_id | BIGINT | - | 是 | - | 壁纸ID |
| album_id | BIGINT | - | 否 | - | 专辑ID |
| author_id | BIGINT | - | 否 | - | 创作者ID |
| report_type | VARCHAR | 20 | 是 | - | 举报类型 |
| reason | VARCHAR | 500 | 是 | - | 举报原因 |
| create_time | DATETIME | - | 否 | - | 创建时间 |
| is_del | BOOLEAN | - | 否 | false | 是否删除 |

## 7. 创作者表 (authors)

| 字段名 | 类型 | 长度 | 必填 | 默认值 | 说明 |
|--------|------|--------|------|----------|------|
| id | BIGINT | - | 是 | 自增 | 主键ID |
| name | VARCHAR | 50 | 是 | - | 创作者名称 |
| avatar | VARCHAR | 200 | 否 | - | 头像URL |
| bio | VARCHAR | 500 | 否 | - | 个人简介 |
| wallpaper_count | INT | - | 否 | 0 | 壁纸数量 |
| follower_count | INT | - | 否 | 0 | 粉丝数 |
| work_count | INT | - | 否 | 0 | 作品数 |
| create_time | DATETIME | - | 否 | - | 创建时间 |
| update_time | DATETIME | - | 否 | - | 更新时间 |
| is_del | BOOLEAN | - | 否 | false | 是否删除 |

## 表关系说明

- **users** 表是用户表，包含普通用户和管理员
- **wallpapers** 表通过 author_id 关联到 users 表
- **wallpapers** 表通过 category_id 关联到 categories 表
- **wallpapers** 表通过 album_id 关联到 albums 表
- **albums** 表通过 author_id 关联到 users 表
- **albums** 表通过 category_id 关联到 categories 表
- **user_actions** 表记录用户的点赞、收藏、下载行为
- **reports** 表记录用户的举报信息