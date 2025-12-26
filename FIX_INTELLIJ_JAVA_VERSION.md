# 修復 IntelliJ IDEA Java 版本錯誤

## 錯誤訊息
```
java: java.lang.ExceptionInInitializerError
com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

## 問題原因
IntelliJ IDEA 的專案設定使用了 Java 25，但專案需要 Java 17。

## 解決方法

### 方法 1：在 IntelliJ IDEA 中設定（推薦）

1. **開啟專案結構設定**
   - 按 `Ctrl + Alt + Shift + S`
   - 或 `File` → `Project Structure`

2. **設定 Project SDK**
   - 點擊左側的 `Project`
   - `SDK`：選擇 `17`（或 `openjdk-17`）
   - `Language level`：選擇 `17 - Sealed types, always-strict floating-point semantics`

3. **設定 Modules**
   - 點擊左側的 `Modules`
   - 選擇 `order-currency-system`
   - `Language level`：選擇 `17 - Sealed types, always-strict floating-point semantics`

4. **設定 SDKs（如果沒有 Java 17）**
   - 點擊左側的 `SDKs`
   - 如果沒有 Java 17，點擊 `+` → `Add JDK`
   - 選擇你的 Java 17 安裝路徑（通常是 `C:\Program Files\Java\jdk-17` 或類似）

5. **重新載入專案**
   - `File` → `Invalidate Caches / Restart`
   - 選擇 `Invalidate and Restart`

### 方法 2：檢查 Java 版本

在 IntelliJ IDEA 的終端機中執行：
```bash
java -version
```

應該顯示：
```
openjdk version "17.0.17"
```

### 方法 3：重新導入 Maven 專案

1. 右鍵點擊 `pom.xml`
2. 選擇 `Maven` → `Reload Project`

## 驗證修復

重新啟動後，應該可以正常編譯和運行。

## 如果還是有問題

1. **清理編譯**
   - `Build` → `Rebuild Project`

2. **檢查編譯器設定**
   - `File` → `Settings` (`Ctrl + Alt + S`)
   - `Build, Execution, Deployment` → `Compiler` → `Java Compiler`
   - `Project bytecode version`：選擇 `17`
   - `Per-module bytecode version`：確認所有模組都是 `17`


