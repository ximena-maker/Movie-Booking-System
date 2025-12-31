# Movie Booking System (電影訂票系統)

## 專案簡介

這是一個使用 Java 和 JavaFX 開發的電影訂票系統，提供完整的電影票預訂、付款、退票等功能。系統包含多個模組，涵蓋使用者認證、電影搜尋、座位選擇、付款處理、推薦系統等功能。

## 專案結構

```
Movie-Booking-System/
├── src/main/java/org/example/
│   ├── data/           # 資料層
│   ├── model/          # 資料模型
│   ├── service/        # 業務邏輯服務
│   ├── util/           # 工具類
│   ├── App.java        # JavaFX 主應用程式
│   └── Main.java       # 程式進入點
├── target/             # 編譯輸出目錄
└── pom.xml            # Maven 專案配置檔
```

## 檔案說明

### data 資料夾

#### DataStore.java
- **作用**: 系統的記憶體資料庫，負責儲存所有系統資料
- **功能**: 
  - 管理使用者、電影、影城、場次、訂單、票券等資料
  - 提供座位對應表 (seatMap)
  - 管理折扣碼和電影熱門度統計
  - 包含 `bootstrapSample()` 方法用於初始化範例資料

### model 資料夾

#### Booking.java
- **作用**: 訂單資料模型
- **欄位**: 訂單ID、使用者ID、場次ID、座位清單、票種、餐點組合、折扣金額、總價、訂單狀態、建立時間

#### BookingStatus.java
- **作用**: 訂單狀態列舉
- **狀態**: CREATED (已建立)、PAID (已付款)、CANCELED (已取消)、REFUNDED (已退款)

#### MealCombo.java
- **作用**: 餐點組合列舉
- **選項**: NONE (無)、POPCORN_COLA (爆米花可樂)、COUPLE_SET (情侶套餐)

#### Movie.java
- **作用**: 電影資料模型
- **欄位**: 電影ID、片名、分級

#### PaymentResult.java
- **作用**: 付款結果資料模型
- **欄位**: 成功與否 (boolean)、訊息 (String)

#### Showtime.java
- **作用**: 電影場次資料模型
- **欄位**: 場次ID、影城ID、電影ID、開始時間、基礎票價

#### Theater.java
- **作用**: 影城資料模型
- **欄位**: 影城ID、名稱、座標 (x, y) - 用於計算距離

#### Ticket.java
- **作用**: 電影票資料模型
- **欄位**: 票券ID、訂單ID、座位ID、電子票券代碼

#### TicketType.java
- **作用**: 票種列舉
- **類型**: ADULT (成人票)、STUDENT (學生票)、EARLY_BIRD (早鳥票)

#### User.java
- **作用**: 使用者資料模型
- **欄位**: 使用者ID、使用者名稱、密碼雜湊值

### service 資料夾

#### AuthService.java
- **作用**: 使用者認證服務
- **功能**:
  - 使用者登入驗證 (login)
  - 修改密碼 (changePassword)
  - 忘記密碼處理 (forgotPassword) - 自動產生隨機密碼
  - 密碼雜湊處理

#### BookingService.java
- **作用**: 訂票服務
- **功能**:
  - 自動選位 (autoSelectSeats) - 挑選前面可用的座位
  - 列出可用座位 (listAvailableSeats)
  - 建立訂單 (createBooking)
  - 計算價格 (含票種、餐點組合、折扣碼)
  - 查詢訂單

#### IDValidator.java
- **作用**: 身分證驗證服務
- **功能**: 驗證台灣身分證字號格式是否正確

#### PaymentService.java
- **作用**: 付款服務
- **功能**:
  - 處理信用卡付款
  - 模擬付款流程（示範用）
  - 付款成功後更新訂單狀態
  - 生成電子票券

#### PriceCompareService.java
- **作用**: 價格比較服務
- **功能**:
  - 比較不同影城同一部電影的票價
  - 提供價格排序和篩選功能

#### RecommendationService.java
- **作用**: 電影推薦服務
- **功能**:
  - 基於熱門度推薦電影
  - 根據使用者所在位置推薦最近的影城

#### RefundService.java
- **作用**: 退票服務
- **功能**:
  - 處理退票申請
  - 計算退款金額（可能扣除手續費）
  - 釋放座位
  - 更新訂單狀態

#### SearchService.java
- **作用**: 搜尋服務
- **功能**:
  - 依電影名稱搜尋
  - 依地點搜尋影城
  - 依條件篩選場次

### 主程式檔案

#### App.java
- **作用**: JavaFX 主應用程式
- **功能**:
  - 建立圖形化使用者介面
  - 管理各種場景（登入、電影選擇、座位選擇、付款等）
  - 整合所有服務模組
  - 處理使用者互動

#### Main.java
- **作用**: 程式進入點
- **功能**: 啟動 JavaFX 應用程式（範例檔案）

## 主要功能

1. **使用者管理**
   - 登入/登出
   - 密碼修改
   - 忘記密碼處理

2. **電影瀏覽**
   - 電影列表顯示
   - 電影搜尋
   - 電影推薦

3. **訂票流程**
   - 選擇電影和場次
   - 自動或手動選座
   - 選擇票種（成人/學生/早鳥）
   - 選擇餐點組合
   - 輸入折扣碼

4. **付款處理**
   - 信用卡付款
   - 產生電子票券
   - 訂單確認

5. **訂單管理**
   - 查詢訂單記錄
   - 退票申請
   - 退款處理

6. **輔助功能**
   - 價格比較
   - 最近影城推薦
   - 身分證驗證

## 技術架構

- **開發語言**: Java
- **UI 框架**: JavaFX
- **專案管理**: Maven
- **設計模式**: 
  - Service 層模式（業務邏輯分離）
  - Record 類別（不可變資料模型）
  - Enum 列舉（狀態和類型定義）

## 執行方式

1. 確保已安裝 Java JDK (建議 17 以上)
2. 確保已安裝 Maven
3. 下載專案：
   ```bash
   git clone https://github.com/ximena-maker/Movie-Booking-System.git
   cd Movie-Booking-System/Movie-Booking-System
   ```
4. 編譯專案：
   ```bash
   mvn clean compile
   ```
5. 執行應用程式：
   ```bash
   mvn javafx:run
   ```

## 範例資料

系統啟動時會自動載入範例資料，包括：
- 測試使用者：alice (密碼: 1234)、bob (密碼: abcd)
- 電影：Interstellar、Spirited Away、Avengers
- 影城：台北信義影城、新北板橋影城、桃園中壢影城
- 折扣碼：OFF50 (折50元)、OFF100 (折100元)

## 開發者

- GitHub: [ximena-maker](https://github.com/ximena-maker)

## 授權

本專案為教育用途，請勿用於商業目的。
