# 權限管理系統使用指南

## 概述

系統已整合 Spring Security 和 JWT (JSON Web Token) 認證機制，提供完整的權限管理功能。

## 功能特點

1. **用戶認證**：使用 JWT token 進行無狀態認證
2. **角色管理**：支援 ADMIN 和 USER 兩種角色
3. **權限控制**：基於角色的訪問控制 (RBAC)

## API 端點

### 1. 註冊新用戶

**POST** `/api/auth/register`

請求體：
```json
{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com"
}
```

回應：
```json
{
  "message": "註冊成功",
  "username": "testuser",
  "email": "test@example.com"
}
```

### 2. 登入

**POST** `/api/auth/login`

請求體：
```json
{
  "username": "admin",
  "password": "admin123"
}
```

回應：
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "type": "Bearer",
  "username": "admin"
}
```

## 權限配置

### 公開端點（無需認證）
- `/api/auth/**` - 認證相關端點
- `/swagger-ui/**` - API 文檔
- `/api/currencies` - 查看所有匯率
- `/api/currencies/{code}` - 查看單一匯率
- `/api/currencies/convert` - 匯率轉換

### 需要登入的端點（USER 或 ADMIN）
- `/api/orders/**` - 所有訂單操作
- `/api/currencies/**` - 幣別管理（除了公開端點）

### 僅管理員端點（ADMIN）
- `/api/currencies/refresh` - 手動刷新匯率
- `/api/currencies/auto-update/**` - 自動更新管理
- `/api/currencies` (POST/PUT/DELETE) - 創建/更新/刪除幣別
- `/api/orders/{id}` (DELETE) - 刪除訂單

## 使用方式

### 1. 註冊用戶

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "password123",
    "email": "newuser@example.com"
  }'
```

### 2. 登入獲取 Token

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "password": "password123"
  }'
```

### 3. 使用 Token 訪問受保護的端點

```bash
curl -X GET http://localhost:8080/api/orders \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## 預設帳號

系統啟動時會自動創建預設管理員帳號：
- **使用者名稱**：`admin`
- **密碼**：`admin123`
- **角色**：ADMIN

⚠️ **安全提醒**：生產環境請務必修改預設管理員密碼！

## 角色說明

### USER 角色
- 可以查看和創建訂單
- 可以查看匯率資訊
- 可以進行匯率轉換

### ADMIN 角色
- 擁有 USER 的所有權限
- 可以管理幣別（創建、更新、刪除）
- 可以手動刷新匯率
- 可以管理自動更新功能
- 可以刪除訂單

## JWT Token 配置

在 `application.properties` 中可配置：

```properties
# JWT 密鑰（生產環境請使用更安全的密鑰）
jwt.secret=mySecretKeyForJWTTokenGenerationThatShouldBeAtLeast256BitsLongForSecurity
# Token 過期時間（毫秒），預設 24 小時
jwt.expiration=86400000
```

## 前端整合範例

### JavaScript/TypeScript

```javascript
// 登入
async function login(username, password) {
  const response = await fetch('http://localhost:8080/api/auth/login', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ username, password })
  });
  
  const data = await response.json();
  if (data.token) {
    localStorage.setItem('token', data.token);
  }
  return data;
}

// 使用 Token 發送請求
async function fetchOrders() {
  const token = localStorage.getItem('token');
  const response = await fetch('http://localhost:8080/api/orders', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return response.json();
}
```

## 安全建議

1. **使用 HTTPS**：生產環境必須使用 HTTPS 傳輸
2. **強密碼策略**：要求用戶使用強密碼
3. **Token 過期時間**：根據需求調整 token 過期時間
4. **密鑰管理**：JWT 密鑰應使用環境變數或密鑰管理服務
5. **定期更新密鑰**：定期輪換 JWT 密鑰

## 故障排除

### Token 無效
- 檢查 token 是否過期
- 確認 Authorization header 格式正確：`Bearer <token>`
- 確認 token 未損壞

### 權限不足
- 確認用戶角色是否正確
- 檢查 SecurityConfig 中的權限配置
- 確認 Controller 方法上的 `@PreAuthorize` 註解

