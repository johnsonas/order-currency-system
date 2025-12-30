# API 文檔

## 概述

本文檔描述了 Order Currency System 的所有 REST API 端點。所有 API 的基礎 URL 為：`http://localhost:8080/api`

---

## 訂單相關 API

### 1. 取得所有訂單

**端點：** `GET /api/orders`

**描述：** 取得所有訂單列表，按建立時間降序排列（最新的在前）

**查詢參數：**
- `searchOrderId` (可選, String): 根據訂單ID進行搜尋（支援精確匹配和模糊搜尋）

**請求範例：**
```http
GET /api/orders
GET /api/orders?searchOrderId=123
GET /api/orders?searchOrderId=12
```

**響應狀態碼：**
- `200 OK`: 成功取得訂單列表

**響應範例：**
```json
[
  {
    "orderId": 1,
    "username": "user1",
    "amount": 1000.00,
    "currency": "USD",
    "status": "PENDING",
    "discount": 0.00,
    "finalAmount": 1000.00,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  },
  {
    "orderId": 2,
    "username": "user2",
    "amount": 5000.00,
    "currency": "TWD",
    "status": "CONFIRMED",
    "discount": 10.00,
    "finalAmount": 4500.00,
    "createdAt": "2024-01-02T11:00:00",
    "updatedAt": "2024-01-02T11:00:00"
  }
]
```

---

### 2. 根據ID取得單一訂單

**端點：** `GET /api/orders/{id}`

**描述：** 根據訂單ID取得單一訂單資訊

**路徑參數：**
- `id` (必填, Long): 訂單ID

**請求範例：**
```http
GET /api/orders/1
```

**響應狀態碼：**
- `200 OK`: 成功取得訂單
- `404 Not Found`: 訂單不存在

