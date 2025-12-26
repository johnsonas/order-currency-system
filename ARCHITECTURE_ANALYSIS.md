# 專案架構分析（基於現有程式碼）

## 📋 專案概述

**專案名稱：** Order Currency System（訂單與幣別轉換系統）  
**技術棧：** Spring Boot 3.2.0 + Vue 3 + Oracle Database  
**Java 版本：** 17  
**架構模式：** 前後端分離 + 三層架構

---

## 🏗️ 整體架構

```
┌─────────────────────────────────────────────────────────┐
│                     前端層 (Frontend)                    │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Vue 3 + Vite                                    │  │
│  │  - App.vue (單頁應用)                            │  │
│  │  - axios (HTTP 客戶端)                           │  │
│  │  - Port: 5173                                    │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
                        ↕ HTTP REST API
┌─────────────────────────────────────────────────────────┐
│                   後端層 (Backend)                        │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Spring Boot 3.2.0                              │  │
│  │  Port: 8080                                      │  │
│  │                                                   │  │
│  │  ┌──────────────┐  ┌──────────────┐            │  │
│  │  │ Controller   │  │ Controller   │            │  │
│  │  │ Order        │  │ Currency     │            │  │
│  │  └──────┬───────┘  └──────┬───────┘            │  │
│  │         │                  │                     │  │
│  │  ┌──────▼───────┐  ┌──────▼───────┐            │  │
│  │  │ Service      │  │ Service      │            │  │
│  │  │ OrderService │  │ Currency     │            │  │
│  │  └──────┬───────┘  └──────┬───────┘            │  │
│  │         │                  │                     │  │
│  │  ┌──────▼───────┐  ┌──────▼───────┐            │  │
│  │  │ Repository   │  │ Repository   │            │  │
│  │  │ Order        │  │ Currency     │            │  │
│  │  └──────┬───────┘  └──────┬───────┘            │  │
│  └─────────┼──────────────────┼─────────────────────┘  │
└────────────┼──────────────────┼─────────────────────────┘
             │                  │
             ▼                  ▼
┌─────────────────────────────────────────────────────────┐
│                   資料庫層 (Database)                     │
│  ┌──────────────────────────────────────────────────┐  │
│  │  Oracle Database                                 │  │
│  │  - ORDERS 表                                     │  │
│  │  - CURRENCIES 表                                │  │
│  │  - ORDER_SEQ 序號                               │  │
│  └──────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────┘
```

---

## 📦 後端架構（Java Spring Boot）

### Package 結構

```
com.example.ordersystem
│
├── OrderCurrencyApplication.java  (主應用程式入口)
│
├── controller/                     (控制器層 - REST API)
│   ├── OrderController.java        (訂單 API)
│   └── CurrencyController.java     (幣別 API)
│
├── service/                        (業務邏輯層)
│   ├── OrderService.java           (訂單業務邏輯)
│   └── CurrencyService.java         (幣別業務邏輯)
│
├── repository/                      (資料存取層)
│   ├── OrderRepository.java        (訂單資料存取)
│   └── CurrencyRepository.java     (幣別資料存取)
│
└── model/                          (實體模型層)
    ├── Order.java                  (訂單實體)
    └── Currency.java                (幣別實體)
```

### 技術棧分析

#### 1. **框架與版本**
- **Spring Boot:** 3.2.0
- **Java:** 17
- **構建工具:** Maven
- **ORM:** Spring Data JPA + Hibernate

#### 2. **依賴套件**
```xml
- spring-boot-starter-web          (Web 功能)
- spring-boot-starter-data-jpa     (JPA 資料存取)
- spring-boot-starter-validation   (資料驗證)
- ojdbc11                           (Oracle JDBC Driver)
- lombok                            (可選，簡化程式碼)
- spring-boot-devtools             (開發工具)
```

#### 3. **資料庫**
- **類型:** Oracle Database
- **連接:** JDBC Thin Driver
- **ORM:** JPA/Hibernate
- **DDL 模式:** update (自動更新表結構)

---

## 🎨 前端架構（Vue.js）

### 檔案結構

```
frontend/
├── index.html              (HTML 入口)
├── package.json            (依賴管理)
├── vite.config.js          (Vite 配置)
└── src/
    ├── main.js             (Vue 應用入口)
    ├── App.vue             (主元件 - 單頁應用)
    └── style.css           (樣式表)
```

### 技術棧分析

#### 1. **框架與版本**
- **Vue:** 3.3.4
- **構建工具:** Vite 5.0.0
- **HTTP 客戶端:** axios 1.6.2

