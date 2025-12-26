# 專案檔案說明

## 保留的重要檔案

### 核心專案檔案
- `pom.xml` - Maven 專案設定
- `README.md` - 專案說明文件
- `src/` - 後端原始碼
- `frontend/` - 前端 Vue 3 專案

### Docker 相關
- `docker-compose-oracle-free.yml` - Oracle 資料庫 Docker 設定（主要使用）
- `docker-compose.yml` - Oracle Express 版本（需要 Oracle 帳號）
- `docker-compose-postgres.yml` - PostgreSQL 替代方案
- `docker-compose-oracle-slim.yml` - Oracle Slim 版本
- `DOCKER_SETUP.md` - Docker 設定說明
- `start-docker-db.bat` / `start-docker-db.sh` - 啟動 Docker 資料庫

### 應用程式啟動
- `start-spring-boot.bat` - 啟動 Spring Boot 應用程式

### 工具腳本（可選保留）
- `quick-sql-query.bat` - 快速 SQL 查詢工具（方便測試）

### 資料庫設定檔
- `src/main/resources/application.properties` - Spring Boot 設定
- `src/main/resources/schema.sql` - 資料庫建表腳本（參考用）
- `src/main/resources/data.sql` - 初始化資料腳本（參考用）

## 已清理的測試檔案

已刪除所有測試、診斷、除錯相關的檔案，包括：
- 所有 test-*.bat, test-*.sql, test-*.txt
- 所有 check-*.bat, check-*.sql
- 所有 verify-*.bat, verify-*.sql
- 所有 DBeaver 相關的診斷檔案
- 所有連線問題排除檔案

## 專案結構

```
order-currency-system/
├── src/                    # 後端原始碼
│   └── main/
│       ├── java/          # Java 原始碼
│       └── resources/    # 設定檔和 SQL 腳本
├── frontend/              # 前端 Vue 3 專案
├── docker-compose*.yml    # Docker 設定檔案
├── start-docker-db.bat    # 啟動資料庫
├── start-spring-boot.bat  # 啟動應用程式
├── pom.xml                # Maven 設定
└── README.md              # 專案說明
```

## 快速開始

1. **啟動資料庫**：`start-docker-db.bat`
2. **啟動後端**：`start-spring-boot.bat`
3. **啟動前端**：`cd frontend && npm install && npm run dev`


