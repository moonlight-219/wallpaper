# 第一阶段：构建
FROM maven:3.6-openjdk-8-slim AS builder

WORKDIR /app

# 先复制 pom.xml，利用 Docker 缓存层加速依赖下载
COPY pom.xml .

# 复制源代码
COPY src ./src

# 执行 Maven 构建（跳过测试）
RUN mvn clean package -DskipTests -q

# 第二阶段：运行（更小的镜像）
FROM openjdk:8-jre-slim

WORKDIR /app

# 从构建阶段复制 JAR 包
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9999

# JVM 参数优化
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=100"

# 启动命令
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar --server.port=9999 --server.servlet.context-path=/api"]
