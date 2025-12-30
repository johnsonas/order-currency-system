# API 測試 - RestAssured 範例

本文檔提供使用 RestAssured 進行 API 測試的範例代碼。

## 安裝 RestAssured

在 `pom.xml` 中添加依賴：

```xml
<dependency>
    <groupId>io.rest-assured</groupId>
    <artifactId>rest-assured</artifactId>
    <scope>test</scope>
</dependency>
```

## 基本設定

```java
import io.rest-assured.RestAssured;
import io.rest-assured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.rest-assured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ApiTest {
    
    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:8080";
        RestAssured.basePath = "/api";
    }
}
```

## 訂單 API 測試範例

### 1. 取得所有訂單

```java
@Test
void testGetAllOrders() {
    given()
        .contentType(ContentType.JSON)
    .when()
        .get("/orders")
    .then()
        .statusCode(200)
        .contentType(ContentType.JSON)
        .body("$", is(not(empty())));
}
```

### 2. 搜尋訂單

```java
@Test
void testSearchOrders() {
    given()
        .contentType(ContentType.JSON)
        .queryParam("searchOrderId", "1")
    .when()
        .get("/orders")
    .then()
        .statusCode(200)
        .body("$", is(not(empty())));
}
```

### 3. 根據ID取得訂單

```java
@Test
void testGetOrderById() {
    given()
        .contentType(ContentType.JSON)
        .pathParam("id", 1)
    .when()
        .get("/orders/{id}")
    .then()
        .statusCode(200)
        .body("orderId", equalTo(1))
        .body("username", notNullValue())
        .body("amount", notNullValue());
}
```

### 4. 建立訂單

```java
@Test
void testCreateOrder() {
    String requestBody = """
        {
          "username": "testuser",
          "amount": 1000.00,
          "currency": "USD",
          "status": "PENDING",
          "discount": 10.00
        }
        """;
    
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
    .when()
        .post("/orders")
    .then()
        .statusCode(201)
        .body("orderId", notNullValue())
        .body("username", equalTo("testuser"))
        .body("finalAmount", notNullValue());
}
```

### 5. 更新訂單

```java
@Test
void testUpdateOrder() {
    String requestBody = """
        {
          "username": "updateduser",
          "amount": 2000.00,
          "currency": "EUR",
          "status": "CONFIRMED",
          "discount": 15.00
        }
        """;
    
    given()
        .contentType(ContentType.JSON)
        .pathParam("id", 1)
        .body(requestBody)
    .when()
        .put("/orders/{id}")
    .then()
        .statusCode(200)
        .body("username", equalTo("updateduser"))
        .body("status", equalTo("CONFIRMED"));
}
```

### 6. 刪除訂單

```java
@Test
void testDeleteOrder() {
    given()
        .contentType(ContentType.JSON)
        .pathParam("id", 1)
    .when()
        .delete("/orders/{id}")
    .then()
        .statusCode(204);
}
```

### 7. 轉換為 TWD

```java
@Test
void testConvertToTwd() {
    given()
        .contentType(ContentType.JSON)
        .pathParam("id", 1)
    .when()
        .get("/orders/{id}/convert/twd")
    .then()
        .statusCode(200)
        .body(notNullValue())
        .body(instanceOf(Number.class));
}
```

## 幣別 API 測試範例

### 1. 取得所有幣別

```java
@Test
void testGetAllCurrencies() {
    given()
        .contentType(ContentType.JSON)
    .when()
        .get("/currencies")
    .then()
        .statusCode(200)
        .body("$", is(not(empty())))
        .body("currencyCode", hasItems("TWD", "USD", "EUR"));
}
```

### 2. 根據代碼取得幣別

```java
@Test
void testGetCurrencyByCode() {
    given()
        .contentType(ContentType.JSON)
        .pathParam("code", "USD")
    .when()
        .get("/currencies/{code}")
    .then()
        .statusCode(200)
        .body("currencyCode", equalTo("USD"))
        .body("rateToTwd", notNullValue());
}
```

### 3. 建立幣別

```java
@Test
void testCreateCurrency() {
    String requestBody = """
        {
          "currencyCode": "GBP",
          "rateToTwd": 39.500000
        }
        """;
    
    given()
        .contentType(ContentType.JSON)
        .body(requestBody)
    .when()
        .post("/currencies")
    .then()
        .statusCode(201)
        .body("currencyCode", equalTo("GBP"))
        .body("rateToTwd", equalTo(39.5f));
}
```

