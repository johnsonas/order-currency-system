# 訂單與幣別轉換系統

這是一個功能完整的訂單管理與幣別轉換系統，使用 Spring Boot 3 + Vue 3 + Oracle 技術棧開發，包含用戶認證、權限管理、表單驗證等完整功能。

## 技術棧

### 後端
- **框架**: Spring Boot 3.2.0
- **安全**: Spring Security + JWT 認證
- **資料庫**: Spring Data JPA + Oracle Database
- **快取**: Redis
- **API 文檔**: SpringDoc OpenAPI (Swagger)
- **排程**: Spring Scheduler（自動更新匯率）
- **測試**: JUnit 5 + Mockito + RestAssured

### 前端
- **框架**: Vue 3 + Vite
- **表單驗證**: VeeValidate + Yup
- **HTTP 客戶端**: Axios
- **UI**: 現代化響應式設計

### 資料庫
- **主要資料庫**: Oracle Database (Free Edition)
- **快取**: Redis

## 核心功能

### 1. 訂單管理 (Order)
- ✅ 新增、查詢、更新、刪除訂單
- ✅ 支援折扣計算（0-100%）
- ✅ 訂單狀態管理（待處理、已確認、已取消、已完成）
- ✅ 訂單 ID 搜尋（支援大量資料）
- ✅ 分頁查詢
- ✅ 訂單金額轉換為 TWD 或其他幣別

### 2. 幣別管理 (Currency)
- ✅ 幣別匯率管理（CRUD）
- ✅ 金額轉換功能
- ✅ 自動從 ExchangeRate-API 更新匯率
- ✅ 支援多種幣別（TWD, USD, EUR, JPY, CNY）

### 3. 用戶認證與授權
- ✅ JWT Token 認證
- ✅ 用戶註冊與登入
- ✅ 角色管理（ROLE_USER, ROLE_ADMIN）
- ✅ 選單權限控制
- ✅ 基於角色的功能存取控制

### 4. 前端表單功能
- ✅ 即時表單驗證（VeeValidate）
- ✅ 金額格式化（千分位）
- ✅ 即時計算預覽（原始金額、折扣、最終金額）
- ✅ 使用者名稱自動完成
- ✅ 折扣滑桿調整
- ✅ 狀態按鈕組選擇
- ✅ 表單狀態指示器

## 專案結構

