# Docker 資料庫設定指南

本專案支援多種資料庫 Docker 設定方式。

## 方案一：Oracle Database Free（推薦）

使用免費的 Oracle Database Free 版本，無需 Oracle 帳號登入。

### 啟動步驟

1. **啟動 Oracle 資料庫容器**
```bash
docker-compose -f docker-compose-oracle-free.yml up -d
```

2. **等待資料庫啟動完成**（約 1-2 分鐘）
```bash
docker logs -f order-currency-oracle
```
看到 `DATABASE IS READY TO USE!` 表示啟動完成

3. **修改 application.properties**
```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/FREEPDB1
spring.datasource.username=sys
spring.datasource.password=Oracle123
```

4. **建立使用者（可選，建議建立專用使用者）**
```bash
docker exec -it order-currency-oracle sqlplus sys/Oracle123@FREEPDB1 as sysdba
```

在 SQL*Plus 中執行：
```sql
CREATE USER ordersystem IDENTIFIED BY ordersystem123;
GRANT CONNECT, RESOURCE, DBA TO ordersystem;
ALTER USER ordersystem QUOTA UNLIMITED ON USERS;
EXIT;
```

然後修改 application.properties：
```properties
spring.datasource.username=ordersystem
spring.datasource.password=ordersystem123
```

### 停止資料庫
```bash
docker-compose -f docker-compose-oracle-free.yml down
```

### 完全清除資料（包含資料）
```bash
docker-compose -f docker-compose-oracle-free.yml down -v
```

## 方案二：Oracle Database Express（官方版本）

需要 Oracle Container Registry 帳號。

### 登入 Oracle Container Registry
```bash
docker login container-registry.oracle.com
```

### 啟動資料庫
```bash
docker-compose up -d
```

### 修改 application.properties
```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/XE
spring.datasource.username=sys
spring.datasource.password=Oracle123
```

## 方案三：PostgreSQL（快速測試用）

如果 Oracle 設定遇到困難，可以使用 PostgreSQL 快速開始。

### 注意事項
使用 PostgreSQL 需要修改：
1. `pom.xml` - 將 Oracle JDBC 驅動改為 PostgreSQL
2. `application.properties` - 修改資料庫連線設定
3. 使用 `schema-postgres.sql` 和 `data-postgres.sql`

### 啟動 PostgreSQL
```bash
docker-compose -f docker-compose-postgres.yml up -d
```

## 常用 Docker 指令

### 查看容器狀態
```bash
docker ps
```

### 查看資料庫日誌
```bash
docker logs order-currency-oracle
```

### 進入容器
```bash
docker exec -it order-currency-oracle bash
```

### 連接到資料庫（Oracle）
```bash
docker exec -it order-currency-oracle sqlplus sys/Oracle123@FREEPDB1 as sysdba
```

### 備份資料
```bash
docker exec order-currency-oracle expdp sys/Oracle123@FREEPDB1 schemas=ORDERSYSTEM directory=DATA_PUMP_DIR dumpfile=backup.dmp
```

## 疑難排解

### 問題：容器無法啟動
- 檢查端口 1521 是否被占用：`netstat -an | findstr 1521`
- 查看日誌：`docker logs order-currency-oracle`

### 問題：連線被拒絕
- 確認容器已完全啟動（等待 1-2 分鐘）
- 檢查 healthcheck 狀態：`docker ps`

### 問題：記憶體不足
Oracle 需要至少 2GB RAM，確保 Docker Desktop 分配足夠記憶體。

## 預設連線資訊

### Oracle Free
- Host: localhost
- Port: 1521
- Service Name: FREEPDB1
- Username: sys
- Password: Oracle123

### PostgreSQL（如果使用）
- Host: localhost
- Port: 5432
- Database: order_currency_db
- Username: postgres
- Password: postgres123