### 4. 更新匯率

```java
@Test
void testUpdateRate() {
    given()
        .contentType(ContentType.JSON)
        .pathParam("code", "USD")
        .body("32.000000")
    .when()
        .put("/currencies/{code}/rate")
    .then()
        .statusCode(200)
        .body("rateToTwd", equalTo(32.0f));
}
```

### 5. 幣別轉換

```java
@Test
void testConvertCurrency() {
    given()
        .contentType(ContentType.JSON)
        .queryParam("amount", 1000)
        .queryParam("sourceCurrency", "USD")
        .queryParam("targetCurrency", "EUR")
    .when()
        .post("/currencies/convert")
    .then()
        .statusCode(200)
        .body(notNullValue())
        .body(instanceOf(Number.class));
}
```

## 完整測試類別範例

```java
package com.example.ordersystem.api;

import io.rest-assured.RestAssured;
import io.rest-assured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static io.rest-assured.RestAssured.given;
import static org.hamcrest.Matchers.*;

@DisplayName("API 整合測試")
public class OrderApiTest {
    
    private static final String BASE_URL = "http://localhost:8080/api";
    
    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost:8080";
        RestAssured.basePath = "/api";
    }
    
    @Test
    @DisplayName("測試完整的訂單流程")
    void testOrderFlow() {
        // 1. 建立訂單
        Integer orderId = given()
            .contentType(ContentType.JSON)
            .body("""
                {
                  "username": "apitest",
                  "amount": 1000.00,
                  "currency": "USD",
                  "status": "PENDING",
                  "discount": 10.00
                }
                """)
        .when()
            .post("/orders")
        .then()
            .statusCode(201)
            .extract()
            .path("orderId");
        
        // 2. 查詢訂單
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", orderId)
        .when()
            .get("/orders/{id}")
        .then()
            .statusCode(200)
            .body("orderId", equalTo(orderId))
            .body("finalAmount", equalTo(900.0f));
        
        // 3. 轉換為 TWD
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", orderId)
        .when()
            .get("/orders/{id}/convert/twd")
        .then()
            .statusCode(200)
            .body(notNullValue());
        
        // 4. 刪除訂單
        given()
            .contentType(ContentType.JSON)
            .pathParam("id", orderId)
        .when()
            .delete("/orders/{id}")
        .then()
            .statusCode(204);
    }
}
```

## 驗證響應結構

```java
@Test
void testOrderResponseStructure() {
    given()
        .contentType(ContentType.JSON)
        .pathParam("id", 1)
    .when()
        .get("/orders/{id}")
    .then()
        .statusCode(200)
        .body("orderId", notNullValue())
        .body("username", notNullValue())
        .body("amount", notNullValue())
        .body("currency", notNullValue())
        .body("status", notNullValue())
        .body("discount", notNullValue())
        .body("finalAmount", notNullValue())
        .body("createdAt", notNullValue())
        .body("updatedAt", notNullValue());
}
```

## 錯誤處理測試

```java
@Test
void testGetNonExistentOrder() {
    given()
        .contentType(ContentType.JSON)
        .pathParam("id", 99999)
    .when()
        .get("/orders/{id}")
    .then()
        .statusCode(404);
}

@Test
void testInvalidCurrencyCode() {
    given()
        .contentType(ContentType.JSON)
        .pathParam("code", "INVALID")
    .when()
        .get("/currencies/{code}")
    .then()
        .statusCode(400);
}
```

## 使用 JSON Schema 驗證

```java
@Test
void testOrderJsonSchema() {
    given()
        .contentType(ContentType.JSON)
        .pathParam("id", 1)
    .when()
        .get("/orders/{id}")
    .then()
        .statusCode(200)
        .body(matchesJsonSchemaInClasspath("order-schema.json"));
}
```

## 注意事項

1. **確保後端服務已啟動**
   - 測試前確保後端運行在 `http://localhost:8080`

2. **測試資料隔離**
   - 建議使用 `@Transactional` 或測試專用資料庫

3. **並發測試**
   - 多個測試同時執行時注意資料衝突

4. **環境變數**
   - 可以使用環境變數設定 base URL：
   ```java
   RestAssured.baseURI = System.getProperty("api.base.url", "http://localhost:8080");
   ```



