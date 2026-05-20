#!/bin/bash

# 壁纸项目后端自动部署脚本
# 使用方法: ./deploy.sh [选项]
#   选项:
#     --build      # 重新编译打包
#     --restart    # 仅重启服务
#     --logs       # 查看最新日志
#     --status     # 查看服务状态
#     --test       # 测试接口

set -e

APP_NAME="wallpaper-server"
JAR_FILE="wallpaper-server-1.0.0.jar"
LOG_FILE="wallpaper.log"
PID_FILE="wallpaper.pid"
PORT=9999

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 获取进程ID
get_pid() {
    if [ -f "$PID_FILE" ]; then
        cat "$PID_FILE"
    else
        pgrep -f "$JAR_FILE" || echo ""
    fi
}

# 停止服务
stop_service() {
    log_info "停止 $APP_NAME 服务..."
    
    PID=$(get_pid)
    if [ -n "$PID" ]; then
        kill $PID 2>/dev/null || true
        sleep 2
        
        # 强制杀死
        if pgrep -f "$JAR_FILE" > /dev/null; then
            log_warn "强制停止服务..."
            kill -9 $(pgrep -f "$JAR_FILE") 2>/dev/null || true
        fi
        
        log_info "服务已停止"
    else
        log_warn "服务未在运行"
    fi
    
    rm -f "$PID_FILE"
}

# 编译打包
build_project() {
    log_info "开始编译打包..."
    
    if [ ! -f "pom.xml" ]; then
        log_error "未找到 pom.xml，请确保在项目根目录执行此脚本"
        exit 1
    fi
    
    mvn clean package -DskipTests -q
    
    if [ $? -eq 0 ]; then
        log_info "编译打包成功！"
        ls -lh target/$JAR_FILE
    else
        log_error "编译失败！"
        exit 1
    fi
}

# 启动服务
start_service() {
    log_info "启动 $APP_NAME 服务..."
    
    if [ ! -f "target/$JAR_FILE" ]; then
        log_error "未找到 JAR 文件: target/$JAR_FILE"
        log_info "请先执行: ./deploy.sh --build"
        exit 1
    fi
    
    # JVM参数
    JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"
    
    # 启动
    nohup java $JAVA_OPTS \
        -jar target/$JAR_FILE \
        --server.port=$PORT \
        --server.servlet.context-path=/api \
        > "$LOG_FILE" 2>&1 &
    
    echo $! > "$PID_FILE"
    
    log_info "服务启动中，PID: $(cat $PID_FILE)"
    log_info "日志文件: $LOG_FILE"
    
    # 等待启动
    sleep 5
    
    # 检查是否启动成功
    if pgrep -f "$JAR_FILE" > /dev/null; then
        log_info "✅ 服务启动成功！"
        log_info "访问地址: http://localhost:$PORT/api/health"
    else
        log_error "❌ 服务启动失败！请查看日志: tail -f $LOG_FILE"
        exit 1
    fi
}

# 查看状态
show_status() {
    log_info "=== $APP_NAME 服务状态 ==="
    
    PID=$(get_pid)
    if [ -n "$PID" ]; then
        log_info "运行状态: ✅ 运行中"
        log_info "进程ID: $PID"
        
        # 检查端口
        if netstat -tlnp 2>/dev/null | grep -q ":$PORT "; then
            log_info "监听端口: ✅ $PORT"
        else
            log_warn "监听端口: ⚠️  未检测到端口 $PORT"
        fi
        
        # 检查健康状态
        HEALTH=$(curl -s http://localhost:$PORT/api/health 2>/dev/null || echo "failed")
        if [ "$HEALTH" != "failed" ]; then
            log_info "健康检查: ✅ 正常"
        else
            log_warn "健康检查: ⚠️  无响应"
        fi
    else
        log_info "运行状态: ❌ 未运行"
    fi
    
    # JAR文件信息
    if [ -f "target/$JAR_FILE" ]; then
        log_info ""
        log_info "JAR文件信息:"
        ls -lh target/$JAR_FILE
        stat target/$JAR_FILE | grep "Modify"
    fi
}

# 查看日志
show_logs() {
    LINES=${1:-50}
    log_info "显示最近 $LINES 行日志:"
    echo "----------------------------------------"
    tail -$LINES "$LOG_FILE" 2>/dev/null || log_error "日志文件不存在"
}

# 测试接口
test_api() {
    log_info "=== API 测试 ==="
    
    # 1. 健康检查
    log_info "1. 健康检查 (GET /api/health):"
    curl -s http://localhost:$PORT/api/health && echo ""
    echo ""
    
    # 2. 测试认证接口（无Token）
    log_info "2. 测试认证 (POST /api/works 无Token):"
    RESPONSE=$(curl -s -w "\nHTTP_CODE:%{http_code}" -X POST \
        http://localhost:$PORT/api/works \
        -H "Content-Type: application/json" \
        -d '{"title":"test"}')
    
    HTTP_CODE=$(echo "$RESPONSE" | grep "HTTP_CODE:" | cut -d: -f2)
    BODY=$(echo "$RESPONSE" | grep -v "HTTP_CODE:")
    
    log_info "HTTP状态码: $HTTP_CODE"
    log_info "响应体: $BODY"
    
    if [ "$HTTP_CODE" = "401" ]; then
        log_info "✅ 新版本已生效！拦截器正确拦截了未认证请求"
    elif [ "$HTTP_CODE" = "200" ] && echo "$BODY" | grep -q '"code":401'; then
        log_warn "⚠️  可能是旧版本！拦截器未正确执行"
    fi
    
    echo ""
    
    # 3. 显示最近的关键日志
    log_info "3. 最近的相关日志:"
    grep -E "(Interceptor|works|认证|Token|JWT)" "$LOG_FILE" 2>/dev/null | tail -10 || log_info "无相关日志"
}

# 主逻辑
case "${1:-}" in
    --build)
        build_project
        ;;
    --restart)
        stop_service
        start_service
        show_status
        ;;
    --logs)
        show_logs ${2:-50}
        ;;
    --status)
        show_status
        ;;
    --test)
        test_api
        ;;
    --full)
        log_info "=== 完整部署流程 ==="
        stop_service
        build_project
        start_service
        sleep 3
        test_api
        ;;
    *)
        echo "用法: $0 {--build|--restart|--logs|--status|--test|--full}"
        echo ""
        echo "选项说明:"
        echo "  --build      重新编译打包"
        echo "  --restart    重启服务"
        echo "  --logs [行数] 查看日志"
        echo "  --status     查看服务状态"
        echo "  --test       测试API接口"
        echo "  --full       完整部署（编译+重启+测试）"
        exit 1
        ;;
esac
