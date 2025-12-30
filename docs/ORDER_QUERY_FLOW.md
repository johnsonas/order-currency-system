# 依據訂單ID查詢訂單 - 完整流程說明

## 概述

本文檔詳細說明「依據訂單ID查詢訂單」功能的完整執行流程，包括前端 Vue 頁面、後端函數調用鏈和資料庫操作。

## 流程圖

```
┌─────────────────────────────────────────────────────────────┐
│  前端 Vue 頁面 (App.vue)                                     │
│                                                              │
│  方式一：搜尋框輸入訂單ID                                      │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ 1. 用戶在搜尋框輸入訂單ID                              │  │
│  │    <input v-model="searchOrderId" @input="debounceSearch">│
│  └──────────────────────────────────────────────────────┘  │
│                        ↓                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ 2. debounceSearch() 函數                              │  │
│  │    - 防抖處理（500ms 延遲）                            │  │
│  │    - 調用 loadOrders(searchOrderId)                  │  │
│  └──────────────────────────────────────────────────────┘  │
│                        ↓                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ 3. loadOrders(searchOrderId) 函數                    │  │
│  │    - 使用 axios 發送 GET 請求                          │  │
│  │    - URL: http://localhost:8080/api/orders           │  │
│  │    - 參數: { searchOrderId: "123" }                  │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                        ↓ HTTP GET Request
┌─────────────────────────────────────────────────────────────┐
│  後端 Spring Boot                                            │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Controller 層: OrderController                        │  │
│  │                                                       │  │
│  │ 4. getAllOrders(@RequestParam String searchOrderId)   │  │
│  │    - 接收 HTTP 請求                                    │  │
│  │    - 路徑: GET /api/orders                           │  │
│  │    - 參數: searchOrderId (可選)                       │  │
│  │    - 判斷是否有 searchOrderId 參數                     │  │
│  │    - 如果有：調用 orderService.searchOrdersByOrderId()│  │
│  │    - 如果沒有：調用 orderService.getAllOrders()       │  │
│  └──────────────────────────────────────────────────────┘  │
│                        ↓                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Service 層: OrderService                              │  │
│  │                                                       │  │
│  │ 5. searchOrdersByOrderId(String orderId)             │  │
│  │    - 檢查 orderId 是否為空                             │  │
│  │    - 嘗試將 orderId 轉換為 Long（精確匹配）            │  │
│  │    - 如果成功：調用 orderRepository.findByOrderId()    │  │
│  │    - 如果失敗：調用 orderRepository.searchByOrderIdContaining()│
│  │    - 返回訂單列表                                      │  │
│  └──────────────────────────────────────────────────────┘  │
│                        ↓                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Repository 層: OrderRepository                       │  │
│  │                                                       │  │
│  │ 6a. findByOrderId(Long orderId)                      │  │
│  │     - 精確匹配查詢                                     │  │
│  │     - 返回 Optional<Order>                           │  │
│  │                                                       │  │
│  │ 6b. searchByOrderIdContaining(String orderId)         │  │
│  │     - 模糊搜尋（部分匹配）                             │  │
│  │     - SQL: SELECT * FROM ORDERS                       │  │
│  │            WHERE TO_CHAR(ORDER_ID) LIKE '%' || :orderId || '%'│
│  │            ORDER BY CREATED_AT DESC                   │  │
│  │     - 返回 List<Order>                                │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                        ↓ JPA/Hibernate
┌─────────────────────────────────────────────────────────────┐
│  資料庫 Oracle                                               │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ 7. 執行 SQL 查詢                                       │  │
│  │                                                       │  │
│  │ 精確匹配：                                             │  │
│  │ SELECT * FROM ORDERS WHERE ORDER_ID = ?              │  │
│  │                                                       │  │
│  │ 模糊搜尋：                                             │  │
│  │ SELECT * FROM ORDERS                                 │  │
│  │ WHERE TO_CHAR(ORDER_ID) LIKE '%123%'                 │  │
│  │ ORDER BY CREATED_AT DESC                             │  │
│  └──────────────────────────────────────────────────────┘  │
│                        ↓                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ 8. 返回查詢結果                                        │  │
│  │    - 資料庫返回符合條件的訂單記錄                       │  │
│  │    - Hibernate 將結果映射為 Order 實體物件              │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                        ↑
┌─────────────────────────────────────────────────────────────┐
│  後端回應流程（反向）                                         │
│                                                              │
│  Repository → Service → Controller → JSON Response          │
└─────────────────────────────────────────────────────────────┘
                        ↑ HTTP Response (JSON)
┌─────────────────────────────────────────────────────────────┐
│  前端接收回應                                                 │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ 9. axios 接收 JSON 回應                                │  │
│  │    - response.data 包含訂單陣列                        │  │
│  └──────────────────────────────────────────────────────┘  │
│                        ↓                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ 10. this.orders = response.data                      │  │
│  │     - 更新 Vue 的 orders 資料                         │  │
│  │     - Vue 自動重新渲染表格                             │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 方式二：直接查詢單一訂單（通過訂單ID路徑參數）

```
┌─────────────────────────────────────────────────────────────┐
│  前端 Vue 頁面 (App.vue)                                     │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ 1. 調用 API（例如：編輯訂單時）                        │  │
│  │    axios.get(`/api/orders/${orderId}`)              │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                        ↓ HTTP GET Request
┌─────────────────────────────────────────────────────────────┐
│  後端 Spring Boot                                            │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Controller 層: OrderController                        │  │
│  │                                                       │  │
│  │ 2. getOrderById(@PathVariable Long id)                │  │
│  │    - 接收 HTTP 請求                                    │  │
│  │    - 路徑: GET /api/orders/{id}                      │  │
│  │    - 路徑參數: id (訂單ID)                             │  │
│  │    - 調用 orderService.getOrderById(id)               │  │
│  └──────────────────────────────────────────────────────┘  │
│                        ↓                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Service 層: OrderService                              │  │
│  │                                                       │  │
│  │ 3. getOrderById(Long orderId)                         │  │
│  │    - 調用 orderRepository.findById(orderId)           │  │
│  │    - 返回 Optional<Order>                             │  │
│  └──────────────────────────────────────────────────────┘  │
│                        ↓                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ Repository 層: OrderRepository                         │  │
│  │                                                       │  │
│  │ 4. findById(Long id)                                 │  │
│  │    - JpaRepository 提供的方法                         │  │
│  │    - 執行: SELECT * FROM ORDERS WHERE ORDER_ID = ?   │  │
│  │    - 返回 Optional<Order>                             │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                        ↓ JPA/Hibernate
┌─────────────────────────────────────────────────────────────┐
│  資料庫 Oracle                                               │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ 5. 執行 SQL 查詢                                       │  │
│  │    SELECT * FROM ORDERS WHERE ORDER_ID = ?            │  │
│  └──────────────────────────────────────────────────────┘  │
│                        ↓                                    │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ 6. 返回查詢結果                                        │  │
│  │    - 如果找到：返回單一 Order 實體                      │  │
│  │    - 如果沒找到：返回空                                 │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                        ↑
┌─────────────────────────────────────────────────────────────┐
│  後端回應流程（反向）                                         │
│                                                              │
│  Repository → Service → Controller                          │
│  - 如果找到：ResponseEntity.ok(order)                       │
│  - 如果沒找到：ResponseEntity.notFound()                    │
└─────────────────────────────────────────────────────────────┘
                        ↑ HTTP Response (JSON)