**響應範例：**
```json
{
  "orderId": 1,
  "username": "user1",
  "amount": 1000.00,
  "currency": "USD",
  "status": "PENDING",
  "discount": 0.00,
  "finalAmount": 1000.00,
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

---

### 3. 根據使用者名稱取得訂單列表

**端點：** `GET /api/orders/username/{username}`

**描述：** 取得指定使用者的所有訂單

**路徑參數：**
- `username` (必填, String): 使用者名稱

**請求範例：**
```http
GET /api/orders/username/user1
```

**響應狀態碼：**
- `200 OK`: 成功取得訂單列表

**響應範例：**
```json
[
  {
    "orderId": 1,
    "username": "user1",
    "amount": 1000.00,
    "currency": "USD",
    "status": "PENDING",
    "discount": 0.00,
    "finalAmount": 1000.00,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
]
```

---

### 4. 根據狀態取得訂單列表

**端點：** `GET /api/orders/status/{status}`

**描述：** 取得指定狀態的所有訂單

**路徑參數：**
- `status` (必填, String): 訂單狀態（如：PENDING, CONFIRMED, CANCELLED, COMPLETED）

**請求範例：**
```http
GET /api/orders/status/PENDING
```

**響應狀態碼：**
- `200 OK`: 成功取得訂單列表

**響應範例：**
```json
[
  {
    "orderId": 1,
    "username": "user1",
    "amount": 1000.00,
    "currency": "USD",
    "status": "PENDING",
    "discount": 0.00,
    "finalAmount": 1000.00,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
]
```

---

### 5. 建立新訂單

**端點：** `POST /api/orders`

**描述：** 建立一筆新訂單，系統會自動計算最終金額（包含折扣）

**請求標頭：**
```
Content-Type: application/json
```

**請求體：**
```json
{
  "username": "user1",
  "amount": 1000.00,
  "currency": "USD",
  "status": "PENDING",
  "discount": 10.00
}
```

**欄位說明：**
- `username` (必填, String): 使用者名稱，不能為空
- `amount` (必填, BigDecimal): 訂單金額，必須大於0
- `currency` (必填, CurrencyCode): 幣別代碼（TWD, USD, EUR, JPY, CNY）
- `status` (可選, String): 訂單狀態，預設為 "PENDING"
- `discount` (可選, BigDecimal): 折扣百分比（0-100），預設為 0

**響應狀態碼：**
- `201 Created`: 訂單建立成功
- `400 Bad Request`: 請求資料驗證失敗

**響應範例：**
```json
{
  "orderId": 1,
  "username": "user1",
  "amount": 1000.00,
  "currency": "USD",
  "status": "PENDING",
  "discount": 10.00,
  "finalAmount": 900.00,
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T10:00:00"
}
```

---

### 6. 更新訂單

**端點：** `PUT /api/orders/{id}`

**描述：** 更新指定訂單的所有資訊，系統會重新計算最終金額

**路徑參數：**
- `id` (必填, Long): 訂單ID

**請求標頭：**
```
Content-Type: application/json
```

**請求體：**
```json
{
  "username": "user1",
  "amount": 2000.00,
  "currency": "EUR",
  "status": "CONFIRMED",
  "discount": 15.00
}
```

**響應狀態碼：**
- `200 OK`: 訂單更新成功
- `400 Bad Request`: 請求資料驗證失敗
- `404 Not Found`: 訂單不存在

**響應範例：**
```json
{
  "orderId": 1,
  "username": "user1",
  "amount": 2000.00,
  "currency": "EUR",
  "status": "CONFIRMED",
  "discount": 15.00,
  "finalAmount": 1700.00,
  "createdAt": "2024-01-01T10:00:00",
  "updatedAt": "2024-01-01T11:00:00"
}
```

---

### 7. 刪除訂單

**端點：** `DELETE /api/orders/{id}`

**描述：** 刪除指定訂單

**路徑參數：**
- `id` (必填, Long): 訂單ID

**請求範例：**
```http
DELETE /api/orders/1
```

**響應狀態碼：**
- `204 No Content`: 訂單刪除成功

---

### 8. 將訂單金額轉換為 TWD

**端點：** `GET /api/orders/{id}/convert/twd`

**描述：** 將指定訂單的最終金額轉換為新台幣（TWD）

**路徑參數：**
- `id` (必填, Long): 訂單ID

**請求範例：**
```http
GET /api/orders/1/convert/twd
```

**響應狀態碼：**
- `200 OK`: 轉換成功
- `404 Not Found`: 訂單不存在

**響應範例：**
```json
31250.00
```

---

### 9. 將訂單金額轉換為指定幣別

**端點：** `GET /api/orders/{id}/convert/{targetCurrency}`

**描述：** 將指定訂單的最終金額轉換為目標幣別（轉換流程：訂單幣別 → TWD → 目標幣別）

**路徑參數：**
- `id` (必填, Long): 訂單ID
- `targetCurrency` (必填, String): 目標幣別代碼（TWD, USD, EUR, JPY, CNY）

**請求範例：**
```http
GET /api/orders/1/convert/EUR
```

**響應狀態碼：**
- `200 OK`: 轉換成功
- `400 Bad Request`: 無效的幣別代碼
- `404 Not Found`: 訂單不存在

**響應範例：**
```json
906.25
```

---

## 幣別相關 API

### 10. 取得所有幣別

**端點：** `GET /api/currencies`

**描述：** 取得所有幣別及其匯率資訊

**請求範例：**
```http
GET /api/currencies
```

**響應狀態碼：**
- `200 OK`: 成功取得幣別列表

**響應範例：**
```json
[
  {
    "currencyCode": "TWD",
    "rateToTwd": 1.000000,
    "lastUpdate": "2024-01-01T10:00:00"
  },
  {
    "currencyCode": "USD",
    "rateToTwd": 31.250000,
    "lastUpdate": "2024-01-01T10:00:00"
  },
  {
    "currencyCode": "EUR",
    "rateToTwd": 34.480000,
    "lastUpdate": "2024-01-01T10:00:00"
  }
]
```

---

### 11. 根據幣別代碼取得幣別資訊

**端點：** `GET /api/currencies/{code}`

**描述：** 取得指定幣別的匯率資訊

**路徑參數：**
- `code` (必填, String): 幣別代碼（TWD, USD, EUR, JPY, CNY）

**請求範例：**
```http
GET /api/currencies/USD
```

**響應狀態碼：**
- `200 OK`: 成功取得幣別資訊
- `400 Bad Request`: 無效的幣別代碼
- `404 Not Found`: 幣別不存在

**響應範例：**
```json
{
  "currencyCode": "USD",
  "rateToTwd": 31.250000,
  "lastUpdate": "2024-01-01T10:00:00"
}
```

---

### 12. 建立新幣別

**端點：** `POST /api/currencies`

**描述：** 建立新的幣別及其匯率資訊

**請求標頭：**
```
Content-Type: application/json
```

**請求體：**
```json
{
  "currencyCode": "GBP",
  "rateToTwd": 39.500000
}
```

**欄位說明：**
- `currencyCode` (必填, CurrencyCode): 幣別代碼 Enum
- `rateToTwd` (必填, BigDecimal): 對 TWD 的匯率，必須大於0

**響應狀態碼：**
- `201 Created`: 幣別建立成功
- `400 Bad Request`: 請求資料驗證失敗

**響應範例：**
```json
{
  "currencyCode": "GBP",
  "rateToTwd": 39.500000,
  "lastUpdate": "2024-01-01T10:00:00"
}
```

---

### 13. 更新幣別資訊

**端點：** `PUT /api/currencies/{code}`

**描述：** 更新指定幣別的所有資訊

**路徑參數：**
- `code` (必填, String): 幣別代碼

**請求標頭：**
```
Content-Type: application/json
```

**請求體：**
```json
{
  "currencyCode": "USD",
  "rateToTwd": 32.000000
}
```

**響應狀態碼：**
- `200 OK`: 幣別更新成功
- `400 Bad Request`: 請求資料驗證失敗或無效的幣別代碼

**響應範例：**
```json
{
  "currencyCode": "USD",
  "rateToTwd": 32.000000,
  "lastUpdate": "2024-01-01T11:00:00"
}
```

---

### 14. 更新幣別匯率

**端點：** `PUT /api/currencies/{code}/rate`

**描述：** 只更新指定幣別的匯率，不更新其他欄位

**路徑參數：**
- `code` (必填, String): 幣別代碼

**請求標頭：**
```
Content-Type: application/json
```

**請求體：**
```json
32.000000
```

**請求範例：**
```http
PUT /api/currencies/USD/rate
Content-Type: application/json

32.000000
```

**響應狀態碼：**
- `200 OK`: 匯率更新成功
- `400 Bad Request`: 無效的幣別代碼或匯率
- `404 Not Found`: 幣別不存在

**響應範例：**
```json
{
  "currencyCode": "USD",
  "rateToTwd": 32.000000,
  "lastUpdate": "2024-01-01T11:00:00"
}
```

---

### 15. 刪除幣別

**端點：** `DELETE /api/currencies/{code}`

**描述：** 刪除指定幣別

**路徑參數：**
- `code` (必填, String): 幣別代碼

**請求範例：**
```http
DELETE /api/currencies/GBP
```

**響應狀態碼：**
- `204 No Content`: 幣別刪除成功
- `400 Bad Request`: 無效的幣別代碼

---

### 16. 幣別轉換

**端點：** `POST /api/currencies/convert`

**描述：** 將金額從來源幣別轉換為目標幣別（轉換流程：來源幣別 → TWD → 目標幣別）

**查詢參數：**
- `amount` (必填, BigDecimal): 要轉換的金額
- `sourceCurrency` (必填, String): 來源幣別代碼
- `targetCurrency` (必填, String): 目標幣別代碼

**請求範例：**
```http
POST /api/currencies/convert?amount=1000&sourceCurrency=USD&targetCurrency=EUR
```

**響應狀態碼：**
- `200 OK`: 轉換成功
- `400 Bad Request`: 無效的幣別代碼或金額

**響應範例：**
```json
906.25
```

---

## 錯誤處理

### 常見錯誤碼

- `400 Bad Request`: 請求參數錯誤或驗證失敗
- `404 Not Found`: 資源不存在
- `500 Internal Server Error`: 伺服器內部錯誤

### 錯誤響應格式

```json
{
  "timestamp": "2024-01-01T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "金額不能為空",
  "path": "/api/orders"
}
```

---

## 幣別代碼說明

系統支援以下幣別：

- `TWD`: 新台幣（基準幣別）
- `USD`: 美元
- `EUR`: 歐元
- `JPY`: 日圓
- `CNY`: 人民幣

---

## CORS 設定

API 允許來自 `http://localhost:5173` 的跨域請求（前端開發環境）。

---

## 注意事項

1. 所有金額欄位使用 `BigDecimal` 類型，確保精確計算
2. 匯率以 `RATE_TO_TWD` 表示：1 單位該幣別 = RATE_TO_TWD TWD
3. 折扣百分比範圍為 0-100
4. 幣別轉換會先轉換為 TWD，再轉換為目標幣別
5. 訂單ID由資料庫序列自動產生
6. 建立時間和更新時間由系統自動管理



