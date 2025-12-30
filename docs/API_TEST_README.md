# API 測試文件說明

本目錄包含多種 API 測試工具和範例，方便您選擇適合的方式進行 API 測試。

---

## 📁 文件列表

### 1. **Postman Collection**
- **文件：** `API_TEST_COLLECTION.postman_collection.json`
- **用途：** 可直接導入 Postman 進行 API 測試
- **使用方式：**
  1. 打開 Postman
  2. 點擊 "Import"
  3. 選擇 `API_TEST_COLLECTION.postman_collection.json`
  4. 設定環境變數 `baseUrl` 為 `http://localhost:8080`

### 2. **cURL 命令範例**
- **文件：** `API_TEST_CURL.md`
- **用途：** 提供所有 API 的 cURL 命令，可直接在終端機執行
- **適用場景：**
  - 快速測試單個 API
  - CI/CD 腳本中執行
  - 不需要 GUI 工具的環境

### 3. **RestAssured 測試代碼**
- **文件：** `API_TEST_RESTASSURED.md`
- **用途：** Java 程式碼範例，使用 RestAssured 進行 API 測試
- **適用場景：**
  - 整合到單元測試中
  - 自動化 API 測試
  - 需要程式化驗證的場景

---

## 🚀 快速開始

### 方式一：使用 Postman（推薦新手）

1. **安裝 Postman**
   - 下載：https://www.postman.com/downloads/

2. **導入 Collection**
   ```
   File → Import → 選擇 API_TEST_COLLECTION.postman_collection.json
   ```

3. **設定環境變數**
   - 建立新環境
   - 添加變數 `baseUrl` = `http://localhost:8080`

4. **開始測試**
   - 選擇環境
   - 點擊請求即可測試

### 方式二：使用 cURL（終端機）

1. **確保後端已啟動**
   ```bash
   # 檢查後端是否運行
   curl http://localhost:8080/api/orders
   ```

2. **執行測試命令**
   ```bash
   # 取得所有訂單
   curl -X GET "http://localhost:8080/api/orders" \
     -H "Content-Type: application/json"
   ```

3. **參考完整範例**
   - 查看 `API_TEST_CURL.md` 獲取所有命令

### 方式三：使用 RestAssured（Java 測試）

1. **添加依賴到 pom.xml**
   ```xml
   <dependency>
       <groupId>io.rest-assured</groupId>
       <artifactId>rest-assured</artifactId>
       <scope>test</scope>
   </dependency>
   ```

2. **編寫測試代碼**
   ```java
   @Test
   void testGetAllOrders() {
       given()
           .contentType(ContentType.JSON)
       .when()
           .get("/api/orders")
       .then()
           .statusCode(200);
   }
   ```

3. **執行測試**
   ```bash
   mvn test
   ```

---

## 📋 API 端點總覽

### 訂單相關（10 個端點）

| 方法 | 端點 | 說明 |
|------|------|------|
| GET | `/api/orders` | 取得所有訂單 |
| GET | `/api/orders?searchOrderId={id}` | 搜尋訂單 |
| GET | `/api/orders/{id}` | 取得單一訂單 |
| GET | `/api/orders/username/{username}` | 根據使用者名稱查詢 |
| GET | `/api/orders/status/{status}` | 根據狀態查詢 |
| POST | `/api/orders` | 建立訂單 |
| PUT | `/api/orders/{id}` | 更新訂單 |
| DELETE | `/api/orders/{id}` | 刪除訂單 |
| GET | `/api/orders/{id}/convert/twd` | 轉換為 TWD |
| GET | `/api/orders/{id}/convert/{currency}` | 轉換為指定幣別 |

### 幣別相關（7 個端點）

| 方法 | 端點 | 說明 |
|------|------|------|
| GET | `/api/currencies` | 取得所有幣別 |
| GET | `/api/currencies/{code}` | 取得單一幣別 |
| POST | `/api/currencies` | 建立幣別 |
| PUT | `/api/currencies/{code}` | 更新幣別 |
| PUT | `/api/currencies/{code}/rate` | 更新匯率 |
| DELETE | `/api/currencies/{code}` | 刪除幣別 |
| POST | `/api/currencies/convert` | 幣別轉換 |

---

## 🔧 測試前準備

### 1. 啟動後端服務

```bash
# 方式一：使用 Maven
mvn spring-boot:run

# 方式二：使用腳本（Windows）
start-spring-boot.bat
```

### 2. 確認服務運行

```bash
# 檢查健康狀態
curl http://localhost:8080/api/orders

# 或使用瀏覽器訪問
# http://localhost:8080/api/orders
```

### 3. 準備測試資料（可選）

如果需要測試資料，可以先建立一些訂單和幣別：

```bash
# 建立測試訂單
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

---

## 📊 測試場景建議

### 基本功能測試
- ✅ 取得所有資源
- ✅ 根據ID取得資源
- ✅ 建立資源
- ✅ 更新資源
- ✅ 刪除資源

### 邊界條件測試
- ✅ 不存在的資源（404）
- ✅ 無效的參數（400）
- ✅ 空值處理
- ✅ 負數金額
- ✅ 無效的幣別代碼

### 業務邏輯測試
- ✅ 折扣計算
- ✅ 幣別轉換
- ✅ 搜尋功能
- ✅ 排序功能

---

## 🐛 常見問題

### 1. 連線被拒絕

**問題：** `Connection refused`

**解決：**
- 確認後端服務已啟動
- 檢查端口是否為 8080
- 確認防火牆設定

### 2. CORS 錯誤

**問題：** `CORS policy: No 'Access-Control-Allow-Origin' header`

**解決：**
- 確認後端 `@CrossOrigin` 設定正確
- 檢查請求來源是否在允許列表中

### 3. 404 Not Found

**問題：** API 返回 404

**解決：**
- 確認 API 路徑正確
- 檢查資源是否存在
- 確認 HTTP 方法正確（GET/POST/PUT/DELETE）

### 4. 400 Bad Request

**問題：** API 返回 400

**解決：**
- 檢查請求體格式是否正確
- 確認必填欄位都有提供
- 檢查資料驗證規則

---

## 📚 相關文檔

- [API 文檔](../API_DOCUMENTATION.md) - 完整的 API 說明
- [OpenAPI 規範](../openapi.yaml) - OpenAPI/Swagger 規範
- [開發指南](../DEVELOPMENT_GUIDE.md) - 開發環境設置
- [測試指南](../TESTING_GUIDE.md) - 測試相關說明

---

## 💡 測試最佳實踐

1. **測試順序**
   - 先測試 GET（查詢）
   - 再測試 POST（建立）
   - 然後測試 PUT（更新）
   - 最後測試 DELETE（刪除）

2. **資料清理**
   - 測試後清理測試資料
   - 使用測試專用資料庫

3. **錯誤處理**
   - 測試正常流程
   - 也要測試錯誤情況

4. **自動化**
   - 將 API 測試整合到 CI/CD
   - 使用 RestAssured 進行自動化測試

---

## 📞 支援

如有問題或建議，請聯繫專案維護者或建立 Issue。



