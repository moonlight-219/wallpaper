#!/bin/bash

echo "=========================================="
echo "后端诊断脚本 - 完整版"
echo "=========================================="
echo ""

# 1. 检查后端进程
echo "【1】检查后端进程是否运行..."
ps aux | grep java | grep -v grep
echo ""
JAVA_PROCESS=$(ps aux | grep java | grep -v grep | wc -l)
if [ $JAVA_PROCESS -gt 0 ]; then
    echo "✅ 找到 $JAVA_PROCESS 个Java进程"
else
    echo "❌ 没有找到Java进程"
fi
echo ""

# 2. 检查端口监听
echo "【2】检查端口9999是否被监听..."
netstat -tlnp 2>/dev/null | grep :9999 || ss -tlnp 2>/dev/null | grep :9999
if [ $? -eq 0 ]; then
    echo "✅ 端口9999正在监听"
else
    echo "❌ 端口9999未被监听"
fi
echo ""

# 3. 检查所有监听的端口
echo "【3】检查所有监听的端口..."
netstat -tlnp 2>/dev/null | grep LISTEN || ss -tlnp 2>/dev/null | grep LISTEN
echo ""

# 4. 测试后端直接访问
echo "【4】测试后端直接访问..."
echo ">>> curl http://127.0.0.1:9999/api/home/data"
RESPONSE=$(curl -s -w "\n---HTTP_CODE:%{http_code}---" http://127.0.0.1:9999/api/home/data)
echo "$RESPONSE"
echo ""

# 5. 测试通过Nginx访问
echo "【5】测试通过Nginx访问..."
echo ">>> curl http://127.0.0.1/api/home/data"
RESPONSE=$(curl -s -w "\n---HTTP_CODE:%{http_code}---" http://127.0.0.1/api/home/data)
echo "$RESPONSE"
echo ""

# 6. 检查Nginx配置文件
echo "【6】检查Nginx配置文件..."
if [ -f "/www/server/panel/vhost/nginx/java_wallaper.conf" ]; then
    echo "✅ 找到配置文件: /www/server/panel/vhost/nginx/java_wallaper.conf"
    echo "--- 配置内容 ---"
    cat /www/server/panel/vhost/nginx/java_wallaper.conf
    echo "--- 配置结束 ---"
else
    echo "❌ 未找到配置文件: /www/server/panel/vhost/nginx/java_wallaper.conf"
    echo "尝试查找其他配置文件..."
    ls -la /www/server/panel/vhost/nginx/ 2>/dev/null || echo "无法访问配置目录"
fi
echo ""

# 7. 检查Nginx主配置
echo "【7】检查Nginx主配置..."
if [ -f "/www/server/nginx/conf/nginx.conf" ]; then
    echo "查找include指令..."
    grep -n "include.*vhost" /www/server/nginx/conf/nginx.conf
else
    echo "未找到Nginx主配置文件"
fi
echo ""

# 8. 检查Nginx访问日志
echo "【8】检查Nginx访问日志（最后10行）..."
if [ -f "/www/wwwlogs/wallpaper_access.log" ]; then
    tail -10 /www/wwwlogs/wallpaper_access.log
else
    echo "❌ 未找到访问日志"
fi
echo ""

# 9. 检查Nginx错误日志
echo "【9】检查Nginx错误日志（最后20行）..."
if [ -f "/www/wwwlogs/wallpaper_error.log" ]; then
    tail -20 /www/wwwlogs/wallpaper_error.log
else
    echo "❌ 未找到错误日志"
fi
echo ""

# 10. 详细的curl请求（显示请求头和响应头）
echo "【10】详细的curl请求测试..."
echo ">>> curl -v http://127.0.0.1/api/home/data"
curl -v http://127.0.0.1/api/home/data 2>&1
echo ""

# 11. 测试Nginx是否运行
echo "【11】检查Nginx进程..."
ps aux | grep nginx | grep -v grep
if [ $? -eq 0 ]; then
    echo "✅ Nginx正在运行"
else
    echo "❌ Nginx未运行"
fi
echo ""

# 12. 测试后端其他路径
echo "【12】测试后端其他路径..."
echo ">>> curl http://127.0.0.1:9999/api/"
curl -s -w "\nHTTP状态码: %{http_code}\n" http://127.0.0.1:9999/api/
echo ""

# 13. 检查防火墙
echo "【13】检查防火墙规则..."
iptables -L -n 2>/dev/null | grep 9999 || echo "无法检查iptables或没有相关规则"
echo ""

echo "=========================================="
echo "诊断完成"
echo "=========================================="
echo ""
echo "请将以上完整输出发送给我，我会帮你分析问题。"
