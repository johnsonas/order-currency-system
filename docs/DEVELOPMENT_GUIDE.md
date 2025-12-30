# 開發指南

## 概述

本文檔提供 Order Currency System 的開發環境設置、編碼規範、專案結構和開發流程說明。

---

## 技術棧

### 後端
- **框架：** Spring Boot 3.2.0
- **Java 版本：** 17
- **資料庫：** Oracle Database
- **ORM：** Spring Data JPA / Hibernate
- **建置工具：** Maven
- **驗證：** Jakarta Bean Validation

### 前端
- **框架：** Vue 3
- **建置工具：** Vite
- **HTTP 客戶端：** Axios

### 開發工具
- **IDE：** IntelliJ IDEA（推薦）或 Eclipse
- **版本控制：** Git
- **容器化：** Docker / Docker Compose

---

## 環境設置

### 1. 必要軟體安裝

#### Java Development Kit (JDK)
- **版本：** JDK 17 或以上
- **下載：** [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) 或 [Eclipse Adoptium](https://adoptium.net/)
- **驗證：**
  ```bash
  java -version
  javac -version
  ```

#### Maven
- **版本：** 3.6+ 
- **下載：** [Apache Maven](https://maven.apache.org/download.cgi)
- **驗證：**
  ```bash
  mvn -version
  ```

#### Node.js 和 npm
- **版本：** Node.js 18+ / npm 9+
- **下載：** [Node.js](https://nodejs.org/)
- **驗證：**
  ```bash
  node -version
  npm -version
  ```

#### Docker（可選，用於資料庫）
- **下載：** [Docker Desktop](https://www.docker.com/products/docker-desktop/)
- **驗證：**
  ```bash
  docker --version
  docker-compose --version
  ```

### 2. 專案克隆

```bash
git clone <repository-url>
cd order-currency-system
```

### 3. 資料庫設置

#### 方式一：使用 Docker（推薦）

**Windows:**
```bash
start-docker-db.bat
```

**Linux/Mac:**
```bash
chmod +x start-docker-db.sh
./start-docker-db.sh
```

**或手動啟動:**
```bash
docker-compose -f docker-compose-oracle-free.yml up -d
```

等待約 1-2 分鐘讓資料庫完全啟動，查看日誌：
```bash
docker logs -f order-currency-oracle
```

看到 `DATABASE IS READY TO USE!` 表示啟動完成。

**連線資訊：**
- URL: `jdbc:oracle:thin:@localhost:1521/FREEPDB1`
- Username: `ordersystem`
- Password: `ordersystem123`

#### 方式二：使用本地 Oracle 資料庫

修改 `src/main/resources/application.properties`：
```properties
spring.datasource.url=jdbc:oracle:thin:@localhost:1521:XE
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. 後端設置

#### 安裝依賴
```bash
mvn clean install
```

#### 啟動後端
```bash
# 方式一：使用 Maven
mvn spring-boot:run

# 方式二：使用腳本（Windows）
start-spring-boot.bat

# 方式三：使用 IDE
# 運行 OrderCurrencyApplication.java 的 main 方法
```

後端服務將運行在 `http://localhost:8080`

### 5. 前端設置

```bash
cd frontend
npm install
npm run dev
```

前端服務將運行在 `http://localhost:5173`

### 6. 一鍵啟動（Windows）

```bash
start-all.bat
```

此腳本會：
1. 啟動前端（等待就緒）
2. 啟動後端
3. 自動檢測並設置 `JAVA_HOME`

---

## 專案結構

```
order-currency-system/
├── docs/                          # 文檔目錄
│   ├── API_DOCUMENTATION.md       # API 文檔
│   ├── DATABASE_SCHEMA.md         # 資料庫設計文檔
│   └── DEVELOPMENT_GUIDE.md       # 開發指南（本文件）
├── frontend/                      # 前端專案
│   ├── src/
│   │   ├── App.vue               # 主應用組件
│   │   ├── main.js               # 入口文件
│   │   └── style.css             # 樣式文件
│   ├── package.json               # 前端依賴配置
│   └── vite.config.js            # Vite 配置
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/ordersystem/
│       │       ├── OrderCurrencyApplication.java  # 主應用類
│       │       ├── controller/                   # 控制器層
│       │       │   ├── OrderController.java
│       │       │   └── CurrencyController.java
│       │       ├── service/                     # 服務層
│       │       │   ├── OrderService.java
│       │       │   └── CurrencyService.java
│       │       ├── repository/                  # 資料存取層
│       │       │   ├── OrderRepository.java
│       │       │   └── CurrencyRepository.java
│       │       └── model/                        # 實體模型
│       │           ├── Order.java
│       │           ├── Currency.java
│       │           └── CurrencyCode.java        # 幣別枚舉
│       └── resources/
│           ├── application.properties            # 應用配置
│           ├── application-docker.properties    # Docker 配置
│           ├── schema.sql                       # 資料庫 Schema
│           └── data.sql                         # 初始化資料
├── target/                        # Maven 編譯輸出
├── pom.xml                        # Maven 配置
├── .gitignore                     # Git 忽略文件
├── README.md                      # 專案說明
└── docker-compose-oracle-free.yml # Docker Compose 配置
```

---

## 編碼規範

### Java 編碼規範

#### 1. 命名規範
- **類別名稱：** PascalCase（如：`OrderService`）
- **方法名稱：** camelCase（如：`getAllOrders`）
- **變數名稱：** camelCase（如：`orderId`）
- **常數名稱：** UPPER_SNAKE_CASE（如：`MAX_RETRY_COUNT`）
- **包名稱：** 小寫，使用點分隔（如：`com.example.ordersystem`）

#### 2. 註解規範
- 所有公開類別和方法必須有 JavaDoc 註解
- 使用 `@author`、`@param`、`@return`、`@throws` 等標籤
- 複雜邏輯必須有行內註解說明

**範例：**
```java
/**
 * 取得所有訂單列表
 * 按照建立時間降序排列（最新的訂單在前）
 * 
 * @return 所有訂單的列表，按建立時間降序排列
 */
public List<Order> getAllOrders() {
    return orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
}
```

#### 3. 異常處理
- 使用明確的異常類型（`IllegalArgumentException`、`IllegalStateException` 等）
- 提供有意義的錯誤訊息
- 不要吞掉異常（catch 後必須處理）

**範例：**
```java
if (amount == null) {
    throw new IllegalArgumentException("金額不能為空");
}
```

#### 4. 空值處理
- 使用 `Optional` 處理可能為 null 的返回值
- 方法參數必須進行 null 檢查
- 使用防禦性編程

**範例：**
```java
public Optional<Order> getOrderById(Long orderId) {
    return orderRepository.findById(orderId);
}
```

#### 5. 金額計算
- 使用 `BigDecimal` 進行金額計算
- 使用 `setScale()` 設定精度
- 使用 `RoundingMode.HALF_UP` 進行四捨五入

**範例：**
```java
BigDecimal finalAmount = amount.multiply(rate)
    .setScale(2, RoundingMode.HALF_UP);
```

### Vue 編碼規範

#### 1. 組件命名
- 使用 PascalCase（如：`OrderList.vue`）

#### 2. 變數命名
- 使用 camelCase（如：`orderList`）

#### 3. 方法命名
- 使用 camelCase（如：`loadOrders`）

#### 4. 樣式規範
- 使用 scoped 樣式避免污染
- 優先使用 class 而非 inline style

---

## 開發流程

### 1. 功能開發流程

1. **建立功能分支**
   ```bash
   git checkout -b feature/your-feature-name
   ```

2. **開發功能**
   - 編寫 Model（如需要）
   - 編寫 Repository
   - 編寫 Service（業務邏輯）
   - 編寫 Controller（API 端點）
   - 編寫前端組件（如需要）

3. **編寫測試**
   - 單元測試
   - 整合測試

4. **提交代碼**
   ```bash
   git add .
   git commit -m "feat: 描述你的功能"
   ```

5. **推送並建立 Pull Request**
   ```bash
   git push origin feature/your-feature-name
   ```

### 2. Bug 修復流程

1. **建立修復分支**
   ```bash
   git checkout -b fix/your-bug-description
   ```

2. **修復 Bug**
   - 重現問題
   - 定位問題
   - 修復問題
   - 驗證修復

3. **提交修復**
   ```bash
   git commit -m "fix: 描述修復的問題"
   ```

### 3. 代碼審查

- 所有 Pull Request 必須經過代碼審查
- 審查重點：
  - 代碼風格是否符合規範
  - 是否有潛在的 Bug
  - 效能是否合理
  - 是否有適當的測試

---

## Git 工作流程

### 分支策略

- **main**: 主分支，用於生產環境
- **develop**: 開發分支（可選）
- **feature/***: 功能分支
- **fix/***: Bug 修復分支

### Commit 訊息規範

使用 [Conventional Commits](https://www.conventionalcommits.org/) 規範：

- `feat:` 新功能
- `fix:` Bug 修復
- `docs:` 文檔更新
- `style:` 代碼格式調整（不影響功能）
- `refactor:` 代碼重構
- `test:` 測試相關
- `chore:` 建置過程或輔助工具的變動

**範例：**
```
feat: 新增訂單搜尋功能
fix: 修復匯率為 0 時的除零錯誤
docs: 更新 API 文檔
```

---

## 測試

### 單元測試

測試文件位於 `src/test/java/` 目錄下。

**執行測試：**
```bash
mvn test
```

**執行特定測試類：**
```bash
mvn test -Dtest=OrderServiceTest
```

### 整合測試

使用 Spring Boot Test 進行整合測試。

**範例：**
```java
@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void testGetAllOrders() throws Exception {
        mockMvc.perform(get("/api/orders"))
            .andExpect(status().isOk());
    }
}
```

---

## 除錯技巧

### 後端除錯

1. **啟用 SQL 日誌**
   在 `application.properties` 中：
   ```properties
   spring.jpa.show-sql=true
   spring.jpa.properties.hibernate.format_sql=true
   ```

2. **使用 IDE 除錯器**
   - 設置斷點
   - 啟動 Debug 模式
   - 逐步執行

3. **查看日誌**
   ```bash
   tail -f logs/application.log
   ```

### 前端除錯

1. **使用瀏覽器開發者工具**
   - F12 開啟開發者工具
   - Console 查看錯誤訊息
   - Network 查看 API 請求

2. **使用 Vue DevTools**
   - 安裝 Vue DevTools 擴充功能
   - 查看組件狀態和資料流

---

## 常見問題

### 1. JAVA_HOME 未設置

**錯誤訊息：**
```
Error: JAVA_HOME not found in your environment.
```

**解決方法：**
- Windows: 設置環境變數 `JAVA_HOME` 指向 JDK 安裝目錄
- Linux/Mac: 在 `~/.bashrc` 或 `~/.zshrc` 中設置：
  ```bash
  export JAVA_HOME=/path/to/jdk
  ```

### 2. 資料庫連線失敗

**錯誤訊息：**
```
ORA-12505: TNS:listener does not currently know of SID given in connect descriptor
```

**解決方法：**
- 確認資料庫是否已啟動
- 檢查 `application.properties` 中的連線資訊
- 確認 Docker 容器是否運行：`docker ps`

### 3. 端口被佔用

**錯誤訊息：**
```
Port 8080 is already in use
```

**解決方法：**
- 更改 `application.properties` 中的 `server.port`
- 或關閉佔用端口的程序

### 4. 前端無法連線後端

**錯誤訊息：**
```
CORS policy: No 'Access-Control-Allow-Origin' header
```

**解決方法：**
- 確認 `@CrossOrigin` 註解已設置
- 確認前端 URL 與後端允許的來源一致

---

## 效能優化建議

1. **資料庫查詢**
   - 使用索引欄位進行查詢
   - 避免 N+1 查詢問題
   - 使用分頁查詢大量資料

2. **API 設計**
   - 使用適當的 HTTP 狀態碼
   - 避免返回過大的響應體
   - 使用快取（如需要）

3. **前端優化**
   - 使用防抖（debounce）減少 API 呼叫
   - 使用虛擬滾動處理大量列表
   - 優化圖片和資源載入

---

## 參考資源

- [Spring Boot 官方文檔](https://spring.io/projects/spring-boot)
- [Vue 3 官方文檔](https://vuejs.org/)
- [Oracle Database 文檔](https://docs.oracle.com/en/database/)
- [Maven 官方文檔](https://maven.apache.org/guides/)

---

## 聯絡資訊

如有問題或建議，請聯繫專案維護者或建立 Issue。



