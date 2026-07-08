# 🏋️ 智能健身房管理系统

> Spring Boot + Vue 3 全栈项目 — 课程设计

## 📋 项目简介

本项目是一个智能健身房管理系统，包含三个子模块：

| 模块         | 技术栈                                                 | 端口   | 说明                |
| ------------ | ------------------------------------------------------ | ------ | ------------------- |
| `gym-server` | Spring Boot 3.2.5 + MyBatis-Plus + MySQL + Redis + JWT | `8080` | 后端 API 服务       |
| `gym-admin`  | Vue 3 + Vite + Element Plus + ECharts                  | `5173` | 后台管理端（PC）    |
| `gym-member` | Vue 3 + Vite + Vant                                    | `5175` | 会员端（移动端 H5） |

### 功能模块

- 🙋 会员管理（注册/登录/信用分）
- 💳 会员卡管理（月卡/季卡/年卡）
- 📅 课程预约 & 排课管理
- 👨‍🏫 教练管理 & 私教预约
- 🏷️ 器材管理 & 报修
- 📊 考勤管理
- 💰 订单 & 支付
- 🤖 AI 课程推荐（DeepSeek）

---

## 🖥️ 环境要求

| 工具    | 版本要求         | 说明                             |
| ------- | ---------------- | -------------------------------- |
| JDK     | **17+**          | 后端运行环境                     |
| Maven   | **3.6+**         | 后端构建工具                     |
| Node.js | **18+**          | 前端运行环境                     |
| MySQL   | **8.0+**         | 数据库                           |
| Redis   | **6.0+**（可选） | 缓存服务，当前配置已禁用自动配置 |

---

## 🚀 快速部署 & 运行

### 第一步：克隆项目

```bash
# 将项目克隆到本地
git clone <your-repo-url>
cd gym2
```

### 第二步：初始化数据库

1. 确保本地 MySQL 服务已启动
2. 使用 MySQL 客户端（如 Navicat、DBeaver 或命令行）连接数据库
3. 执行建表脚本：

```bash
# 方式一：命令行执行
mysql -u root -p < gym2/gym/gym-server/src/main/resources/db/schema.sql

# 方式二：复制 schema.sql 内容到数据库客户端中执行
```

4. （可选）执行测试数据：

```bash
mysql -u root -p gym_db < gym2/gym/gym-server/src/main/resources/db/data.sql
```

> 默认数据库名为 `gym_db`，用户名 `root`，密码 `123456`
> 如需修改，请编辑 `gym2/gym/gym-server/src/main/resources/application-dev.yml`

### 第三步：启动后端服务

```bash
# 进入后端目录
cd gym2/gym/gym-server

# 使用 Maven 启动（二选一）
mvn spring-boot:run

# 或者先打包再运行
mvn clean package -DskipTests
java -jar target/gym-server-1.0.0.jar
```

启动成功后访问：

- API 服务：`http://localhost:8080`
- Swagger 文档：`http://localhost:8080/swagger-ui.html`

### 第四步：启动前端（管理端 & 会员端）

打开**两个新的终端**，分别启动管理端和会员端：

**终端 2 — 管理端：**

```bash
cd gym2/gym/gym-admin
npm install    # 首次运行需要安装依赖
npm run dev
```

启动后访问：`http://localhost:5173`

**终端 3 — 会员端：**

```bash
cd gym2/gym/gym-member
npm install    # 首次运行需要安装依赖
npm run dev
```

启动后访问：`http://localhost:5175`（移动端页面，建议使用浏览器移动端模式查看）

---

## 📁 项目结构

```
gym2/
├── gym/                        # 代码主目录
│   ├── gym-server/             # 后端 Spring Boot 项目
│   │   ├── src/main/java/com/gym/
│   │   │   ├── controller/     # 控制器层
│   │   │   ├── service/        # 业务逻辑层
│   │   │   ├── mapper/         # 数据访问层
│   │   │   ├── entity/         # 实体类
│   │   │   ├── dto/            # 数据传输对象
│   │   │   ├── config/         # 配置类
│   │   │   └── common/         # 公共类
│   │   ├── src/main/resources/
│   │   │   ├── application.yml       # 主配置
│   │   │   ├── application-dev.yml   # 开发环境配置
│   │   │   ├── db/schema.sql         # 建表脚本
│   │   │   └── db/data.sql           # 测试数据
│   │   └── pom.xml             # Maven 依赖
│   ├── gym-admin/              # 后台管理端（Vue 3 + Element Plus）
│   │   ├── src/
│   │   ├── package.json
│   │   └── vite.config.js
│   └── gym-member/             # 会员移动端（Vue 3 + Vant）
│       ├── src/
│       ├── package.json
│       └── vite.config.js
└── README.md
```

---

## ⚙️ 配置说明

### 数据库配置

编辑 `gym2/gym/gym-server/src/main/resources/application-dev.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gym_db?...  # 数据库连接
    username: root                                 # 数据库用户名
    password: 123456                               # 数据库密码
```

### JWT 配置

编辑 `gym2/gym/gym-server/src/main/resources/application.yml`：

```yaml
jwt:
  secret: gymSystemSecretKey2024CourseDesign  # JWT 签名密钥
  expire: 7200000                              # Token 过期时间（毫秒），默认2小时
```

---

## 🔧 常见问题

### 1. 端口被占用？

修改对应端口：

- 后端：`application.yml` → `server.port`
- 管理端：`gym-admin/vite.config.js` → `server.port`
- 会员端：`gym-member/vite.config.js` → `server.port`

### 2. 数据库连接失败？

- 确保 MySQL 服务已启动
- 确认 `application-dev.yml` 中的用户名和密码正确
- 确认数据库 `gym_db` 已创建（schema.sql 会自动创建）

### 3. Redis 连接失败？

当前配置已禁用 Redis 自动配置，不影响系统运行。如需启用 Redis，请编辑 `application.yml`，移除 `autoconfigure.exclude` 中的 Redis 相关配置。

### 4. npm install 太慢？ 

```bash
# 使用国内镜像
npm config set registry https://registry.npmmirror.com
npm install
```

---

## 📄 License

本项目仅用于课程设计学习用途。
