# 測試資料說明

## 測試訂單資料

已建立 `src/main/resources/test-orders.sql` 檔案，包含 70 筆測試訂單資料。

### 資料特點

- **總筆數**: 70 筆訂單
- **使用者**: 7 位使用者（user1, user2, user3, alice, bob, charlie, david）
- **幣別**: 涵蓋所有支援的幣別（TWD, USD, EUR, JPY, CNY）
- **狀態**: 包含所有狀態（PENDING, CONFIRMED, CANCELLED, COMPLETED）
- **折扣**: 0% 到 25% 不等
- **時間順序**: 使用 `SYSDATE - N` 建立不同的時間順序，方便測試分頁排序功能

### 如何使用

#### 方式一：使用 SQL 客戶端工具執行

1. 連接到 Oracle 資料庫
2. 執行 `src/main/resources/test-orders.sql` 檔案

#### 方式二：使用 Docker 容器執行

```bash
# 複製 SQL 檔案到容器
docker cp src/main/resources/test-orders.sql order-currency-oracle:/tmp/test-orders.sql

# 進入容器
docker exec -it order-currency-oracle bash

# 使用 sqlplus 執行（需要根據實際的資料庫設定調整）
sqlplus sys/Oracle123@FREEPDB1 as sysdba @/tmp/test-orders.sql
```

#### 方式三：使用 Spring Boot 應用程式執行

如果您的應用程式有設定自動執行 SQL 檔案，可以將 `test-orders.sql` 的內容加入到 `data.sql` 中，或使用 Spring Boot 的 `@Sql` 註解在測試中執行。

### 資料分布

- **user1**: 10 筆訂單
- **user2**: 10 筆訂單
- **user3**: 10 筆訂單
- **alice**: 10 筆訂單
- **bob**: 10 筆訂單
- **charlie**: 10 筆訂單
- **david**: 10 筆訂單

### 注意事項

1. **ORDER_ID**: 會由 SEQUENCE 自動產生，不需要手動指定
2. **FINAL_AMOUNT**: 已根據 AMOUNT 和 DISCOUNT 計算完成
3. **時間戳記**: 使用 `SYSDATE - N` 建立不同的建立時間，確保資料有時間順序
4. **重複執行**: 如果重複執行此腳本，會產生重複的訂單資料

### 驗證資料

執行後可以使用以下 SQL 查詢驗證：

```sql
-- 檢查總筆數
SELECT COUNT(*) FROM ORDERS;

-- 檢查各使用者的訂單數
SELECT USERNAME, COUNT(*) as ORDER_COUNT 
FROM ORDERS 
GROUP BY USERNAME 
ORDER BY ORDER_COUNT DESC;

-- 檢查各狀態的訂單數
SELECT STATUS, COUNT(*) as ORDER_COUNT 
FROM ORDERS 
GROUP BY STATUS;

-- 檢查各幣別的訂單數
SELECT CURRENCY, COUNT(*) as ORDER_COUNT 
FROM ORDERS 
GROUP BY CURRENCY;
```