```
order-currency-system/
├── src/
│   ├── main/
│   │   ├── java/com/example/ordersystem/
│   │   │   ├── OrderCurrencyApplication.java    # 應用程式入口
│   │   │   ├── config/                          # 配置類
│   │   │   │   ├── DataInitializer.java         # 資料初始化
│   │   │   │   ├── OpenApiConfig.java           # Swagger 配置
│   │   │   │   ├── PasswordEncoderConfig.java   # 密碼編碼器
│   │   │   │   ├── RedisConfig.java              # Redis 配置
│   │   │   │   ├── RestTemplateConfig.java       # HTTP 客戶端配置
│   │   │   │   ├── SecurityConfig.java           # Spring Security 配置
│   │   │   │   ├── ShutdownConfig.java           # 優雅關閉配置
│   │   │   │   ├── StartupInfoListener.java     # 啟動資訊監聽器
│   │   │   │   └── WebConfig.java                # Web 配置（CORS 等）
│   │   │   ├── controller/                      # REST API 控制器
│   │   │   │   ├── AuthController.java           # 認證 API
│   │   │   │   ├── CurrencyController.java       # 幣別 API
│   │   │   │   └── OrderController.java          # 訂單 API
│   │   │   ├── dto/                              # 資料傳輸物件
│   │   │   │   ├── ErrorResponse.java
│   │   │   │   ├── ExchangeRateResponse.java
│   │   │   │   ├── JwtResponse.java
│   │   │   │   ├── LoginRequest.java
│   │   │   │   ├── MenuItem.java
│   │   │   │   └── RegisterRequest.java
│   │   │   ├── exception/                        # 異常處理
│   │   │   │   ├── BadRequestException.java
│   │   │   │   ├── CurrencyNotFoundException.java
│   │   │   │   ├── GlobalExceptionHandler.java   # 全域異常處理器
│   │   │   │   ├── OrderNotFoundException.java
│   │   │   │   └── ResourceNotFoundException.java
│   │   │   ├── filter/                           # 過濾器
│   │   │   │   └── JwtAuthenticationFilter.java  # JWT 認證過濾器
│   │   │   ├── model/                             # 實體類
│   │   │   │   ├── Currency.java                 # 幣別實體
│   │   │   │   ├── CurrencyCode.java             # 幣別代碼枚舉
│   │   │   │   ├── Menu.java                     # 選單實體
│   │   │   │   ├── Order.java                    # 訂單實體
│   │   │   │   ├── Role.java                     # 角色實體
│   │   │   │   └── User.java                     # 用戶實體
│   │   │   ├── repository/                       # 資料存取層
│   │   │   │   ├── CurrencyRepository.java
│   │   │   │   ├── MenuRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   ├── RoleRepository.java
│   │   │   │   └── UserRepository.java
│   │   │   ├── scheduler/                        # 排程任務
│   │   │   │   └── CurrencyRateUpdateScheduler.java  # 匯率自動更新
│   │   │   ├── service/                           # 業務邏輯層
│   │   │   │   ├── CurrencyService.java
│   │   │   │   ├── ExchangeRateApiService.java   # ExchangeRate-API 整合
│   │   │   │   ├── OrderService.java
│   │   │   │   └── UserService.java
│   │   │   └── util/                             # 工具類
│   │   │       └── JwtUtil.java                  # JWT 工具
│   │   └── resources/
│   │       ├── application.properties            # 應用程式配置
│   │       ├── application-docker.properties     # Docker 環境配置
│   │       ├── schema.sql                        # 資料庫 Schema
│   │       └── data.sql                          # 初始資料
│   └── test/                                      # 測試程式碼
│       ├── java/com/example/ordersystem/
│       │   ├── controller/                        # 控制器測試
│       │   ├── integration/                       # 整合測試
│       │   ├── service/                            # 服務層測試
│       │   └── util/                              # 工具類測試
│       └── resources/
│           ├── application-test.properties         # 測試環境配置
│           └── test-data.sql                      # 測試資料
├── frontend/
│   ├── src/
│   │   ├── App.vue                                # 主應用程式組件
│   │   ├── Login.vue                              # 登入組件
│   │   ├── main.js                                # 入口檔案
│   │   └── style.css                             # 樣式檔案
│   ├── index.html                                 # HTML 模板
│   ├── package.json                               # 前端依賴
│   └── vite.config.js                             # Vite 配置
├── docs/                                          # 文檔目錄
│   ├── API_DOCUMENTATION.md                       # API 文檔
│   ├── ARCHITECTURE.md                            # 架構文檔
│   ├── AUTHENTICATION_GUIDE.md                   # 認證指南
│   ├── DATABASE_SCHEMA.md                         # 資料庫設計
│   ├── DEVELOPMENT_GUIDE.md                      # 開發指南
│   ├── openapi.yaml                               # OpenAPI 規範
│   └── testing/                                   # 測試文檔
├── docker-compose-oracle-free.yml                 # Oracle Docker Compose
├── docker-compose-postgres.yml                    # PostgreSQL Docker Compose（備用）
├── Dockerfile                                     # 後端 Dockerfile
├── Dockerfile.frontend                            # 前端 Dockerfile
├── pom.xml                                        # Maven 配置
└── README.md                                      # 本檔案
```

## 資料庫設計

### Orders 表
- `ORDER_ID` (NUMBER, 主鍵) - 訂單 ID
- `USERNAME` (VARCHAR2(50), NOT NULL) - 使用者名稱
- `AMOUNT` (NUMBER(19,2), NOT NULL) - 金額
- `CURRENCY` (VARCHAR2(3), NOT NULL) - 幣別
- `STATUS` (VARCHAR2(20)) - 狀態（PENDING, CONFIRMED, CANCELLED, COMPLETED）
- `DISCOUNT` (NUMBER(5,2)) - 折扣百分比（0-100）
- `FINAL_AMOUNT` (NUMBER(19,2)) - 最終金額
- `CREATED_AT` (TIMESTAMP) - 建立時間
- `UPDATED_AT` (TIMESTAMP) - 更新時間

**索引**:
- `IDX_ORDERS_USERNAME` - 使用者名稱索引
- `IDX_ORDERS_STATUS` - 狀態索引
- `IDX_ORDERS_CURRENCY` - 幣別索引
- `IDX_ORDERS_CREATED_AT` - 建立時間索引（DESC）
- `IDX_ORDERS_ORDER_ID_STR` - 訂單 ID 字串索引（用於 LIKE 查詢）

### Currencies 表
- `CURRENCY_CODE` (VARCHAR2(3), 主鍵) - 幣別代碼
- `RATE_TO_TWD` (NUMBER(19,6), NOT NULL) - 對 TWD 的匯率
- `LAST_UPDATE` (TIMESTAMP) - 最後更新時間

