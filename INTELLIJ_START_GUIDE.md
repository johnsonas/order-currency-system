# IntelliJ IDEA 啟動後端指南

## 方法 1：直接運行主類別（最簡單，推薦）

### 步驟：

1. **開啟 IntelliJ IDEA**

2. **開啟專案**
   - `File` → `Open`
   - 選擇專案資料夾：`C:\Users\2500916\order-currency-system`
   - 點擊 `OK`

3. **等待專案索引完成**
   - IntelliJ 會自動下載依賴（Maven）
   - 右下角會顯示索引進度

4. **找到主類別**
   - 在專案視窗中找到：`src/main/java/com/example/ordersystem/OrderCurrencyApplication.java`
   - 或使用快速搜尋：按 `Ctrl + Shift + N`，輸入 `OrderCurrencyApplication`

5. **啟動應用程式**
   - **方法 A**：右鍵點擊 `OrderCurrencyApplication.java` → `Run 'OrderCurrencyApplication'`
   - **方法 B**：點擊類別名稱旁邊的綠色三角形 ▶️
   - **方法 C**：點擊類別名稱，按 `Shift + F10`

## 方法 2：使用 Maven 工具視窗

1. **開啟 Maven 工具視窗**
   - `View` → `Tool Windows` → `Maven`
   - 或點擊右側邊欄的 `Maven` 圖示

2. **展開專案結構**
   - 展開：`order-currency-system` → `Lifecycle`

3. **執行啟動命令**
   - 雙擊 `spring-boot:run`

## 方法 3：使用終端機（在 IntelliJ 內）

1. **開啟終端機**
   - `View` → `Tool Windows` → `Terminal`
   - 或按 `Alt + F12`

2. **執行命令**
   ```bash
   mvnw.cmd spring-boot:run
   ```
   或（如果有全局 Maven）：
   ```bash
   mvn spring-boot:run
   ```

## 啟動前檢查清單

### ✅ 必要檢查：

1. **Java SDK 設定**
   - `File` → `Project Structure` (`Ctrl + Alt + Shift + S`)
   - `Project` → `SDK`：選擇 Java 17 或更高版本
   - `Project` → `Language level`：選擇 17

2. **Maven 設定**
   - `File` → `Settings` (`Ctrl + Alt + S`)
   - `Build, Execution, Deployment` → `Build Tools` → `Maven`
   - 確認 Maven 路徑正確（或使用 IntelliJ 內建 Maven）

3. **資料庫運行**
   - 確認 Docker Oracle 容器正在運行：
   ```bash
   docker ps --filter "name=order-currency-oracle"
   ```

## 啟動成功標誌

啟動成功後，在 IntelliJ 的 `Run` 視窗會看到：

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v3.2.0)

... (啟動日誌) ...

Started OrderCurrencyApplication in X.XXX seconds (process running for X.XXX)
```

## 常見問題

### 問題 1：找不到主類別
**解決**：右鍵專案根目錄 → `Maven` → `Reload Project`

### 問題 2：編譯錯誤
**解決**：
- `File` → `Invalidate Caches / Restart`
- 選擇 `Invalidate and Restart`

### 問題 3：資料庫連線失敗
**解決**：
- 檢查 `application.properties` 中的資料庫配置
- 確認 Docker 容器正在運行
- 檢查資料庫用戶名和密碼

### 問題 4：端口 8080 被占用
**解決**：
- 修改 `application.properties` 中的 `server.port=8081`
- 或關閉占用 8080 端口的其他應用

## 訪問地址

啟動成功後可以訪問：
- **後端 API**：http://localhost:8080
- **API 測試**：http://localhost:8080/api/orders

## 停止應用

- 點擊 `Run` 視窗左上角的紅色停止按鈕 ⏹️
- 或按 `Ctrl + F2`


