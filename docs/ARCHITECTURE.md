# 專案架構說明

## 概述

本專案採用 **分層架構（Layered Architecture）** 設計模式，結合 **MVC（Model-View-Controller）** 模式，使用 Spring Boot 3 作為後端框架，Vue 3 作為前端框架，Oracle 作為資料庫。

## 整體架構圖

```
┌─────────────────────────────────────────────────────────────┐
│                        前端層 (Frontend)                      │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Vue 3 + Vite                                         │  │
│  │  - App.vue (主要組件)                                  │  │
│  │  - main.js (入口文件)                                  │  │
│  │  - style.css (樣式)                                   │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↕ HTTP/REST API
┌─────────────────────────────────────────────────────────────┐
│                      後端層 (Backend)                        │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Controller 層 (API 端點)                             │  │
│  │  - OrderController                                    │  │
│  │  - CurrencyController                                 │  │
│  └──────────────────────────────────────────────────────┘  │
│                            ↕                                │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Service 層 (業務邏輯)                                │  │
│  │  - OrderService                                       │  │
│  │  - CurrencyService                                    │  │
│  └──────────────────────────────────────────────────────┘  │
│                            ↕                                │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Repository 層 (資料存取)                              │  │
│  │  - OrderRepository                                    │  │
│  │  - CurrencyRepository                                 │  │
│  └──────────────────────────────────────────────────────┘  │
│                            ↕                                │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Model 層 (實體類別)                                   │  │
│  │  - Order                                              │  │
│  │  - Currency                                           │  │
│  │  - CurrencyCode (Enum)                                │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                            ↕ JPA/Hibernate
┌─────────────────────────────────────────────────────────────┐
│                      資料庫層 (Database)                      │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Oracle Database                                      │  │
│  │  - ORDERS 表                                          │  │
│  │  - CURRENCIES 表                                      │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

## 技術棧

### 後端技術
- **框架**: Spring Boot 3.2.0
- **Java 版本**: Java 17
- **ORM**: Spring Data JPA + Hibernate
- **資料庫**: Oracle Database
- **建置工具**: Maven
- **驗證**: Jakarta Validation
- **開發工具**: Lombok (可選)

### 前端技術
- **框架**: Vue 3
- **建置工具**: Vite
- **HTTP 客戶端**: Fetch API

### 測試技術
- **單元測試**: JUnit 5 + Mockito
- **整合測試**: Spring Boot Test + H2 Database
- **測試覆蓋率**: JaCoCo

## 分層架構詳解

### 1. Controller 層（控制器層）

**職責**：
- 接收 HTTP 請求
- 參數驗證與轉換
- 呼叫 Service 層處理業務邏輯
- 返回 HTTP 回應

**位置**: `src/main/java/com/example/ordersystem/controller/`

**主要類別**：
- `OrderController`: 處理訂單相關的 API 請求
- `CurrencyController`: 處理幣別相關的 API 請求

**特點**：
- 使用 `@RestController` 標註，自動處理 JSON 序列化
- 使用 `@CrossOrigin` 處理 CORS 跨域請求
- 使用 `@RequestMapping` 定義 API 路徑前綴
- 使用 `@Valid` 進行請求參數驗證

**範例**：
```java
@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "http://localhost:5173")
public class OrderController {
    @Autowired
    private OrderService orderService;
    
