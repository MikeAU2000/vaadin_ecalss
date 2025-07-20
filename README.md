# E-Class 電子課堂系統

這是一個基於 Vaadin 和 Spring Boot 開發的電子課堂管理系統，支持管理員、老師和學生三種角色。

## 功能特性

### 管理員功能
- 用戶管理：新增、編輯、刪除老師和學生賬戶
- 用戶狀態管理：啟用/禁用用戶賬戶
- 系統統計：查看用戶數量統計
- 用戶搜索和篩選功能

### 老師功能
- 作業管理：創建、編輯、刪除作業
- 查看學生提交情況
- 作業評分和評語
- 統計信息：查看作業數量和待評分數量

### 學生功能
- 查看所有作業列表
- 提交作業（支持重新提交）
- 查看提交記錄和評分結果
- 查看老師評語

## 技術棧

- **後端框架**: Spring Boot 3.2.0
- **前端框架**: Vaadin 24.2.5
- **安全框架**: Spring Security
- **數據庫**: H2 (內存數據庫)
- **ORM**: Spring Data JPA
- **構建工具**: Maven

## 快速開始

### 環境要求
- Java 17 或更高版本
- Maven 3.6 或更高版本

### 運行步驟

1. 克隆項目到本地
```bash
git clone <repository-url>
cd vaadin_adminproject
```

2. 編譯和運行項目
```bash
mvn clean install
mvn spring-boot:run
```

3. 打開瀏覽器訪問 `http://localhost:8080`

### 默認測試賬戶

系統會自動創建以下測試賬戶：

| 角色 | 用戶名 | 密碼 | 姓名 |
|------|--------|------|------|
| 管理員 | admin | admin123 | 系統管理員 |
| 老師 | teacher1 | teacher123 | 張老師 |
| 學生 | student1 | student123 | 李小明 |
| 學生 | student2 | student123 | 王小華 |

## 數據庫訪問

開發環境下可以通過 H2 控制台查看數據庫：
- URL: `http://localhost:8080/h2-console`
- JDBC URL: `jdbc:h2:mem:eclassdb`
- 用戶名: `sa`
- 密碼: `password`

## 項目結構

```
src/main/java/com/example/eclass/
├── config/          # 配置類
│   ├── DataInitializer.java    # 數據初始化
│   └── SecurityConfig.java     # 安全配置
├── entity/          # 實體類
│   ├── User.java
│   ├── Role.java
│   ├── Assignment.java
│   └── Submission.java
├── repository/      # 數據訪問層
│   ├── UserRepository.java
│   ├── AssignmentRepository.java
│   └── SubmissionRepository.java
├── service/         # 業務邏輯層
│   ├── UserService.java
│   ├── AssignmentService.java
│   └── SubmissionService.java
├── security/        # 安全相關
│   ├── CustomUserDetailsService.java
│   └── SecurityUtils.java
├── view/           # 視圖層
│   ├── LoginView.java
│   ├── MainLayout.java
│   ├── HomeView.java
│   ├── admin/
│   │   └── AdminDashboardView.java
│   ├── teacher/
│   │   └── TeacherDashboardView.java
│   └── student/
│       └── StudentDashboardView.java
└── EClassApplication.java  # 主應用類
```

## 主要功能說明

### 用戶認證和授權
- 使用 Spring Security 進行用戶認證
- 基於角色的訪問控制 (RBAC)
- 密碼使用 BCrypt 加密

### 數據模型
- **User**: 用戶實體，包含用戶名、密碼、姓名、郵箱和角色
- **Assignment**: 作業實體，包含標題、描述、截止時間和發布老師
- **Submission**: 提交實體，包含學生提交的內容、提交時間、分數和評語

### 界面特性
- 響應式設計，支持不同屏幕尺寸
- 現代化的 Material Design 風格
- 直觀的操作界面和友好的用戶體驗

## 開發說明

### 添加新功能
1. 在相應的 service 層添加業務邏輯
2. 在 repository 層添加數據訪問方法
3. 在 view 層創建或修改用戶界面
4. 根據需要更新安全配置

### 數據庫遷移
當前使用 H2 內存數據庫，如需切換到持久化數據庫：
1. 修改 `application.properties` 中的數據庫配置
2. 添加相應的數據庫驅動依賴
3. 調整 JPA 配置

## 許可證

本項目僅供學習和演示使用。