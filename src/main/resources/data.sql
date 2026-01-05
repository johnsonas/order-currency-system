-- 初始化幣別資料
-- 注意：Oracle 需要使用不同的方式初始化資料，這裡僅作為參考

-- 插入幣別資料
-- RATE_TO_TWD 表示：1 單位該幣別 = RATE_TO_TWD TWD
-- 例如：1 USD = 31.25 TWD，所以 USD 的 RATE_TO_TWD = 31.25
INSERT INTO CURRENCIES (CURRENCY_CODE, RATE_TO_TWD, LAST_UPDATE) VALUES ('TWD', 1.000000, SYSDATE);
INSERT INTO CURRENCIES (CURRENCY_CODE, RATE_TO_TWD, LAST_UPDATE) VALUES ('USD', 31.250000, SYSDATE);
INSERT INTO CURRENCIES (CURRENCY_CODE, RATE_TO_TWD, LAST_UPDATE) VALUES ('EUR', 34.480000, SYSDATE);
INSERT INTO CURRENCIES (CURRENCY_CODE, RATE_TO_TWD, LAST_UPDATE) VALUES ('JPY', 0.220000, SYSDATE);
INSERT INTO CURRENCIES (CURRENCY_CODE, RATE_TO_TWD, LAST_UPDATE) VALUES ('CNY', 4.350000, SYSDATE);

-- 插入範例訂單資料
-- 注意：ORDER_ID 會由 SEQUENCE 自動產生
INSERT INTO ORDERS (USERNAME, AMOUNT, CURRENCY, STATUS, DISCOUNT, FINAL_AMOUNT, CREATED_AT, UPDATED_AT) 
VALUES ('user1', 1000.00, 'USD', 'PENDING', 0.00, 1000.00, SYSDATE, SYSDATE);

INSERT INTO ORDERS (USERNAME, AMOUNT, CURRENCY, STATUS, DISCOUNT, FINAL_AMOUNT, CREATED_AT, UPDATED_AT) 
VALUES ('user2', 5000.00, 'TWD', 'CONFIRMED', 10.00, 4500.00, SYSDATE, SYSDATE);

INSERT INTO ORDERS (USERNAME, AMOUNT, CURRENCY, STATUS, DISCOUNT, FINAL_AMOUNT, CREATED_AT, UPDATED_AT) 
VALUES ('user3', 200.00, 'EUR', 'COMPLETED', 5.00, 190.00, SYSDATE, SYSDATE);

-- ============================================
-- 用戶和權限管理初始數據
-- ============================================

-- 插入角色數據
-- 注意：ROLE_ID 會由 SEQUENCE 自動產生
INSERT INTO ROLES (ROLE_NAME) VALUES ('ADMIN');
INSERT INTO ROLES (ROLE_NAME) VALUES ('USER');

-- 插入預設管理員用戶
-- 密碼：admin123 (BCrypt 加密後的結果，實際使用時會由 DataInitializer 自動創建)
-- 這裡提供一個範例，實際密碼會由 Spring Security 的 BCryptPasswordEncoder 加密
-- 注意：USER_ID 會由 SEQUENCE 自動產生
-- 密碼欄位需要存儲 BCrypt 加密後的結果，這裡僅作為參考
-- 實際應用中，DataInitializer 會自動創建管理員帳號

-- 插入測試用戶（可選）
-- 注意：實際使用時，建議通過註冊 API 創建用戶，因為密碼需要 BCrypt 加密
-- 以下僅作為範例，實際密碼需要先加密

-- 如果需要手動插入用戶，可以使用以下方式（需要先加密密碼）：
-- INSERT INTO USERS (USERNAME, PASSWORD, EMAIL, ENABLED, CREATED_AT, UPDATED_AT) 
-- VALUES ('testuser', '$2a$10$加密後的密碼', 'test@example.com', 1, SYSDATE, SYSDATE);

-- 插入用戶角色關聯（假設管理員的 USER_ID 是 1，ADMIN 角色的 ROLE_ID 是 1）
-- 注意：實際的 ID 會根據 SEQUENCE 自動產生，這裡僅作為參考
-- 實際應用中，DataInitializer 會自動處理角色關聯

-- 範例：將 USER_ID=1 的用戶關聯到 ROLE_ID=1 (ADMIN)
-- INSERT INTO USER_ROLES (USER_ID, ROLE_ID) VALUES (1, 1);

-- 範例：將 USER_ID=2 的用戶關聯到 ROLE_ID=2 (USER)
-- INSERT INTO USER_ROLES (USER_ID, ROLE_ID) VALUES (2, 2);

-- 注意：由於密碼需要 BCrypt 加密，建議通過以下方式創建用戶：
-- 1. 使用註冊 API (/api/auth/register)
-- 2. 使用 DataInitializer 自動創建預設管理員
-- 3. 如果需要手動插入，請先使用 BCryptPasswordEncoder 加密密碼

-- ============================================
-- 選單初始數據
-- ============================================

-- 插入選單數據
-- 注意：MENU_ID 會由 SEQUENCE 自動產生
-- 如果選單不需要特定角色（所有登入用戶都可以訪問），則不插入 MENU_ROLES 記錄

-- 訂單列表選單（所有登入用戶都可以訪問）
INSERT INTO MENUS (MENU_KEY, LABEL, ICON, ROUTE, SORT_ORDER, ENABLED) 
VALUES ('orders', '訂單列表', '📋', 'orders', 1, 1);

-- 幣別轉換系統選單（所有登入用戶都可以訪問）
INSERT INTO MENUS (MENU_KEY, LABEL, ICON, ROUTE, SORT_ORDER, ENABLED) 
VALUES ('currency', '幣別轉換系統', '💱', 'currency', 2, 1);

-- 匯率管理選單（僅管理員，需要關聯 ADMIN 角色）
INSERT INTO MENUS (MENU_KEY, LABEL, ICON, ROUTE, SORT_ORDER, ENABLED) 
VALUES ('rates', '匯率管理', '📊', 'rates', 3, 1);

-- 注意：選單與角色的關聯會在 DataInitializer 中自動建立
-- 或者可以手動插入（需要先知道 MENU_ID 和 ROLE_ID）：
-- INSERT INTO MENU_ROLES (MENU_ID, ROLE_ID) VALUES (3, 1); -- 假設匯率管理的 MENU_ID 是 3，ADMIN 的 ROLE_ID 是 1