    @GetMapping
    public ResponseEntity<List<Order>> getAllOrders() {
        // ...
    }
}
```

### 2. Service 層（服務層）

**職責**：
- 實現業務邏輯
- 事務管理
- 資料驗證與處理
- 協調多個 Repository 的操作

**位置**: `src/main/java/com/example/ordersystem/service/`

**主要類別**：
- `OrderService`: 訂單業務邏輯（CRUD、搜尋、折扣計算）
- `CurrencyService`: 幣別業務邏輯（匯率管理、幣別轉換）

**特點**：
- 使用 `@Service` 標註，由 Spring 管理
- 使用 `@Transactional` 管理事務
- 包含完整的 null 檢查和異常處理
- 使用 `BigDecimal` 進行精確的金融計算

**範例**：
```java
@Service
@Transactional
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    
    @Autowired
    private CurrencyService currencyService;
    
    public Order createOrder(Order order) {
        // 業務邏輯處理
    }
}
```

### 3. Repository 層（資料存取層）

**職責**：
- 資料庫 CRUD 操作
- 自定義查詢方法
- 資料查詢優化

**位置**: `src/main/java/com/example/ordersystem/repository/`

**主要介面**：
- `OrderRepository`: 訂單資料存取
- `CurrencyRepository`: 幣別資料存取

**特點**：
- 繼承 `JpaRepository<T, ID>`，自動提供基本 CRUD 方法
- 使用 Spring Data JPA 方法命名規則自動生成查詢
- 支援 `@Query` 自定義 SQL/JPQL 查詢
- 使用 `@Repository` 標註

**範例**：
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUsername(String username);
    
    @Query("SELECT o FROM Order o WHERE o.currency = :currency")
    List<Order> findByCurrency(@Param("currency") CurrencyCode currency);
}
```

### 4. Model 層（實體層）

**職責**：
- 定義資料庫實體結構
- 資料驗證規則
- 生命週期回調處理

**位置**: `src/main/java/com/example/ordersystem/model/`

**主要類別**：
- `Order`: 訂單實體
- `Currency`: 幣別實體
- `CurrencyCode`: 幣別代碼枚舉（TWD, USD, EUR, JPY, CNY）

**特點**：
- 使用 `@Entity` 標註為 JPA 實體
- 使用 `@Table` 指定資料表名稱
- 使用 `@Id` 和 `@GeneratedValue` 定義主鍵
- 使用 `@Column` 定義欄位屬性
- 使用 Jakarta Validation 進行欄位驗證
- 使用 `@PrePersist` 和 `@PreUpdate` 處理時間戳記

**範例**：
```java
@Entity
@Table(name = "ORDERS")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
    @SequenceGenerator(name = "order_seq", sequenceName = "ORDER_SEQ")
    private Long orderId;
    
    @NotBlank(message = "使用者名稱不能為空")
    private String username;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

## 資料流程

### 請求流程（Request Flow）

```
1. 前端發送 HTTP 請求
   ↓
2. Controller 接收請求，進行參數驗證
   ↓
3. Controller 呼叫 Service 層方法
   ↓
4. Service 層處理業務邏輯，可能需要：
   - 呼叫 Repository 查詢資料
   - 呼叫其他 Service 進行協作
   - 進行資料計算和轉換
   ↓
5. Repository 執行資料庫操作（透過 JPA/Hibernate）
   ↓
6. 資料庫返回結果
   ↓
7. Repository 返回實體物件
   ↓
8. Service 返回處理結果
   ↓
9. Controller 將結果封裝為 ResponseEntity
   ↓
10. 前端接收 JSON 回應
```

### 回應流程（Response Flow）

```
資料庫 → Repository → Service → Controller → JSON → 前端
```

## 資料庫設計

### 表結構

#### ORDERS 表
- `ORDER_ID` (主鍵，序列生成)
- `USERNAME` (使用者名稱)
- `AMOUNT` (金額)
- `CURRENCY` (幣別代碼)
- `STATUS` (訂單狀態)
- `DISCOUNT` (折扣百分比)
- `FINAL_AMOUNT` (最終金額)
- `CREATED_AT` (建立時間)
- `UPDATED_AT` (更新時間)

#### CURRENCIES 表
- `CURRENCY_CODE` (主鍵，幣別代碼)
- `RATE_TO_TWD` (對 TWD 的匯率)
- `LAST_UPDATE` (最後更新時間)

### 索引設計
- `ORDERS.USERNAME` 索引（提升查詢效能）
- `ORDERS.STATUS` 索引（提升查詢效能）
- `ORDERS.CREATED_AT` 索引（提升排序效能）

## 設計模式

### 1. 分層架構模式（Layered Architecture）
- 明確的職責分離
- 每層只依賴下層，不依賴上層
- 便於測試和維護

### 2. 依賴注入（Dependency Injection）
- 使用 Spring 的 `@Autowired` 進行依賴注入
- 降低耦合度，提高可測試性

### 3. Repository 模式
- 封裝資料存取邏輯
- 提供統一的資料存取介面
- 隱藏資料庫實作細節

### 4. Builder 模式（測試中）
- `OrderTestDataBuilder` 和 `CurrencyTestDataBuilder`
- 簡化測試資料建立

## 安全性設計

### 1. 輸入驗證
- Controller 層使用 `@Valid` 驗證請求參數
- Model 層使用 Jakarta Validation 註解
- Service 層進行業務邏輯驗證

### 2. Null 安全
- Service 層包含完整的 null 檢查
- 使用 `Optional` 處理可能為空的返回值
- 明確的異常訊息

### 3. 事務管理
- Service 層使用 `@Transactional` 確保資料一致性
- 自動回滾機制

## 測試架構

### 測試分層

```
src/test/java/
├── controller/          # Controller 單元測試
│   └── OrderControllerTest.java
├── service/            # Service 單元測試
│   ├── OrderServiceTest.java
│   └── CurrencyServiceTest.java
├── integration/        # 整合測試
│   └── OrderControllerIntegrationTest.java
└── util/               # 測試工具類
    ├── OrderTestDataBuilder.java
    ├── CurrencyTestDataBuilder.java
    └── TestUtils.java
