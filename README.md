# 訂單與幣別轉換系統

這是一個簡單的訂單管理與幣別轉換系統，使用 Spring Boot 3 + Vue 3 + Oracle 技術棧開發。

## 技術棧

- **後端**: Spring Boot 3 + Spring Data JPA
- **前端**: Vue 3 + Vite
- **資料庫**: Oracle
- **開發工具**: IntelliJ IDEA

## 核心功能

1. **訂單 CRUD** (Order)
   - 新增、查詢、更新、刪除訂單
   - 支援折扣計算
   - 訂單狀態管理

2. **幣別換算** (Currency)
   - 幣別匯率管理
   - 金額轉換功能
   - 訂單金額轉換為 TWD

3. **小折扣/計算邏輯**
   - 自動計算折扣後的最終金額
   - 支援幣別轉換計算

## 專案結構

```
order-currency-system/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/ordersystem/
│       │       ├── OrderCurrencyApplication.java
│       │       ├── model/
│       │       │   ├── Order.java
│       │       │   └── Currency.java
│       │       ├── repository/
│       │       │   ├── OrderRepository.java
│       │       │   └── CurrencyRepository.java
│       │       ├── service/
│       │       │   ├── OrderService.java
│       │       │   └── CurrencyService.java
│       │       └── controller/
│       │           ├── OrderController.java
│       │           └── CurrencyController.java
│       └── resources/
│           └── application.properties
├── frontend/
│   ├── src/
│   │   ├── App.vue
│   │   ├── main.js
│   │   └── style.css
│   ├── index.html
│   ├── package.json
│   └── vite.config.js
└── pom.xml
```

## 資料庫設計

### Order Table
- ORDER_ID (主鍵)
- USERNAME (使用者名稱)
- AMOUNT (金額)
- CURRENCY (幣別)
- STATUS (狀態)
- DISCOUNT (折扣百分比)
- FINAL_AMOUNT (最終金額)
- CREATED_AT (建立時間)
- UPDATED_AT (更新時間)

### Currency Table
- CURRENCY_CODE (主鍵，幣別代碼)
- RATE_TO_TWD (對 TWD 的匯率)
- LAST_UPDATE (最後更新時間)

## 設定步驟

### 1. 資料庫設定（使用 Docker）

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

#### 方式二：使用本地 Oracle 資料庫

修改 `src/main/resources/application.properties`：

```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=your_username
spring.datasource.password=your_password
```

**Docker 版本的預設連線資訊：**
- URL: `jdbc:oracle:thin:@localhost:1521/FREEPDB1`
- Username: `sys`
- Password: `Oracle123`

詳細 Docker 設定請參考 [DOCKER_SETUP.md](DOCKER_SETUP.md)

### 2. 後端啟動

```bash
# 使用 Maven 啟動
mvn spring-boot:run
```

後端服務將運行在 `http://localhost:8080`

### 3. 前端啟動

```bash
cd frontend
npm install
npm run dev
```

前端服務將運行在 `http://localhost:5173`

## API 端點

### 訂單相關
- `GET /api/orders` - 取得所有訂單
- `GET /api/orders/{id}` - 取得單一訂單
- `POST /api/orders` - 新增訂單
- `PUT /api/orders/{id}` - 更新訂單
- `DELETE /api/orders/{id}` - 刪除訂單
- `GET /api/orders/{id}/convert/twd` - 將訂單金額轉換為 TWD
- `GET /api/orders/{id}/convert/{targetCurrency}` - 將訂單金額轉換為指定幣別

### 幣別相關
- `GET /api/currencies` - 取得所有幣別
- `GET /api/currencies/{code}` - 取得單一幣別
- `POST /api/currencies` - 新增幣別
- `PUT /api/currencies/{code}` - 更新幣別
- `PUT /api/currencies/{code}/rate` - 更新匯率
- `DELETE /api/currencies/{code}` - 刪除幣別
- `POST /api/currencies/convert` - 幣別換算

## 已知問題（用於 Debug/Refactor 示範）

1. **OrderService.calculateFinalAmount()**: 沒有檢查折扣是否超過 100%
2. **CurrencyService.convertToTwd()**: 沒有處理匯率為 null 的情況
3. **CurrencyService.convertCurrency()**: 除法運算可能有精度問題
4. **OrderService**: 使用 `==` 比較 BigDecimal（應該使用 `compareTo()`）

## 開發建議

這個專案設計時故意留了一些小 bug 和不優化的地方，可以用來示範：
- Debug 技巧
- Code Refactoring
- 理解模組架構
- 最佳實踐改進

## 授權

MIT License