#### 2. **應用架構**
- **模式:** 單頁應用 (SPA)
- **狀態管理:** Vue 3 Composition API (data + methods)
- **路由:** 無路由（單頁）
- **API 通訊:** axios 直接呼叫 REST API

---

## 🔄 資料流程分析

### 請求流程範例：新增訂單

```
1. 使用者操作
   └─> 前端：App.vue - saveOrder()
       └─> axios.post('/api/orders', orderData)

2. HTTP 請求
   └─> POST http://localhost:8080/api/orders
       Headers: Content-Type: application/json
       Body: { username, amount, currency, discount, status }

3. 後端處理
   ├─> OrderController.createOrder()
   │   ├─> @Valid 驗證 (Bean Validation)
   │   └─> OrderService.createOrder()
   │       ├─> calculateFinalAmount() - 計算最終金額
   │       └─> OrderRepository.save()
   │           └─> JPA Entity 生命週期
   │               └─> @PrePersist onCreate() - 設定時間戳
   │
   └─> 資料庫操作
       └─> INSERT INTO ORDERS ...

4. 回應流程
   └─> ResponseEntity<Order> (HTTP 201 Created)
       └─> 前端接收回應
           └─> closeModal() + loadOrders() - 更新畫面
```

---

## 📊 各層職責分析

### Controller 層（控制器）
**檔案：** `OrderController.java`, `CurrencyController.java`

**職責：**
- ✅ 接收 HTTP 請求
- ✅ 參數驗證（@Valid）
- ✅ 呼叫 Service 層
- ✅ 返回 HTTP 回應
- ❌ 不包含業務邏輯
- ❌ 異常處理不統一（各方法自行 try-catch）

**特點：**
- 使用 `@RestController`（自動 JSON 序列化）
- 使用 `@CrossOrigin` 處理 CORS（每個 Controller 重複）
- 直接返回 Entity 物件（未使用 DTO）

---

### Service 層（業務邏輯）
**檔案：** `OrderService.java`, `CurrencyService.java`

**職責：**
- ✅ 業務邏輯處理
- ✅ 資料轉換與計算
- ✅ 呼叫 Repository 層
- ✅ 事務管理（@Transactional）
- ✅ 跨 Service 呼叫（OrderService → CurrencyService）

**特點：**
- 使用 `@Service` 標註
- 使用 `@Transactional` 確保資料一致性
- 包含業務計算邏輯（折扣計算、匯率轉換）
- 有「故意留」的 bug 註解（教學用途）

---

### Repository 層（資料存取）
**檔案：** `OrderRepository.java`, `CurrencyRepository.java`

**職責：**
- ✅ 資料庫 CRUD 操作
- ✅ 自訂查詢方法
- ✅ 使用 Spring Data JPA 方法命名規則
- ✅ 支援原生 SQL 查詢

**特點：**
- 繼承 `JpaRepository<Entity, ID>`
- 方法命名自動生成查詢（如 `findByUsername`）
- 使用 `@Query` 自訂 JPQL 查詢
- 使用 `nativeQuery = true` 執行原生 SQL

---

### Model 層（實體模型）
**檔案：** `Order.java`, `Currency.java`

**職責：**
- ✅ 定義資料庫表結構
- ✅ JPA 實體映射
- ✅ 資料驗證規則（Bean Validation）
- ✅ 自動時間戳設定（@PrePersist, @PreUpdate）

**特點：**
- 使用 `@Entity` 標註
- 使用 `@Table` 指定表名
- 使用 `@Id`, `@GeneratedValue` 定義主鍵
- 使用 `@Column` 定義欄位映射
- 使用 Bean Validation（@NotBlank, @NotNull, @Positive）
- 使用 `@PrePersist` 自動設定建立時間

---

## 🌐 API 端點分析

### Order API (`/api/orders`)

| 方法 | 路徑 | 功能 | 參數 |
|------|------|------|------|
| GET | `/api/orders` | 取得所有訂單 | `?searchOrderId=xxx` (可選) |
| GET | `/api/orders/{id}` | 取得單一訂單 | `id` (路徑參數) |
| GET | `/api/orders/username/{username}` | 依使用者查詢 | `username` (路徑參數) |
| GET | `/api/orders/status/{status}` | 依狀態查詢 | `status` (路徑參數) |
| POST | `/api/orders` | 建立訂單 | Order 物件 (Body) |
| PUT | `/api/orders/{id}` | 更新訂單 | `id` + Order 物件 |
| DELETE | `/api/orders/{id}` | 刪除訂單 | `id` (路徑參數) |
| GET | `/api/orders/{id}/convert/twd` | 轉換為 TWD | `id` (路徑參數) |
| GET | `/api/orders/{id}/convert/{targetCurrency}` | 轉換為指定幣別 | `id` + `targetCurrency` |