### Users 表
- `USER_ID` (NUMBER, 主鍵) - 用戶 ID
- `USERNAME` (VARCHAR2(50), UNIQUE, NOT NULL) - 使用者名稱
- `PASSWORD` (VARCHAR2(255), NOT NULL) - 密碼（BCrypt 加密）
- `EMAIL` (VARCHAR2(100), UNIQUE, NOT NULL) - 電子郵件
- `ENABLED` (NUMBER(1)) - 是否啟用
- `CREATED_AT` (TIMESTAMP) - 建立時間
- `UPDATED_AT` (TIMESTAMP) - 更新時間

### Roles 表
- `ROLE_ID` (NUMBER, 主鍵) - 角色 ID
- `ROLE_NAME` (VARCHAR2(50), UNIQUE, NOT NULL) - 角色名稱

### User_Roles 表（關聯表）
- `USER_ID` (NUMBER, 外鍵) - 用戶 ID
- `ROLE_ID` (NUMBER, 外鍵) - 角色 ID
- 主鍵：`(USER_ID, ROLE_ID)`

### Menus 表
- `MENU_ID` (NUMBER, 主鍵) - 選單 ID
- `MENU_KEY` (VARCHAR2(50), UNIQUE, NOT NULL) - 選單鍵值
- `LABEL` (VARCHAR2(100), NOT NULL) - 選單標籤
- `ICON` (VARCHAR2(50)) - 圖示
- `ROUTE` (VARCHAR2(50), NOT NULL) - 路由路徑
- `SORT_ORDER` (NUMBER) - 排序順序
- `ENABLED` (NUMBER(1)) - 是否啟用

### Menu_Roles 表（關聯表）
- `MENU_ID` (NUMBER, 外鍵) - 選單 ID
- `ROLE_ID` (NUMBER, 外鍵) - 角色 ID
- 主鍵：`(MENU_ID, ROLE_ID)`

## 設定步驟

### 1. 前置需求
- Java 17+
- Maven 3.6+
- Node.js 18+ 和 npm
- Docker 和 Docker Compose（推薦）
- Oracle Database（或使用 Docker）

### 2. 資料庫設定

#### 方式一：使用 Docker Compose（推薦）

**Windows:**
```bash
start-docker-db.bat
```

**Linux/Mac:**
```bash
chmod +x start-docker-db.sh
./start-docker-db.sh
```

**或手動啟動:**
```bash
docker-compose -f docker-compose-oracle-free.yml up -d
```

等待約 1-2 分鐘讓資料庫完全啟動，可以查看日誌：
```bash
docker logs -f order-currency-oracle
```

看到 `DATABASE IS READY TO USE!` 表示啟動完成。

**Docker 版本的預設連線資訊：**
- URL: `jdbc:oracle:thin:@localhost:1521/FREEPDB1`
- Username: `ORDERSYSTEM`
- Password: `ordersystem123`

#### 方式二：使用本地 Oracle 資料庫

修改 `src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521/FREEPDB1
spring.datasource.username=your_username
spring.datasource.password=your_password
```

詳細 Docker 設定請參考 [DOCKER_SETUP.md](DOCKER_SETUP.md)

### 3. Redis 設定（可選）

如果沒有 Redis，系統仍可運行，但會失去快取功能。

**使用 Docker 啟動 Redis:**
```bash
docker run -d -p 6379:6379 redis:latest
```

### 4. 後端啟動

```bash
# 使用 Maven 啟動
mvn spring-boot:run

# 或使用提供的腳本（Windows）
start-spring-boot.bat

# 或使用提供的腳本（Linux/Mac）
chmod +x start-spring-boot.sh
./start-spring-boot.sh
```

後端服務將運行在 `http://localhost:8080`

**API 文檔（Swagger）**: `http://localhost:8080/swagger-ui.html`

### 5. 前端啟動

```bash
cd frontend
npm install
npm run dev
```

前端服務將運行在 `http://localhost:5173`

### 6. 預設帳號

系統初始化後會自動建立以下帳號：

- **管理員帳號**:
  - Username: `admin`
  - Password: `admin123`
  - Role: `ROLE_ADMIN`

- **一般用戶帳號**:
  - Username: `user`
  - Password: `user123`
  - Role: `ROLE_USER`

## API 端點

### 認證相關
- `POST /api/auth/register` - 註冊新用戶
- `POST /api/auth/login` - 用戶登入（取得 JWT Token）
- `GET /api/auth/menus` - 取得用戶可用的選單（需要認證）

### 訂單相關
- `GET /api/orders` - 取得所有訂單（分頁、搜尋）
  - Query Parameters:
    - `page`: 頁碼（從 0 開始）
    - `size`: 每頁筆數
    - `orderId`: 訂單 ID 搜尋（可選）
