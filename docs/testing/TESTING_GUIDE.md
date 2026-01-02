# 測試指南

## 概述

本文檔說明 Order Currency System 的測試策略、測試工具和測試輔助功能。

---

## 測試類型

### 1. 單元測試（Unit Tests）

測試單個類別或方法的邏輯，使用 Mock 隔離依賴。

**位置：** `src/test/java/com/example/ordersystem/service/`

**範例：**
- `OrderServiceTest.java`
- `CurrencyServiceTest.java`

### 2. 整合測試（Integration Tests）

測試多個組件之間的整合，使用真實的資料庫連線。

**位置：** `src/test/java/com/example/ordersystem/integration/`

**範例：**
- `OrderControllerIntegrationTest.java`
- `CurrencyControllerIntegrationTest.java`

### 3. Repository 測試

測試資料存取層的查詢邏輯。

**位置：** `src/test/java/com/example/ordersystem/repository/`

**範例：**
- `OrderRepositoryTest.java`
- `CurrencyRepositoryTest.java`

### 4. Controller 測試

測試 REST API 端點，使用 MockMvc。

**位置：** `src/test/java/com/example/ordersystem/controller/`

**範例：**
- `OrderControllerTest.java`
- `CurrencyControllerTest.java`

---

## 測試輔助工具

### 1. 測試資料生成器（Test Data Builders）

位於 `src/test/java/com/example/ordersystem/util/`：

- **`OrderTestDataBuilder.java`**: 建立測試用的 Order 物件
- **`CurrencyTestDataBuilder.java`**: 建立測試用的 Currency 物件

**使用範例：**
```java
Order order = OrderTestDataBuilder.builder()
    .withUsername("testuser")
    .withAmount(new BigDecimal("1000.00"))
    .withCurrency(CurrencyCode.USD)
    .build();
```

### 2. 測試工具類（Test Utils）

位於 `src/test/java/com/example/ordersystem/util/`：

- **`TestUtils.java`**: 提供通用的測試輔助方法
  - `createTestOrder()`: 建立測試訂單
  - `createTestCurrency()`: 建立測試幣別
  - `assertOrderEquals()`: 比較訂單是否相等
  - `assertCurrencyEquals()`: 比較幣別是否相等

### 3. 測試配置檔案

- **`application-test.properties`**: 測試環境專用配置
  - 使用 H2 記憶體資料庫（快速測試）
  - 關閉不必要的日誌
  - 設定測試專用的 JPA 配置

### 4. 測試資料 SQL 腳本

- **`test-data.sql`**: 測試用的初始化資料
  - 預設幣別資料
  - 範例訂單資料

---

## 執行測試

### 執行所有測試

```bash
mvn test
```

### 執行特定測試類別

```bash
mvn test -Dtest=OrderServiceTest
```

### 執行特定測試方法

```bash
mvn test -Dtest=OrderServiceTest#testCreateOrder_Success
```

### 執行整合測試

```bash
mvn test -Dtest=*IntegrationTest
```

### 跳過測試（僅編譯）

```bash
mvn clean install -DskipTests
```

---

## 測試覆蓋率

### 使用 JaCoCo 生成覆蓋率報告

1. **執行測試並生成報告：**
   ```bash
   mvn clean test jacoco:report
   ```

2. **查看報告：**
   打開 `target/site/jacoco/index.html`

### 覆蓋率目標

- **行覆蓋率（Line Coverage）**: ≥ 80%
- **分支覆蓋率（Branch Coverage）**: ≥ 70%
- **方法覆蓋率（Method Coverage）**: ≥ 80%

---

## Mock 資料生成

### 使用 Builder Pattern

```java
// Order Builder
Order order = OrderTestDataBuilder.builder()
    .withOrderId(1L)
    .withUsername("user1")
    .withAmount(new BigDecimal("1000.00"))
    .withCurrency(CurrencyCode.USD)
    .withStatus("PENDING")
    .withDiscount(new BigDecimal("10.00"))
    .build();

// Currency Builder
Currency currency = CurrencyTestDataBuilder.builder()
    .withCurrencyCode(CurrencyCode.USD)
    .withRateToTwd(new BigDecimal("31.250000"))
    .build();
```

---

## 測試最佳實踐

### 1. 測試命名規範

使用 `方法名_場景_預期結果` 格式：

