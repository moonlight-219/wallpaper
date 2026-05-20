#!/bin/bash

echo "=========================================="
echo "Nginx 代理诊断脚本"
echo "=========================================="
echo ""

# 1. 检查后端服务是否运行
echo "1. 检查后端服务..."
if curl -s http://127.0.0.1:9999/api/home/data > /dev/null; then
    echo "✅ 后端服务正常运行"
    echo "   测试URL: http://127.0.0.1:9999/api/home/data"
else
    echo "❌ 后端服务无法访问"
    echo "   请检查后端是否启动: ps aux | grep java"
    exit 1
fi
echo ""

# 2. 检查nginx是否运行
echo "2. 检查nginx服务..."
if pgrep nginx > /dev/null; then
    echo "✅ Nginx正在运行"
    nginx -v 2>&1 | head -1
else
    echo "❌ Nginx未运行"
    echo "   启动命令: systemctl start nginx"
    exit 1
fi
echo ""

# 3. 测试nginx配置
echo "3. 测试nginx配置..."
if nginx -t 2>&1 | grep -q "successful"; then
    echo "✅ Nginx配置语法正确"
else
    echo "❌ Nginx配置有错误"
    nginx -t
    exit 1
fi
echo ""

# 4. 检查端口占用
echo "4. 检查端口占用..."
echo "   后端端口9999:"
netstat -tlnp | grep :9999 || echo "   ⚠️  端口9999未被监听"
echo "   Nginx端口80:"
netstat -tlnp | grep :80 || echo "   ⚠️  端口80未被监听"
echo ""

# 5. 测试通过nginx访问
echo "5. 测试通过nginx访问API..."
response=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1/api/home/data)
if [ "$response" = "200" ]; then
    echo "✅ 通过nginx访问成功 (HTTP $response)"
    echo "   测试URL: http://127.0.0.1/api/home/data"
else
    echo "❌ 通过nginx访问失败 (HTTP $response)"
    echo "   测试URL: http://127.0.0.1/api/home/data"
    echo ""
    echo "   详细响应:"
    curl -v http://127.0.0.1/api/home/data 2>&1 | head -20
fi
echo ""

# 6. 检查nginx日志
echo "6. 检查nginx错误日志（最后10行）..."
if [ -f /www/wwwlogs/wallpaper_error.log ]; then
    tail -10 /www/wwwlogs/wallpaper_error.log
else
    echo "   ⚠️  日志文件不存在: /www/wwwlogs/wallpaper_error.log"
fi
echo ""

# 7. 检查防火墙
echo "7. 检查防火墙..."
if command -v firewall-cmd &> /dev/null; then
    if firewall-cmd --list-ports | grep -q "80/tcp"; then
        echo "✅ 防火墙已开放80端口"
    else
        echo "⚠️  防火墙未开放80端口"
        echo "   开放命令: firewall-cmd --permanent --add-port=80/tcp && firewall-cmd --reload"
    fi
else
    echo "   ℹ️  未安装firewalld"
fi
echo ""

# 8. 显示当前nginx配置
echo "8. 当前nginx配置（/api/相关）..."
nginx -T 2>/dev/null | grep -A 10 "location.*\/api"
echo ""

echo "=========================================="
echo "诊断完成"
echo "=========================================="
