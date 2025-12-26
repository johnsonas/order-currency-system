# 使用 IntelliJ IDEA 啟動後端

## 方法 1：直接在 IntelliJ IDEA 中啟動（最簡單）

1. **開啟 IntelliJ IDEA**
2. **開啟專案**
   - File → Open → 選擇 `order-currency-system` 資料夾
3. **等待專案索引完成**
4. **找到主類別**
   - `src/main/java/com/example/ordersystem/OrderCurrencyApplication.java`
5. **右鍵點擊 → Run 'OrderCurrencyApplication'**
   - 或點擊類別旁邊的綠色三角形 ▶️

## 方法 2：使用 Maven 工具視窗

1. **開啟 Maven 工具視窗**
   - View → Tool Windows → Maven
   - 或右側邊欄的 Maven 圖示
2. **展開專案 → Lifecycle**
3. **雙擊 `spring-boot:run`**

## 方法 3：使用終端機（在 IntelliJ 內）

1. **開啟終端機**
   - View → Tool Windows → Terminal
   - 或 Alt + F12
2. **執行：**
   ```bash
   mvn spring-boot:run
   ```

## 檢查設定

確保：
- ✅ Java SDK 已設定（File → Project Structure → Project SDK）
- ✅ Maven 已設定（File → Settings → Build → Build Tools → Maven）
- ✅ 資料庫已啟動（Docker 容器運行中）

## 預期結果

啟動成功後會看到：
```
Started OrderCurrencyApplication in X.XXX seconds
```

然後可以訪問：http://localhost:8080


