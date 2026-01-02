# 測試文檔目錄

本目錄包含所有測試相關的文檔和指南。

## 📚 文檔列表

### 測試指南
- **[TESTING_GUIDE.md](./TESTING_GUIDE.md)** - 完整的測試指南
  - 測試類型說明（單元測試、整合測試等）
  - 測試工具和框架
  - 測試輔助功能
  - 測試最佳實踐

### API 測試
- **[api-testing/](./api-testing/)** - API 測試工具和範例
  - Postman Collection
  - cURL 命令範例
  - RestAssured 測試代碼

## 🧪 測試類型

### 1. 單元測試（Unit Tests）
- **位置：** `src/test/java/com/example/ordersystem/service/`
- **用途：** 測試單個類別或方法的邏輯
- **工具：** JUnit 5, Mockito

### 2. 整合測試（Integration Tests）
- **位置：** `src/test/java/com/example/ordersystem/integration/`
- **用途：** 測試多個組件之間的整合
- **工具：** Spring Boot Test, Testcontainers

### 3. Controller 測試
- **位置：** `src/test/java/com/example/ordersystem/controller/`
- **用途：** 測試 REST API 端點
- **工具：** MockMvc, WebMvcTest

### 4. Repository 測試
- **位置：** `src/test/java/com/example/ordersystem/repository/`
- **用途：** 測試資料存取層
- **工具：** @DataJpaTest

## 🚀 快速開始

### 運行所有測試
```bash
mvn test
```

### 運行特定測試類別
```bash
mvn test -Dtest=OrderServiceTest
```

### 運行整合測試
```bash
mvn verify
```

### 查看測試覆蓋率
```bash
mvn jacoco:report
# 報告位置：target/site/jacoco/index.html
```

## 📖 相關文檔

- [開發指南](../DEVELOPMENT_GUIDE.md) - 開發環境設置
- [API 文檔](../API_DOCUMENTATION.md) - API 端點說明
- [CI/CD 規劃](../ci-cd/CI_CD_PLAN.md) - CI/CD 中的測試流程