- `GET /api/orders/{id}` - 取得單一訂單
- `POST /api/orders` - 新增訂單（需要認證）
- `PUT /api/orders/{id}` - 更新訂單（需要認證）
- `DELETE /api/orders/{id}` - 刪除訂單（需要認證）
- `GET /api/orders/{id}/convert/twd` - 將訂單金額轉換為 TWD
- `GET /api/orders/{id}/convert/{targetCurrency}` - 將訂單金額轉換為指定幣別

### 幣別相關
- `GET /api/currencies` - 取得所有幣別
- `GET /api/currencies/{code}` - 取得單一幣別
- `POST /api/currencies` - 新增幣別（需要認證）
- `PUT /api/currencies/{code}` - 更新幣別（需要認證）
- `PUT /api/currencies/{code}/rate` - 更新匯率（需要認證）
- `DELETE /api/currencies/{code}` - 刪除幣別（需要認證）
- `POST /api/currencies/convert` - 幣別換算
- `POST /api/currencies/refresh` - 從 ExchangeRate-API 更新所有匯率（需要認證）
- `POST /api/currencies/auto-update/enable` - 啟用自動更新匯率（需要認證）
- `POST /api/currencies/auto-update/disable` - 停用自動更新匯率（需要認證）

詳細 API 文檔請參考：
- [API_DOCUMENTATION.md](docs/API_DOCUMENTATION.md)
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI 規範: [docs/openapi.yaml](docs/openapi.yaml)

## 前端功能特色

### 表單驗證
- ✅ 即時驗證（VeeValidate + Yup）
- ✅ 必填欄位標記
- ✅ 錯誤訊息顯示
- ✅ 表單狀態指示器

### 金額處理
- ✅ 自動格式化（千分位）
- ✅ 即時計算預覽
- ✅ 折扣限制（0-100%）

### 使用者體驗
- ✅ 使用者名稱自動完成
- ✅ 折扣滑桿調整
- ✅ 狀態按鈕組選擇
- ✅ 響應式設計
- ✅ 現代化 UI

## 測試

### 執行測試

```bash
# 執行所有測試
mvn test

# 執行特定測試類
mvn test -Dtest=OrderControllerTest

# 使用提供的腳本（Windows）
run-tests.bat
```

### 測試覆蓋率

使用 JaCoCo 生成測試覆蓋率報告：

```bash
mvn clean test jacoco:report
```

報告位置：`target/site/jacoco/index.html`

詳細測試文檔請參考：[docs/testing/TESTING_GUIDE.md](docs/testing/TESTING_GUIDE.md)

## 部署

### Docker 部署

```bash
# 構建後端映像
docker build -t order-currency-backend .

# 構建前端映像
docker build -f Dockerfile.frontend -t order-currency-frontend ./frontend

# 使用 Docker Compose 啟動所有服務
docker-compose up -d
```

### 生產環境配置

修改 `src/main/resources/application.properties` 或使用環境變數：

```properties
# 資料庫連線
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JWT 密鑰（生產環境請使用強密鑰）
jwt.secret=${JWT_SECRET}

# Redis 連線
spring.data.redis.host=${REDIS_HOST}
spring.data.redis.port=${REDIS_PORT}
```

## 相關文檔

- [架構文檔](docs/ARCHITECTURE.md) - 系統架構說明
- [API 文檔](docs/API_DOCUMENTATION.md) - 完整 API 說明
- [認證指南](docs/AUTHENTICATION_GUIDE.md) - JWT 認證使用說明
- [資料庫設計](docs/DATABASE_SCHEMA.md) - 資料庫 Schema 詳細說明
- [開發指南](docs/DEVELOPMENT_GUIDE.md) - 開發規範與最佳實踐
- [Docker 設定](DOCKER_SETUP.md) - Docker 環境設定說明
- [測試指南](docs/testing/TESTING_GUIDE.md) - 測試相關說明

## 已知限制

1. **折扣限制**: 前端已限制折扣不能超過 100% 或為負數
2. **匯率更新**: 自動更新功能依賴外部 API（ExchangeRate-API）
3. **資料庫**: 目前僅支援 Oracle Database

## 授權

MIT License

## 貢獻

歡迎提交 Issue 或 Pull Request！

## 更新日誌

### v1.0.0
- ✅ 完整的訂單管理功能
- ✅ 幣別轉換功能
- ✅ JWT 認證與授權
- ✅ 角色與權限管理
- ✅ 前端表單驗證（VeeValidate）
- ✅ 自動匯率更新
- ✅ API 文檔（Swagger）
- ✅ 完整的測試覆蓋
