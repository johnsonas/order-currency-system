# API 測試 - cURL 命令範例

本文檔提供所有 API 端點的 cURL 命令範例，可直接在終端機執行測試。

**基礎 URL：** `http://localhost:8080/api`

---

## 訂單相關 API

### 1. 取得所有訂單

```bash
curl -X GET "http://localhost:8080/api/orders" \
  -H "Content-Type: application/json"
```

### 2. 搜尋訂單（根據訂單ID）

```bash
curl -X GET "http://localhost:8080/api/orders?searchOrderId=1" \
  -H "Content-Type: application/json"
```

### 3. 根據ID取得單一訂單

```bash
curl -X GET "http://localhost:8080/api/orders/1" \
  -H "Content-Type: application/json"
```

### 4. 根據使用者名稱取得訂單列表

```bash
curl -X GET "http://localhost:8080/api/orders/username/user1" \
  -H "Content-Type: application/json"
```

### 5. 根據狀態取得訂單列表

```bash
curl -X GET "http://localhost:8080/api/orders/status/PENDING" \
  -H "Content-Type: application/json"
```

### 6. 建立新訂單

```bash
curl -X POST "http://localhost:8080/api/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "amount": 1000.00,
    "currency": "USD",
    "status": "PENDING",
    "discount": 10.00
  }'
```

### 7. 更新訂單

```bash
curl -X PUT "http://localhost:8080/api/orders/1" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "updateduser",
    "amount": 2000.00,
    "currency": "EUR",
    "status": "CONFIRMED",
    "discount": 15.00
  }'
```

### 8. 刪除訂單

```bash
curl -X DELETE "http://localhost:8080/api/orders/1" \
  -H "Content-Type: application/json"
```

### 9. 將訂單金額轉換為 TWD

```bash
curl -X GET "http://localhost:8080/api/orders/1/convert/twd" \
  -H "Content-Type: application/json"
```

### 10. 將訂單金額轉換為指定幣別

```bash
curl -X GET "http://localhost:8080/api/orders/1/convert/EUR" \
  -H "Content-Type: application/json"
```

---

## 幣別相關 API

### 11. 取得所有幣別

```bash
curl -X GET "http://localhost:8080/api/currencies" \
  -H "Content-Type: application/json"
```

### 12. 根據幣別代碼取得幣別資訊

```bash
curl -X GET "http://localhost:8080/api/currencies/USD" \
  -H "Content-Type: application/json"
```

### 13. 建立新幣別

```bash
curl -X POST "http://localhost:8080/api/currencies" \
  -H "Content-Type: application/json" \
  -d '{
    "currencyCode": "GBP",
    "rateToTwd": 39.500000
  }'
```

### 14. 更新幣別資訊

```bash
curl -X PUT "http://localhost:8080/api/currencies/USD" \
  -H "Content-Type: application/json" \
  -d '{
    "currencyCode": "USD",
    "rateToTwd": 32.000000
  }'
```

### 15. 更新幣別匯率

```bash
curl -X PUT "http://localhost:8080/api/currencies/USD/rate" \
  -H "Content-Type: application/json" \
  -d "32.000000"
```

### 16. 刪除幣別

```bash
curl -X DELETE "http://localhost:8080/api/currencies/GBP" \
  -H "Content-Type: application/json"
```

### 17. 幣別轉換

```bash
curl -X POST "http://localhost:8080/api/currencies/convert?amount=1000&sourceCurrency=USD&targetCurrency=EUR" \
  -H "Content-Type: application/json"
```

---

## Windows PowerShell 範例

在 Windows PowerShell 中，需要使用不同的語法：

### 建立訂單（PowerShell）

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/orders" `
  -Method POST `
  -ContentType "application/json" `
  -Body '{
    "username": "testuser",
    "amount": 1000.00,
    "currency": "USD",
    "status": "PENDING",
    "discount": 10.00
  }'
```

### 取得所有訂單（PowerShell）

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/orders" `
  -Method GET `
  -ContentType "application/json"
```

---

## 測試腳本範例

### Linux/Mac Bash 腳本

建立 `test-api.sh`：

```bash
#!/bin/bash

BASE_URL="http://localhost:8080/api"

echo "=== Testing Order APIs ==="

# 1. Get all orders
echo "1. Getting all orders..."
curl -X GET "$BASE_URL/orders" -H "Content-Type: application/json"
echo -e "\n"

# 2. Create order
echo "2. Creating order..."
ORDER_RESPONSE=$(curl -s -X POST "$BASE_URL/orders" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "amount": 1000.00,
    "currency": "USD",
    "status": "PENDING",
    "discount": 10.00
  }')
echo "$ORDER_RESPONSE"
ORDER_ID=$(echo "$ORDER_RESPONSE" | grep -o '"orderId":[0-9]*' | grep -o '[0-9]*')
echo -e "\nCreated Order ID: $ORDER_ID\n"

# 3. Get order by ID
echo "3. Getting order by ID..."
curl -X GET "$BASE_URL/orders/$ORDER_ID" -H "Content-Type: application/json"
echo -e "\n"

echo "=== Testing Currency APIs ==="

# 4. Get all currencies
echo "4. Getting all currencies..."
curl -X GET "$BASE_URL/currencies" -H "Content-Type: application/json"
echo -e "\n"
```

執行：
```bash
chmod +x test-api.sh
./test-api.sh
```

---

## 使用 jq 美化輸出（可選）

如果安裝了 `jq`，可以使用它來美化 JSON 輸出：

```bash
curl -X GET "http://localhost:8080/api/orders" | jq
```

---

## 注意事項

1. **確保後端服務已啟動**
   - 後端應運行在 `http://localhost:8080`

2. **CORS 設定**
   - API 允許來自 `http://localhost:5173` 的跨域請求

3. **幣別代碼**
   - 支援的幣別：TWD, USD, EUR, JPY, CNY

4. **錯誤處理**
   - 如果訂單不存在，會返回 `404 Not Found`
   - 如果請求參數錯誤，會返回 `400 Bad Request`



