# Oracle SQL Developer 安裝與連線設定

## 下載建議

**下載：Windows 64-bit with JDK 17 included (555 MB)**

✅ **優點：**
- 已包含 JDK 17，不需要另外安裝 Java
- 64-bit 版本，適合現代 Windows 系統
- 一步到位，安裝簡單

## 安裝步驟

1. **下載檔案**
   - 檔案名稱類似：`sqldeveloper-xx.x.x-windows-x64.zip`
   - 解壓縮到任意目錄（例如：`C:\sqldeveloper`）

2. **執行 SQL Developer**
   - 進入解壓縮的目錄
   - 執行 `sqldeveloper.exe`

3. **首次啟動**
   - 可能會詢問 Java 路徑（如果使用內含 JDK 的版本，會自動找到）
   - 接受授權協議

## 連線設定步驟

### 1. 建立新連線

1. 點擊左上角的「+」圖示（新增連線）
2. 或：File → New → Database Connection

### 2. 填入連線資訊

```
連線名稱: Docker Oracle
使用者名稱: sys
密碼: Oracle123
角色: SYSDBA
主機名稱: localhost
端口: 1521
服務名稱: freepdb1
```

**重要設定：**
- ✅ **服務名稱**：`freepdb1`（不是 SID）
- ✅ **角色**：選擇 `SYSDBA`（使用 sys 時）
- ✅ **儲存密碼**：可選，方便下次連線

### 3. 測試連線

1. 點擊「測試」按鈕
2. 如果成功，狀態會顯示「成功」
3. 點擊「連線」儲存並連線

## 如果連線失敗

### 檢查項目：

1. **Docker 容器是否運行**
   ```bash
   docker ps | findstr oracle
   ```

2. **服務名稱是否正確**
   - 使用 `freepdb1`（小寫）
   - 不是 `FREEPDB1` 或 `FREE`

3. **端口是否正確**
   - 確認是 `1521`

4. **密碼是否正確**
   - 確認是 `Oracle123`（大小寫敏感）

## 使用 SQL Developer

### 常用功能：

1. **查看資料表**
   - 展開連線 → Tables
   - 雙擊資料表名稱查看資料

2. **執行 SQL**
   - 點擊「SQL Worksheet」圖示
   - 輸入 SQL 並執行（F5 或 Ctrl+Enter）

3. **查看結構**
   - 右鍵資料表 → Describe

## 建立專案專用使用者（建議）

連線成功後，建議建立專用使用者：

```sql
-- 切換到可插拔資料庫
ALTER SESSION SET CONTAINER = FREEPDB1;

-- 建立使用者
CREATE USER ordersystem IDENTIFIED BY ordersystem123;

-- 授予權限
GRANT CONNECT, RESOURCE, DBA TO ordersystem;

-- 設定表空間配額
ALTER USER ordersystem QUOTA UNLIMITED ON USERS;

-- 授予所有權限
GRANT ALL PRIVILEGES TO ordersystem;
```

然後可以用 `ordersystem` 使用者連線（不需要 SYSDBA 角色）。

## 常見問題

### Q: 找不到 Java？

A: 如果下載的是「with JDK 17 included」版本，應該不會有這個問題。如果還是有問題，可以：
- 下載並安裝 JDK 17：https://www.oracle.com/java/technologies/downloads/#java17
- 在 SQL Developer 設定中指定 Java 路徑

### Q: 連線超時？

A: 檢查 Docker 容器狀態：
```bash
docker logs order-currency-oracle --tail 20
```

### Q: 找不到服務名稱？

A: 確認使用 `freepdb1`（小寫），不是 `FREEPDB1` 或 `FREE`

## 優勢

相比 DBeaver，Oracle SQL Developer：
- ✅ Oracle 官方工具，對 Oracle 支援最好
- ✅ 連線設定更簡單
- ✅ 不會有引號或 URL 格式問題
- ✅ 免費且穩定


