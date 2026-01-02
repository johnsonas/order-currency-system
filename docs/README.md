# 文檔目錄

歡迎來到 Order Currency System 文檔中心。本文檔目錄提供了系統的完整文檔索引。

## 📚 核心文檔

### 系統文檔
- **[架構文檔](./ARCHITECTURE.md)** - 系統整體架構說明
- **[資料庫架構](./DATABASE_SCHEMA.md)** - 資料庫表結構和關係
- **[開發指南](./DEVELOPMENT_GUIDE.md)** - 開發環境設置和開發規範
- **[測試文檔](./testing/)** - 測試相關說明和指南

### API 文檔
- **[API 文檔](./API_DOCUMENTATION.md)** - 完整的 REST API 端點說明
- **[OpenAPI 規範](./openapi.yaml)** - OpenAPI/Swagger 規範文件

### 功能文檔
- **[排程器說明](./SCHEDULER_EXPLANATION.md)** - 匯率自動更新排程器原理說明
- **[訂單查詢流程](./ORDER_QUERY_FLOW.md)** - 訂單查詢功能的詳細流程說明

### CI/CD 文檔
- **[CI/CD 規劃](./ci-cd/)** - CI/CD 流程規劃和配置說明

## 🗂️ 文檔結構

```
docs/
├── README.md                    # 本文檔（文檔索引）
├── ARCHITECTURE.md              # 系統架構
├── DATABASE_SCHEMA.md           # 資料庫架構
├── DEVELOPMENT_GUIDE.md        # 開發指南
├── TESTING_GUIDE.md            # 測試指南
├── API_DOCUMENTATION.md         # API 文檔
├── openapi.yaml                 # OpenAPI 規範
├── SCHEDULER_EXPLANATION.md     # 排程器說明
├── ORDER_QUERY_FLOW.md         # 訂單查詢流程
├── api-testing/                 # API 測試相關
│   ├── README.md
│   ├── API_TEST_CURL.md
│   ├── API_TEST_RESTASSURED.md
│   └── API_TEST_COLLECTION.postman_collection.json
└── ci-cd/                       # CI/CD 相關
    ├── README.md
    └── CI_CD_PLAN.md
```

## 🚀 快速開始

### 新開發者
1. 閱讀 [開發指南](./DEVELOPMENT_GUIDE.md) 設置開發環境
2. 查看 [架構文檔](./ARCHITECTURE.md) 了解系統架構
3. 參考 [API 文檔](./API_DOCUMENTATION.md) 了解 API 使用方式

### 測試相關
1. 查看 [測試文檔](./testing/README.md) 了解測試策略
2. 查看 [API 測試工具](./testing/api-testing/README.md) 選擇適合的測試方式

### 部署相關
1. 查看 [CI/CD 規劃](./ci-cd/CI_CD_PLAN.md)
2. 參考 `.github/workflows/` 目錄下的工作流程配置

## 📝 文檔維護

- 文檔應保持最新，與代碼同步更新
- 重大變更應更新相關文檔
- 如有疑問或發現文檔錯誤，請提交 Issue 或 Pull Request

