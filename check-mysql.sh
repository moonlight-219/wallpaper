#!/bin/bash

echo "========== MySQL 连接检查 =========="
echo ""

# 检查 MySQL 是否运行
echo "1. 检查 MySQL 服务状态："
systemctl status mysql 2>/dev/null || systemctl status mysqld 2>/dev/null || echo "无法检查服务状态，请手动确认"
echo ""

# 检查 MySQL 端口
echo "2. 检查 MySQL 端口 3306："
netstat -tlnp | grep 3306 || ss -tlnp | grep 3306 || echo "端口 3306 未监听"
echo ""

# 测试连接（需要输入密码）
echo "3. 测试 MySQL 连接："
echo "请输入 MySQL root 密码进行测试："
mysql -u root -p -e "SELECT 'MySQL 连接成功！' as status;"
echo ""

# 检查数据库是否存在
echo "4. 检查 wallpaper_db 数据库："
echo "请输入 MySQL root 密码："
mysql -u root -p -e "SHOW DATABASES LIKE 'wallpaper_db';"
echo ""

echo "========== 检查完成 =========="