┌─────────────────────────────────────────────────────────────┐
│  前端接收回應                                                 │
│                                                              │
│  ┌──────────────────────────────────────────────────────┐  │
│  │ 7. 接收 JSON 回應                                      │  │
│  │    - 成功：response.data 包含訂單物件                  │  │
│  │    - 失敗：404 Not Found                              │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

---

## 詳細函數調用鏈

### 方式一：搜尋框查詢（支援精確和模糊搜尋）

#### 前端函數調用順序

1. **`debounceSearch()`** (App.vue:178-186)
   ```javascript
   debounceSearch() {
     if (this.searchTimer) {
       clearTimeout(this.searchTimer)
     }
     this.searchTimer = setTimeout(() => {
       this.loadOrders(this.searchOrderId)
     }, 500)
   }
   ```
   - **功能**: 防抖處理，避免頻繁請求
   - **延遲**: 500ms
   - **調用**: `loadOrders(this.searchOrderId)`

2. **`loadOrders(searchOrderId)`** (App.vue:166-177)
   ```javascript
   async loadOrders(searchOrderId = null) {
     const params = searchOrderId && searchOrderId.trim() 
       ? { searchOrderId: searchOrderId.trim() } 
       : {}
     const response = await axios.get(`${API_BASE_URL}/orders`, { params })
     this.orders = response.data
   }
   ```
   - **功能**: 發送 HTTP GET 請求
   - **URL**: `http://localhost:8080/api/orders`
   - **參數**: `{ searchOrderId: "123" }`
   - **更新**: `this.orders = response.data`

#### 後端函數調用順序

