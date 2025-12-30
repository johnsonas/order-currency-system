# 資料庫設計文檔

## 概述

本文檔描述了 Order Currency System 的資料庫結構設計，包括表結構、索引、序列和關係說明。

**資料庫類型：** Oracle Database  
**字符集：** UTF-8

---

## 資料庫表結構

### 1. CURRENCIES（幣別表）

儲存系統支援的所有幣別及其對 TWD 的匯率資訊。

| 欄位名稱 | 資料類型 | 約束 | 說明 |
|---------|---------|------|------|
| CURRENCY_CODE | VARCHAR2(3) | PRIMARY KEY, NOT NULL | 幣別代碼（TWD, USD, EUR, JPY, CNY） |
| RATE_TO_TWD | NUMBER(19, 6) | NOT NULL | 對 TWD 的匯率（1 單位該幣別 = RATE_TO_TWD TWD） |
| LAST_UPDATE | TIMESTAMP | NULL | 最後更新時間 |

**索引：**
- `PRIMARY KEY (CURRENCY_CODE)`: 主鍵索引

**範例資料：**
```sql
CURRENCY_CODE | RATE_TO_TWD | LAST_UPDATE
--------------|-------------|-------------
TWD           | 1.000000    | 2024-01-01 10:00:00
USD           | 31.250000   | 2024-01-01 10:00:00
EUR           | 34.480000   | 2024-01-01 10:00:00
JPY           | 0.220000    | 2024-01-01 10:00:00
CNY           | 4.350000    | 2024-01-01 10:00:00
```

**說明：**
- `RATE_TO_TWD` 表示：1 單位該幣別 = RATE_TO_TWD TWD
- 例如：1 USD = 31.25 TWD，所以 USD 的 `RATE_TO_TWD` = 31.25
- TWD 的 `RATE_TO_TWD` 固定為 1.0（基準幣別）

---

### 2. ORDERS（訂單表）

儲存所有訂單資訊，包括金額、幣別、狀態、折扣等。

| 欄位名稱 | 資料類型 | 約束 | 說明 |
|---------|---------|------|------|
| ORDER_ID | NUMBER | PRIMARY KEY | 訂單ID（由序列自動產生） |
| USERNAME | VARCHAR2(50) | NOT NULL | 使用者名稱 |
| AMOUNT | NUMBER(19, 2) | NOT NULL | 訂單原始金額 |
| CURRENCY | VARCHAR2(3) | NOT NULL | 幣別代碼（對應 CURRENCIES.CURRENCY_CODE） |
| STATUS | VARCHAR2(20) | DEFAULT 'PENDING' | 訂單狀態（PENDING, CONFIRMED, CANCELLED, COMPLETED） |
| DISCOUNT | NUMBER(5, 2) | DEFAULT 0 | 折扣百分比（0-100） |
| FINAL_AMOUNT | NUMBER(19, 2) | NULL | 最終金額（扣除折扣後） |
| CREATED_AT | TIMESTAMP | NULL | 建立時間 |
| UPDATED_AT | TIMESTAMP | NULL | 更新時間 |

**索引：**
- `PRIMARY KEY (ORDER_ID)`: 主鍵索引
- `IDX_ORDERS_USERNAME`: 使用者名稱索引（提升查詢效能）
- `IDX_ORDERS_STATUS`: 訂單狀態索引（提升查詢效能）
- `IDX_ORDERS_CURRENCY`: 幣別索引（提升查詢效能）
- `IDX_ORDERS_CREATED_AT`: 建立時間降序索引（用於排序，提升查詢效能）
- `IDX_ORDERS_ORDER_ID_STR`: 函數索引 `TO_CHAR(ORDER_ID)`（用於 ORDER_ID 字串搜尋，大幅提升 LIKE 查詢效能）

**範例資料：**
```sql
ORDER_ID | USERNAME | AMOUNT   | CURRENCY | STATUS    | DISCOUNT | FINAL_AMOUNT | CREATED_AT          | UPDATED_AT
---------|----------|----------|----------|-----------|----------|--------------|---------------------|-------------------
1        | user1    | 1000.00  | USD      | PENDING   | 0.00     | 1000.00      | 2024-01-01 10:00:00 | 2024-01-01 10:00:00
2        | user2    | 5000.00  | TWD      | CONFIRMED | 10.00    | 4500.00      | 2024-01-02 11:00:00 | 2024-01-02 11:00:00
3        | user3    | 200.00   | EUR      | COMPLETED | 5.00     | 190.00       | 2024-01-03 12:00:00 | 2024-01-03 12:00:00
```

**說明：**
- `ORDER_ID` 由序列 `ORDER_SEQ` 自動產生
- `FINAL_AMOUNT` 由系統自動計算：`FINAL_AMOUNT = AMOUNT - (AMOUNT × DISCOUNT / 100)`
- `CREATED_AT` 和 `UPDATED_AT` 由 JPA `@PrePersist` 和 `@PreUpdate` 自動管理
- `STATUS` 預設值為 'PENDING'
- `DISCOUNT` 預設值為 0

---

## 序列（Sequences）

### ORDER_SEQ（訂單序號）

用於自動產生訂單ID。

```sql
CREATE SEQUENCE ORDER_SEQ
    START WITH 1
    INCREMENT BY 1
    NOCACHE
    NOCYCLE;
```

**說明：**
- 起始值：1
- 增量：1
- 不使用快取（NOCACHE）
- 不循環（NOCYCLE）

---

## 索引設計

### 基本索引