```java
@Test
@DisplayName("測試建立訂單 - 成功")
void testCreateOrder_Success() {
    // ...
}

@Test
@DisplayName("測試建立訂單 - null 參數")
void testCreateOrder_NullParameter() {
    // ...
}
```

### 2. AAA 模式（Arrange-Act-Assert）

```java
@Test
void testGetOrderById_Success() {
    // Arrange: 準備測試資料
    Long orderId = 1L;
    Order expectedOrder = createTestOrder();
    when(orderRepository.findById(orderId)).thenReturn(Optional.of(expectedOrder));
    
    // Act: 執行被測試的方法
    Optional<Order> result = orderService.getOrderById(orderId);
    
    // Assert: 驗證結果
    assertTrue(result.isPresent());
    assertEquals(expectedOrder.getOrderId(), result.get().getOrderId());
}
```

### 3. 測試隔離

每個測試應該獨立，不依賴其他測試的執行順序。

### 4. 使用 @BeforeEach 和 @AfterEach

```java
@BeforeEach
void setUp() {
    // 每個測試前執行的初始化
}

@AfterEach
void tearDown() {
    // 每個測試後執行的清理
}
```

### 5. Mock 驗證

```java
// 驗證方法被呼叫
verify(orderRepository, times(1)).save(any(Order.class));

// 驗證方法未被呼叫
verify(currencyService, never()).convertToTwd(any(), any());
```

---

## 測試資料管理

### 使用 @Sql 註解載入測試資料

```java
@Test
@Sql(scripts = "/test-data.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
void testGetAllOrders_WithData() {
    // 測試會自動載入 test-data.sql 中的資料
}
```

### 使用 @Transactional 自動回滾

```java
@SpringBootTest
@Transactional
class OrderServiceIntegrationTest {
    // 測試結束後自動回滾，不影響資料庫
}
```

---

## 常見測試場景

### 1. 測試異常處理

```java
@Test
@DisplayName("測試建立訂單 - null 參數拋出異常")
void testCreateOrder_NullParameter() {
    assertThrows(IllegalArgumentException.class, () -> {
        orderService.createOrder(null);
    });
}
```

### 2. 測試邊界條件

```java
@Test
@DisplayName("測試轉換金額 - 零金額")
void testConvertToTwd_ZeroAmount() {
    BigDecimal result = currencyService.convertToTwd(BigDecimal.ZERO, CurrencyCode.USD);
    assertEquals(BigDecimal.ZERO, result);
}
```

### 3. 測試業務邏輯

```java
@Test
@DisplayName("測試計算最終金額 - 包含折扣")
void testCalculateFinalAmount_WithDiscount() {
    Order order = createTestOrder();
    order.setAmount(new BigDecimal("1000.00"));
    order.setDiscount(new BigDecimal("10.00"));
    
    orderService.createOrder(order);
    
    assertEquals(new BigDecimal("900.00"), order.getFinalAmount());
}
```

---

## 測試工具和框架

### 核心框架

- **JUnit 5**: 測試框架
- **Mockito**: Mock 框架
- **Spring Boot Test**: Spring 整合測試支援
- **AssertJ**: 流暢的斷言庫（可選）

### 資料庫測試

- **H2 Database**: 記憶體資料庫（快速測試）
- **Testcontainers**: Docker 容器測試（整合測試）

---

## 持續整合（CI）測試

### GitHub Actions 範例

```yaml
name: Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Run tests
        run: mvn test
      - name: Generate coverage report
        run: mvn jacoco:report
```

---

## 疑難排解

### 問題 1: 測試無法連線資料庫

**解決方法：**
- 確認 `application-test.properties` 配置正確
- 使用 H2 記憶體資料庫進行單元測試
- 整合測試使用 Testcontainers

### 問題 2: Mock 不生效

**解決方法：**
- 確認使用 `@ExtendWith(MockitoExtension.class)`
- 確認使用 `@Mock` 或 `@InjectMocks` 註解
- 檢查 Mock 設定是否正確

### 問題 3: 測試執行緩慢

**解決方法：**
- 使用 H2 記憶體資料庫而非真實資料庫
- 減少不必要的資料庫操作
- 使用 `@MockBean` 而非真實 Bean

---

## 參考資源

- [JUnit 5 官方文檔](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito 官方文檔](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [Spring Boot Test 文檔](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.testing)