3. **`OrderController.getAllOrders()`** (OrderController.java:24-32)
   ```java
   @GetMapping
   public ResponseEntity<List<Order>> getAllOrders(
       @RequestParam(required = false) String searchOrderId) {
       if (searchOrderId != null && !searchOrderId.trim().isEmpty()) {
           List<Order> orders = orderService.searchOrdersByOrderId(searchOrderId);
           return ResponseEntity.ok(orders);
       }
       List<Order> orders = orderService.getAllOrders();
       return ResponseEntity.ok(orders);
   }
   ```
   - **功能**: 接收 HTTP 請求，判斷是否有搜尋參數
   - **路徑**: `GET /api/orders`
   - **調用**: `orderService.searchOrdersByOrderId(searchOrderId)`

4. **`OrderService.searchOrdersByOrderId()`** (OrderService.java:83-102)
   ```java
   public List<Order> searchOrdersByOrderId(String orderId) {
       if (orderId == null || orderId.trim().isEmpty()) {
           return getAllOrders();
       }
       String trimmedId = orderId.trim();
       
       // 先嘗試精確匹配
       try {
           Long exactId = Long.parseLong(trimmedId);
           Optional<Order> exactOrder = orderRepository.findByOrderId(exactId);
           if (exactOrder.isPresent()) {
               return List.of(exactOrder.get());
           }
       } catch (NumberFormatException e) {
           // 如果不是數字，使用模糊搜尋
       }
       
       // 模糊搜尋
       return orderRepository.searchByOrderIdContaining(trimmedId);
   }
   ```
   - **功能**: 處理搜尋邏輯
   - **策略**: 
     - 先嘗試精確匹配（如果輸入是完整數字）
     - 如果精確匹配失敗或輸入不是數字，使用模糊搜尋
   - **調用**: 
     - `orderRepository.findByOrderId(exactId)` (精確匹配)
     - `orderRepository.searchByOrderIdContaining(trimmedId)` (模糊搜尋)

5. **`OrderRepository.findByOrderId()`** (OrderRepository.java:23)
   ```java
   Optional<Order> findByOrderId(Long orderId);
   ```
   - **功能**: 精確匹配查詢
   - **SQL**: `SELECT * FROM ORDERS WHERE ORDER_ID = ?`
   - **返回**: `Optional<Order>`

6. **`OrderRepository.searchByOrderIdContaining()`** (OrderRepository.java:25-26)
   ```java
   @Query(value = "SELECT * FROM ORDERS WHERE TO_CHAR(ORDER_ID) LIKE '%' || :orderId || '%' ORDER BY CREATED_AT DESC", nativeQuery = true)
   List<Order> searchByOrderIdContaining(@Param("orderId") String orderId);
   ```
   - **功能**: 模糊搜尋（部分匹配）
   - **SQL**: 
     ```sql
     SELECT * FROM ORDERS 
     WHERE TO_CHAR(ORDER_ID) LIKE '%' || :orderId || '%' 
     ORDER BY CREATED_AT DESC
     ```
   - **返回**: `List<Order>`

#### 資料庫操作

7. **Oracle 資料庫執行 SQL**
   - **精確匹配**:
     ```sql
     SELECT * FROM ORDERS WHERE ORDER_ID = ?
     ```
   - **模糊搜尋**:
     ```sql
     SELECT * FROM ORDERS 
     WHERE TO_CHAR(ORDER_ID) LIKE '%123%' 
     ORDER BY CREATED_AT DESC
     ```

8. **Hibernate 映射結果**
   - 將資料庫記錄映射為 `Order` 實體物件
   - 返回給 Repository 層

---

### 方式二：直接查詢單一訂單

#### 前端函數調用

1. **直接調用 API**（例如在編輯訂單時）
   ```javascript
   const response = await axios.get(`${API_BASE_URL}/orders/${orderId}`)
   ```
   - **URL**: `http://localhost:8080/api/orders/{id}`
   - **方法**: GET

#### 後端函數調用順序

2. **`OrderController.getOrderById()`** (OrderController.java:34-39)
   ```java
   @GetMapping("/{id}")
   public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
       Optional<Order> order = orderService.getOrderById(id);
       return order.map(ResponseEntity::ok)
                  .orElse(ResponseEntity.notFound().build());
   }
   ```
   - **功能**: 接收 HTTP 請求
   - **路徑**: `GET /api/orders/{id}`
   - **調用**: `orderService.getOrderById(id)`