```

### 測試策略
- **單元測試**: 使用 Mockito 模擬依賴
- **整合測試**: 使用 H2 記憶體資料庫
- **測試覆蓋率**: 使用 JaCoCo 生成報告

## 配置管理

### 配置檔案
- `application.properties`: 主要配置（資料庫連線、JPA 設定）
- `application-docker.properties`: Docker 環境配置
- `application-test.properties`: 測試環境配置（H2 資料庫）

### 環境變數支援
- 支援透過環境變數覆蓋配置
- Docker 環境自動切換配置

## API 設計

### RESTful 設計原則
- 使用標準 HTTP 方法（GET, POST, PUT, DELETE）
- 資源導向的 URL 設計
- 統一的回應格式（ResponseEntity）

### API 端點結構
```
/api/orders          # 訂單資源
/api/currencies      # 幣別資源
```

### CORS 設定
- 允許前端 `http://localhost:5173` 跨域請求
- 使用 `@CrossOrigin` 註解

## 專案結構

```
order-currency-system/
├── src/
│   ├── main/
│   │   ├── java/com/example/ordersystem/
│   │   │   ├── controller/      # Controller 層
│   │   │   ├── service/         # Service 層
│   │   │   ├── repository/      # Repository 層
│   │   │   ├── model/           # Model 層
│   │   │   └── OrderCurrencyApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── schema.sql       # 資料庫結構
│   │       └── data.sql         # 初始資料
│   └── test/                    # 測試程式碼
├── frontend/                     # 前端專案
├── docs/                         # 文件資料夾
├── pom.xml                       # Maven 配置
└── README.md                     # 專案說明
```

## 優點與特色

### 1. 清晰的職責分離
- 每層職責明確，易於理解和維護

### 2. 高可測試性
- 分層設計便於單元測試
- 使用 Mockito 模擬依賴

### 3. 高可擴展性
- 易於新增新的功能模組
- 遵循開閉原則

### 4. 安全性
- 完整的輸入驗證
- Null 安全處理
- 事務管理

### 5. 文件完善
- API 文件（OpenAPI）
- 資料庫設計文件
- 開發指南
- 測試指南

## 改進建議

### 1. 引入 DTO/VO 層
- 目前直接使用 Model 作為 API 回應
- 建議引入 DTO 或 VO 層，隱藏內部實作細節

### 2. 統一異常處理
- 建議使用 `@ControllerAdvice` 統一處理異常
- 提供統一的錯誤回應格式

### 3. 日誌管理
- 建議使用 SLF4J + Logback
- 添加結構化日誌

### 4. API 版本控制
- 建議在 URL 中加入版本號（如 `/api/v1/orders`）

### 5. 快取機制
- 對於不常變動的資料（如幣別匯率），可考慮加入快取

## 總結

本專案採用標準的 Spring Boot 分層架構，結構清晰、職責分明，適合中小型專案開發。架構設計遵循 SOLID 原則，具有良好的可維護性和可擴展性。透過完善的測試和文件，確保專案品質和可持續發展。

