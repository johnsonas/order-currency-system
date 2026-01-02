# 多階段構建 - 後端 Spring Boot 應用
# Stage 1: 構建階段
FROM maven:3.9-eclipse-temurin-17 AS build

# 設置工作目錄
WORKDIR /app

# 複製 pom.xml 和源碼
COPY pom.xml .
COPY src ./src

# 構建應用（跳過測試，測試在 CI 中執行）
RUN mvn clean package -DskipTests

# Stage 2: 運行階段
FROM eclipse-temurin:17-jre-alpine

# 設置工作目錄
WORKDIR /app

# 創建非 root 用戶
RUN addgroup -S spring && adduser -S spring -G spring

# 從構建階段複製 JAR 檔案
COPY --from=build /app/target/*.jar app.jar

# 更改所有權
RUN chown spring:spring app.jar

# 切換到非 root 用戶
USER spring:spring

# 暴露端口
EXPOSE 8080

# 健康檢查
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 啟動應用
ENTRYPOINT ["java", "-jar", "app.jar"]