3. **`OrderService.getOrderById()`** (OrderService.java:49-51)
   ```java
   public Optional<Order> getOrderById(Long orderId) {
       return orderRepository.findById(orderId);
   }
   ```
   - **功能**: 查詢訂單
   - **調用**: `orderRepository.findById(orderId)`

4. **`OrderRepository.findById()`** (JpaRepository 提供)
   ```java
   // 繼承自 JpaRepository<Order, Long>
   Optional<Order> findById(Long id);
   ```
   - **功能**: JPA 提供的標準查詢方法
   - **SQL**: `SELECT * FROM ORDERS WHERE ORDER_ID = ?`
   - **返回**: `Optional<Order>`

#### 資料庫操作

5. **Oracle 資料庫執行 SQL**
   ```sql
   SELECT * FROM ORDERS WHERE ORDER_ID = ?
   ```

6. **Hibernate 映射結果**
   - 將資料庫記錄映射為 `Order` 實體物件
   - 返回給 Repository 層

---

## 資料結構

### Order 實體結構

```java
@Entity
@Table(name = "ORDERS")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
    private Long orderId;           // 訂單ID（主鍵）
    
    private String username;        // 使用者名稱
    private BigDecimal amount;      // 金額
    private CurrencyCode currency;  // 幣別
    private String status;          // 狀態
    private BigDecimal discount;    // 折扣百分比
    private BigDecimal finalAmount; // 最終金額
    private LocalDateTime createdAt; // 建立時間
    private LocalDateTime updatedAt; // 更新時間
}
```

### 資料庫表結構

```sql
CREATE TABLE ORDERS (
    ORDER_ID NUMBER PRIMARY KEY,
    USERNAME VARCHAR2(50) NOT NULL,
    AMOUNT NUMBER(19,2) NOT NULL,
    CURRENCY VARCHAR2(3) NOT NULL,
    STATUS VARCHAR2(20),
    DISCOUNT NUMBER(5,2),
    FINAL_AMOUNT NUMBER(19,2),
    CREATED_AT TIMESTAMP,
    UPDATED_AT TIMESTAMP
);
```

---

## 關鍵技術點

### 1. 防抖處理（Debounce）
- **目的**: 避免用戶輸入時頻繁發送請求
- **實現**: 使用 `setTimeout` 延遲 500ms
- **位置**: `App.vue:178-186`

### 2. 精確匹配 vs 模糊搜尋
- **精確匹配**: 輸入完整數字時，先嘗試精確查詢
- **模糊搜尋**: 如果精確匹配失敗或輸入不是數字，使用 LIKE 查詢
- **位置**: `OrderService.java:83-102`

### 3. Optional 處理
- **目的**: 安全處理可能為空的返回值
- **使用**: `Optional<Order>` 避免 NullPointerException
- **位置**: Repository 和 Service 層

### 4. JPA/Hibernate 自動映射
- **功能**: 自動將資料庫記錄映射為 Java 物件
- **配置**: 透過 `@Entity` 和 `@Column` 註解

---

## 錯誤處理

### 前端錯誤處理

```javascript
try {
    const response = await axios.get(`${API_BASE_URL}/orders`, { params })
    this.orders = response.data
} catch (error) {
    console.error('載入訂單失敗:', error)
    alert('載入訂單失敗')
}
```

### 後端錯誤處理

```java
// Controller 層
Optional<Order> order = orderService.getOrderById(id);
return order.map(ResponseEntity::ok)
           .orElse(ResponseEntity.notFound().build());
```

---

## 效能優化

### 1. 防抖處理
- 減少不必要的 API 請求
- 提升用戶體驗

### 2. 索引優化
- `ORDER_ID` 主鍵索引（自動）
- `CREATED_AT` 索引（用於排序）

### 3. 查詢策略
- 先嘗試精確匹配（更快）
- 必要時才使用模糊搜尋

---

## 總結

「依據訂單ID查詢訂單」功能支援兩種方式：

1. **搜尋框查詢**（方式一）:
   - 前端: `debounceSearch()` → `loadOrders()`
   - 後端: `OrderController.getAllOrders()` → `OrderService.searchOrdersByOrderId()` → `OrderRepository.findByOrderId()` 或 `searchByOrderIdContaining()`
   - 資料庫: 精確匹配或模糊搜尋 SQL

2. **直接查詢**（方式二）:
   - 前端: 直接調用 `GET /api/orders/{id}`
   - 後端: `OrderController.getOrderById()` → `OrderService.getOrderById()` → `OrderRepository.findById()`
   - 資料庫: 精確匹配 SQL

兩種方式都經過完整的 Controller → Service → Repository → Database 調用鏈，確保資料的一致性和安全性。

