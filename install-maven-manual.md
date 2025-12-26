# Maven 手動安裝指南

## 步驟 1：下載 Maven

1. 訪問：https://maven.apache.org/download.cgi
2. 下載：`apache-maven-3.9.x-bin.zip`（最新版本）
3. 解壓縮到：`C:\Program Files\Apache\maven`

## 步驟 2：設定環境變數

### 方法 A：使用圖形介面

1. 按 `Win + R`，輸入 `sysdm.cpl`，按 Enter
2. 點擊「進階」標籤
3. 點擊「環境變數」
4. 在「系統變數」中：
   - 點擊「新增」
   - 變數名稱：`MAVEN_HOME`
   - 變數值：`C:\Program Files\Apache\maven`
   - 確定
5. 編輯 `Path` 變數：
   - 選擇 `Path` → 編輯
   - 新增：`%MAVEN_HOME%\bin`
   - 確定

### 方法 B：使用 PowerShell（管理員）

```powershell
# 設定 MAVEN_HOME
[Environment]::SetEnvironmentVariable("MAVEN_HOME", "C:\Program Files\Apache\maven", "Machine")

# 新增到 PATH
$currentPath = [Environment]::GetEnvironmentVariable("Path", "Machine")
$newPath = $currentPath + ";%MAVEN_HOME%\bin"
[Environment]::SetEnvironmentVariable("Path", $newPath, "Machine")
```

## 步驟 3：驗證安裝

重新開啟命令提示字元（重要！），執行：

```bash
mvn -version
```

應該會看到 Maven 版本資訊。

## 步驟 4：啟動後端

```bash
mvn spring-boot:run
```

## 如果遇到問題

- 確保 Java 已安裝：`java -version`
- 確保環境變數已正確設定
- 重新開啟命令提示字元（環境變數變更需要重新開啟）


