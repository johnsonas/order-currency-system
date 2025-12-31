# 排程任務原理說明

## 🤔 問題：為什麼不會一直掃描？

### 傳統的錯誤理解（會一直掃描）：
```java
// ❌ 錯誤方式：會一直掃描，浪費 CPU
while (true) {
    Thread.sleep(1000); // 每秒檢查一次
    if (現在是整點) {
        執行任務();
    }
}
```
**問題**：每秒都在檢查時間，浪費 CPU 資源！

---

## ✅ 正確的方式：使用定時器（Timer）

### 1. 原理示意圖

```
現在時間：13:30:00
  ↓
startScheduledTask() 被調用
  ↓
CronTrigger 計算：下一次執行時間是 14:00:00
  ↓
告訴系統：「在 14:00:00 叫醒我」
  ↓
系統設置一個「鬧鐘」（定時器）
  ↓
線程進入「睡眠狀態」（不消耗 CPU）
  ↓
等待 30 分鐘...
  ↓
14:00:00 到了！
  ↓
系統「叫醒」線程
  ↓
執行 updateExchangeRates()
  ↓
CronTrigger 重新計算：下一次是 15:00:00
  ↓
再次設置「鬧鐘」，繼續等待...
```

### 2. 實際代碼流程

```java
// 步驟 1：創建 CronTrigger（計算下一次執行時間）
CronTrigger trigger = new CronTrigger("0 0 * * * ?");
// 內部會計算：現在是 13:30，下一次整點是 14:00

// 步驟 2：告訴 TaskScheduler 在指定時間執行
scheduledTask = taskScheduler.schedule(
    this::updateExchangeRates,  // 要執行的任務
    trigger                      // 什麼時候執行
);

// 步驟 3：TaskScheduler 內部使用 ScheduledExecutorService
// 類似於：
// executor.schedule(task, delay, TimeUnit.MILLISECONDS);
// delay = 14:00:00 - 13:30:00 = 30 分鐘
```

---

## 🔍 深入理解：ScheduledExecutorService

### Java 底層實現

```java
// Spring TaskScheduler 內部使用 ScheduledExecutorService
ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

// schedule() 方法會：
// 1. 計算延遲時間（delay）
// 2. 將任務放入「延遲隊列」（DelayQueue）
// 3. 線程從隊列中取出任務，等待到指定時間才執行
executor.schedule(
    () -> updateExchangeRates(),
    30,  // 延遲 30 分鐘
    TimeUnit.MINUTES
);
```

### DelayQueue 的工作原理

```
DelayQueue（延遲隊列）：
┌─────────────────────────┐
│ 任務1: 14:00:00 執行    │ ← 線程在這裡等待
│ 任務2: 15:00:00 執行    │
│ 任務3: 16:00:00 執行    │
└─────────────────────────┘

線程狀態：
- 不是「一直檢查」
- 而是「等待隊列中的第一個任務到期」
- 使用 wait() 或 park() 進入睡眠狀態
- 不消耗 CPU！
```

---

## 📊 資源消耗對比

### ❌ 輪詢方式（每秒檢查）
```
CPU 使用率：持續 1-5%
記憶體：低
問題：每秒都在檢查，浪費資源
```

### ✅ 定時器方式（我們的實現）
```
CPU 使用率：0%（等待期間）
記憶體：極低（只保存一個任務引用）
優點：只在指定時間執行，不浪費資源
```

---

## 🎯 CronTrigger 如何計算下一次執行時間？

### Cron 表達式：`"0 0 * * * ?"`

```
秒 分 時 日 月 星期
0  0  *  *  *  ?
│  │  │  │  │  └─ 星期（? 表示不指定）
│  │  │  │  └─── 月份（* 表示每月）
│  │  │  └───── 日期（* 表示每天）
│  │  └─────── 小時（* 表示每小時）
│  └───────── 分鐘（0 表示第 0 分）
└─────────── 秒（0 表示第 0 秒）

意思：每小時的第 0 分 0 秒執行
例如：00:00:00, 01:00:00, 02:00:00...
```

### 計算邏輯（簡化版）

```java
// CronTrigger 內部邏輯（簡化版）
public Date nextExecutionTime(Date lastExecutionTime) {
    Calendar cal = Calendar.getInstance();
    cal.setTime(lastExecutionTime);
    
    // 現在是 13:30:00
    // 設置為下一個整點：14:00:00
    cal.set(Calendar.MINUTE, 0);
    cal.set(Calendar.SECOND, 0);
    cal.add(Calendar.HOUR, 1); // 加 1 小時
    
    return cal.getTime(); // 返回 14:00:00
}
```

---

## 🔄 完整執行流程

### 時間線

```
13:30:00 - 應用啟動
    ↓
    startScheduledTask() 被調用
    ↓
    CronTrigger 計算：下一次是 14:00:00
    ↓
    線程進入等待狀態（不消耗 CPU）
    
13:31:00 - 線程在等待...
13:32:00 - 線程在等待...
...
13:59:59 - 線程在等待...

14:00:00 - ⏰ 時間到了！
    ↓
    系統喚醒線程
    ↓
    執行 updateExchangeRates()
    ↓
    呼叫 ExchangeRate-API
    ↓
    更新資料庫和 Redis
    ↓
    CronTrigger 重新計算：下一次是 15:00:00
    ↓
    線程再次進入等待狀態

14:01:00 - 線程在等待...
14:02:00 - 線程在等待...
...
```

---

## 💡 關鍵點總結

1. **不會一直掃描**
   - 使用定時器機制，只在指定時間執行
   - 等待期間線程處於睡眠狀態，不消耗 CPU

2. **CronTrigger 的作用**
   - 計算下一次執行時間
   - 不需要每秒檢查，只需要計算一次

3. **TaskScheduler 的作用**
   - 使用 ScheduledExecutorService
   - 將任務放入延遲隊列
   - 線程等待隊列中的任務到期

4. **資源消耗**
   - CPU：等待期間為 0%
   - 記憶體：只保存任務引用
   - 執行頻率：每小時一次

---

## 🧪 驗證方式

如果想驗證不會一直掃描，可以：

1. **查看線程狀態**
   ```bash
   # 使用 jstack 或 JVisualVM
   # 會看到線程處於 WAITING 狀態，不是 RUNNABLE
   ```

2. **查看 CPU 使用率**
   ```bash
   # 使用 top 或 htop
   # 等待期間 CPU 使用率應該接近 0%
   ```

3. **添加日誌**
   ```java
   // 在 updateExchangeRates() 中添加日誌
   // 會發現只在整點執行，不會每秒都執行
   ```


