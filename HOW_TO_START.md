# 如何啟動前後端

## 問題
Maven 命令在 cmd 中找不到，但 IntelliJ IDEA 內建的 Maven 可以使用。

## 解決方案

### 方法 1：使用 IntelliJ IDEA 啟動後端（推薦）

1. **開啟 IntelliJ IDEA**
2. **開啟專案**
   - File → Open → 選擇 `order-currency-system` 資料夾
3. **等待專案索引完成**
4. **找到主類別**
   - `src/main/java/com/example/ordersystem/OrderCurrencyApplication.java`
5. **右鍵點擊 → Run 'OrderCurrencyApplication'**
   - 或點擊類別旁邊的綠色三角形 ▶️

### 方法 2：使用 Maven 工具視窗

1. **開啟 Maven 工具視窗**
   - View → Tool Windows → Maven
   - 或右側邊欄的 Maven 圖示
2. **展開專案 → Lifecycle**
3. **雙擊 `spring-boot:run`**

### 啟動前端

執行 `start-frontend-manual.bat` 或手動執行：

```bash
cd frontend
npm run dev
```

## 完整啟動步驟

1. **啟動資料庫**（如果還沒啟動）
   ```bash
   start-docker-db.bat
   ```

2. **啟動後端**（在 IntelliJ IDEA 中）
   - 右鍵 `OrderCurrencyApplication.java` → Run

3. **啟動前端**（在終端機中）
   ```bash
   start-frontend-manual.bat
   ```

## 訪問地址

- 後端 API: http://localhost:8080
- 前端頁面: http://localhost:5173

## 檢查服務狀態

後端啟動成功會看到：
```
Started OrderCurrencyApplication in X.XXX seconds
```

前端啟動成功會看到：
```
VITE v5.x.x  ready in XXX ms
➜  Local:   http://localhost:5173/
```


