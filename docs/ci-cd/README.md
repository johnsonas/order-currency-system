# CI/CD 文檔目錄

本目錄包含 CI/CD 相關的所有文檔和配置說明。

## 📚 文檔列表

- **[CI_CD_PLAN.md](./CI_CD_PLAN.md)** - CI/CD 完整規劃文件
  - 系統架構概述
  - CI/CD 流程設計
  - 技術選項說明
  - 測試策略
  - 安全考量
  - 部署策略
  - 實施優先級

## 🔧 相關配置檔案

CI/CD 的實際配置檔案位於專案根目錄：

### GitHub Actions 工作流程
- `.github/workflows/ci.yml` - CI 流程（自動測試和構建）
- `.github/workflows/cd-dev.yml` - 開發環境自動部署
- `.github/workflows/cd-prod.yml` - 生產環境手動部署

### Docker 配置
- `Dockerfile` - 後端 Spring Boot 應用 Dockerfile
- `Dockerfile.frontend` - 前端 Vue.js 應用 Dockerfile
- `docker-compose.ci.yml` - CI 測試環境 Docker Compose

## 🚀 快速開始

### 1. 查看 CI/CD 規劃
閱讀 [CI_CD_PLAN.md](./CI_CD_PLAN.md) 了解完整的 CI/CD 架構和流程。

### 2. 檢查工作流程配置
查看 `.github/workflows/` 目錄下的工作流程檔案，了解自動化流程的詳細配置。

### 3. 本地測試 Docker 構建
```bash
# 構建後端映像
docker build -t order-currency-backend:test .

# 構建前端映像
docker build -f Dockerfile.frontend -t order-currency-frontend:test .
```

### 4. 觸發 CI 流程
- Push 到 `main` 或 `develop` 分支
- 創建 Pull Request
- 手動觸發（GitHub Actions UI）

## 📖 更多資訊

- [開發指南](../DEVELOPMENT_GUIDE.md) - 開發環境設置
- [測試指南](../TESTING_GUIDE.md) - 測試相關說明
- [架構文檔](../ARCHITECTURE.md) - 系統架構說明