### Currency API (`/api/currencies`)

| 方法 | 路徑 | 功能 | 參數 |
|------|------|------|------|
| GET | `/api/currencies` | 取得所有幣別 | 無 |
| GET | `/api/currencies/{code}` | 取得單一幣別 | `code` (路徑參數) |
| POST | `/api/currencies` | 建立幣別 | Currency 物件 (Body) |
| PUT | `/api/currencies/{code}` | 更新幣別 | `code` + Currency 物件 |
| PUT | `/api/currencies/{code}/rate` | 更新匯率 | `code` + `newRate` (Body) |
| DELETE | `/api/currencies/{code}` | 刪除幣別 | `code` (路徑參數) |
| POST | `/api/currencies/convert` | 幣別轉換 | `amount`, `sourceCurrency`, `targetCurrency` (Query) |

---

## 💾 資料庫設計

### 表結構

#### ORDERS 表
```sql
- ORDER_ID (NUMBER, PRIMARY KEY, SEQUENCE)
- USERNAME (VARCHAR2(50), NOT NULL)
- AMOUNT (NUMBER(19,2), NOT NULL)
- CURRENCY (VARCHAR2(3), NOT NULL)
- STATUS (VARCHAR2(20), DEFAULT 'PENDING')
- DISCOUNT (NUMBER(5,2), DEFAULT 0)
- FINAL_AMOUNT (NUMBER(19,2))
- CREATED_AT (TIMESTAMP)
- UPDATED_AT (TIMESTAMP)
```

#### CURRENCIES 表
```sql
- CURRENCY_CODE (VARCHAR2(3), PRIMARY KEY)
- RATE_TO_TWD (NUMBER(19,6), NOT NULL)
- LAST_UPDATE (TIMESTAMP)
```

#### 索引
```sql
- IDX_ORDERS_USERNAME (USERNAME)
- IDX_ORDERS_STATUS (STATUS)
- IDX_ORDERS_CURRENCY (CURRENCY)
- IDX_ORDERS_CREATED_AT (CREATED_AT DESC)
- IDX_ORDERS_ORDER_ID_STR (TO_CHAR(ORDER_ID)) - 函數索引
```

---

## 🔍 架構特點總結

### ✅ 優點

1. **清晰的分層架構**
   - Controller → Service → Repository → Database
   - 職責分離明確

2. **標準 Spring Boot 實踐**
   - 使用 Spring Data JPA
   - 使用 Bean Validation
   - 使用 @Transactional

3. **前後端分離**
   - RESTful API 設計
   - Vue 3 單頁應用

4. **資料庫優化**
   - 索引規劃
   - 函數索引支援搜尋

### ⚠️ 可改進之處

1. **異常處理**
   - 各 Controller 方法自行 try-catch
   - 缺少統一異常處理器

2. **API 設計**
   - 直接返回 Entity（應使用 DTO）
   - 缺少統一回應格式

3. **配置**
   - CORS 配置重複（每個 Controller）
   - 缺少統一配置類

4. **型別安全**
   - 狀態使用字串而非列舉

---

## 📈 資料流向圖

```
前端 (Vue 3)
  │
  ├─> axios.post('/api/orders', data)
  │
  ▼
後端 (Spring Boot)
  │
  ├─> OrderController.createOrder()
  │   │
  │   ├─> @Valid 驗證
  │   │
  │   └─> OrderService.createOrder()
  │       │
  │       ├─> calculateFinalAmount() - 業務邏輯
  │       │
  │       └─> OrderRepository.save()
  │           │
  │           └─> JPA Entity
  │               │
  │               ├─> @PrePersist onCreate()
  │               │
  │               └─> Hibernate
  │                   │
  │                   ▼
資料庫 (Oracle)
  │
  └─> INSERT INTO ORDERS ...
```

---

## 🎯 總結

這是一個**標準的 Spring Boot + Vue 3 前後端分離專案**，採用**三層架構模式**：

1. **前端：** Vue 3 SPA，使用 axios 呼叫 REST API
2. **後端：** Spring Boot REST API，三層架構（Controller-Service-Repository）
3. **資料庫：** Oracle Database，使用 JPA/Hibernate ORM

架構清晰、職責分明，符合 Spring Boot 最佳實踐的基本要求。

