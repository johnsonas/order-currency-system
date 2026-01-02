# API 測試文檔

本目錄包含多種 API 測試工具和範例，方便您選擇適合的方式進行 API 測試。

## 📁 文件列表

### 1. **Postman Collection**
- **文件：** `API_TEST_COLLECTION.postman_collection.json`
- **用途：** 可直接導入 Postman 進行 API 測試
- **使用方式：**
  1. 打開 Postman
  2. 點擊 "Import"
  3. 選擇 `API_TEST_COLLECTION.postman_collection.json`
  4. 設定環境變數 `baseUrl` 為 `http://localhost:8080`

### 2. **cURL 命令範例**
- **文件：** `API_TEST_CURL.md`
- **用途：** 提供所有 API 的 cURL 命令，可直接在終端機執行
- **適用場景：**
  - 快速測試單個 API
  - CI/CD 腳本中執行
  - 不需要 GUI 工具的環境

### 3. **RestAssured 測試代碼**
- **文件：** `API_TEST_RESTASSURED.md`
- **用途：** Java 程式碼範例，使用 RestAssured 進行 API 測試
- **適用場景：**
  - 整合到單元測試中
  - 自動化 API 測試
  - 回歸測試

## 🔗 相關文檔

- [API 文檔](../API_DOCUMENTATION.md) - 完整的 API 端點說明
- [OpenAPI 規範](../openapi.yaml) - OpenAPI/Swagger 規範文件