1. **IDX_ORDERS_USERNAME**
   - 欄位：`ORDERS.USERNAME`
   - 用途：加速根據使用者名稱查詢訂單
   - 使用場景：`GET /api/orders/username/{username}`

2. **IDX_ORDERS_STATUS**
   - 欄位：`ORDERS.STATUS`
   - 用途：加速根據訂單狀態查詢
   - 使用場景：`GET /api/orders/status/{status}`

3. **IDX_ORDERS_CURRENCY**
   - 欄位：`ORDERS.CURRENCY`
   - 用途：加速根據幣別查詢訂單
   - 使用場景：根據幣別篩選訂單

### 效能優化索引

4. **IDX_ORDERS_CREATED_AT**
   - 欄位：`ORDERS.CREATED_AT DESC`
   - 類型：降序索引
   - 用途：加速按建立時間降序排序（最新的在前）
   - 使用場景：`GET /api/orders`（預設排序）

5. **IDX_ORDERS_ORDER_ID_STR**
   - 欄位：`TO_CHAR(ORDER_ID)`
   - 類型：函數索引
   - 用途：加速 ORDER_ID 的字串模糊搜尋（LIKE '%...%'）
   - 使用場景：`GET /api/orders?searchOrderId=...`
   - **說明：** 這個索引會將 `ORDER_ID` 轉換為字串後建立索引，讓 `LIKE '%...%'` 查詢可以使用索引，大幅提升搜尋效能

---

## 資料關係

### 外鍵關係（邏輯）

雖然資料庫中沒有定義外鍵約束，但存在邏輯關係：

- `ORDERS.CURRENCY` → `CURRENCIES.CURRENCY_CODE`
  - 訂單的幣別必須存在於幣別表中
  - 由應用層進行驗證

---

## 資料完整性約束

### 主鍵約束

- `CURRENCIES.CURRENCY_CODE`: PRIMARY KEY
- `ORDERS.ORDER_ID`: PRIMARY KEY

### 非空約束

- `CURRENCIES.CURRENCY_CODE`: NOT NULL
- `CURRENCIES.RATE_TO_TWD`: NOT NULL
- `ORDERS.USERNAME`: NOT NULL
- `ORDERS.AMOUNT`: NOT NULL
- `ORDERS.CURRENCY`: NOT NULL

### 預設值

- `ORDERS.STATUS`: DEFAULT 'PENDING'
- `ORDERS.DISCOUNT`: DEFAULT 0

### 檢查約束（應用層）

- `ORDERS.AMOUNT`: 必須 > 0
- `ORDERS.DISCOUNT`: 必須在 0-100 之間
- `CURRENCIES.RATE_TO_TWD`: 必須 > 0

---

## 資料類型說明

### NUMBER(p, s)

- `NUMBER(19, 2)`: 總位數 19，小數位數 2（用於金額）
- `NUMBER(19, 6)`: 總位數 19，小數位數 6（用於匯率）
- `NUMBER(5, 2)`: 總位數 5，小數位數 2（用於折扣百分比）

### VARCHAR2(n)

- `VARCHAR2(3)`: 幣別代碼（固定 3 個字符）
- `VARCHAR2(20)`: 訂單狀態
- `VARCHAR2(50)`: 使用者名稱

### TIMESTAMP

- 用於儲存日期和時間（包含時區資訊）

---

## 初始化資料

### 幣別初始化資料

系統預設支援以下幣別：

```sql
INSERT INTO CURRENCIES (CURRENCY_CODE, RATE_TO_TWD, LAST_UPDATE) 
VALUES ('TWD', 1.000000, SYSDATE);

INSERT INTO CURRENCIES (CURRENCY_CODE, RATE_TO_TWD, LAST_UPDATE) 
VALUES ('USD', 31.250000, SYSDATE);

INSERT INTO CURRENCIES (CURRENCY_CODE, RATE_TO_TWD, LAST_UPDATE) 
VALUES ('EUR', 34.480000, SYSDATE);

INSERT INTO CURRENCIES (CURRENCY_CODE, RATE_TO_TWD, LAST_UPDATE) 
VALUES ('JPY', 0.220000, SYSDATE);

INSERT INTO CURRENCIES (CURRENCY_CODE, RATE_TO_TWD, LAST_UPDATE) 
VALUES ('CNY', 4.350000, SYSDATE);
```

---

## 效能優化建議

1. **索引使用**
   - 所有常用查詢欄位都已建立索引
   - `CREATED_AT` 降序索引用於預設排序
   - `ORDER_ID` 函數索引用於字串搜尋

2. **查詢優化**
   - 使用索引欄位進行查詢和排序
   - 避免在非索引欄位上進行 LIKE 查詢

3. **資料量考量**
   - 當訂單數量超過百萬筆時，考慮分區表（Partitioning）
   - 考慮建立複合索引（如：USERNAME + CREATED_AT）

---

## 資料庫腳本位置

- **Schema 建立腳本：** `src/main/resources/schema.sql`
- **初始化資料腳本：** `src/main/resources/data.sql`

---

## 注意事項

1. **幣別代碼**
   - 使用 Enum (`CurrencyCode`) 確保類型安全
   - 資料庫中儲存為字串（VARCHAR2）

2. **金額精度**
   - 使用 `NUMBER(19, 2)` 確保金額精度
   - 應用層使用 `BigDecimal` 進行計算

3. **時間戳記**
   - `CREATED_AT` 和 `UPDATED_AT` 由 JPA 自動管理
   - 使用 `TIMESTAMP` 類型儲存完整時間資訊

4. **匯率更新**
   - 更新匯率時會自動更新 `LAST_UPDATE` 欄位
   - 建議定期更新匯率以保持準確性



