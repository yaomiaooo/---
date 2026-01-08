# 医院管理系统 - 分布式数据库课程设计

## 项目简介

这是一个基于分布式数据库技术的医院管理系统，采用前后端分离架构。后端使用Spring Boot + MyBatis-Plus开发，前端使用Vue 3 + Element Plus构建。系统支持多院区分布式部署，实现了医院信息管理、医生排班、预约挂号、就诊管理等核心功能。

## 技术栈

### 后端
- **框架**: Spring Boot 3.4.12
- **ORM**: MyBatis-Plus
- **数据库**: MySQL (分布式部署)
- **分片**: ShardingSphere (Apache ShardingSphere)
- **认证**: JWT (JSON Web Token)
- **文档**: RESTful API
- **Java版本**: JDK 17

### 前端
- **框架**: Vue 3 + Composition API
- **UI库**: Element Plus
- **HTTP客户端**: Axios
- **路由**: Vue Router 4
- **图标**: @iconify/vue
- **构建工具**: Vite
- **包管理**: npm/pnpm

## 系统架构

### 数据库设计
- **分片策略**: 按医院ID进行水平分片
- **主数据表**:
  - `hospital` - 医院信息
  - `department` - 科室信息
  - `doctor` - 医生信息
  - `user` - 用户信息
  - `patient` - 患者信息
  - `schedule` - 排班信息
  - `appointment` - 预约信息
  - `visit` - 就诊记录
  - `review` - 评价信息

### 核心功能模块
1. **用户管理**
   - 用户注册/登录
   - 角色权限管理 (普通用户/医生/管理员)
   - 个人信息维护

2. **医院管理**
   - 多院区支持 (朝晖院区/屏峰院区)
   - 科室信息管理
   - 医生信息管理

3. **排班管理**
   - 医生排班设置
   - 号源管理
   - 排班状态控制

4. **预约挂号**
   - 在线预约
   - 预约状态跟踪
   - 取消预约

5. **就诊管理**
   - 就诊记录
   - 诊断信息
   - 评价反馈

6. **管理员功能**
   - 系统数据管理
   - 审计日志查看
   - 统计报表

## 快速开始

### 环境要求
- JDK 17+
- MySQL 8.0+
- Node.js 16+
- Apache ShardingSphere Proxy (可选，用于分布式部署)

### 后端启动

1. 克隆项目
```bash
git clone <repository-url>
cd fenbushishujuku
```

2. 配置数据库
```sql
-- 执行初始化脚本
mysql -u root -p < data.sql
```

3. 配置application.properties
```properties
# 数据库连接配置
spring.datasource.url=jdbc:mysql://localhost:3306/hospital_db
spring.datasource.username=root
spring.datasource.password=your_password

# JWT配置
jwt.secret=your-secret-key
jwt.expire=86400

# 分片配置 (如果使用ShardingSphere)
spring.shardingsphere.enabled=true
```

4. 启动后端服务
```bash
cd hospital-backend
mvn spring-boot:run
```
服务将在 `http://localhost:8080` 启动

### 前端启动

1. 安装依赖
```bash
cd hospital-frontend
npm install
# 或使用pnpm
pnpm install
```

2. 启动开发服务器
```bash
npm run dev
# 或使用pnpm
pnpm dev
```
前端将在 `http://localhost:5173` 启动

## 项目结构

```
fenbushishujuku/
├── data.sql                           # 数据库初始化脚本
├── hospital-backend/                  # 后端项目
│   ├── src/main/java/com/example/hospital/
│   │   ├── controller/                # REST控制器
│   │   │   ├── admin/                 # 管理员接口
│   │   │   └── ...                    # 用户、医生等接口
│   │   ├── service/                   # 业务服务层
│   │   │   ├── impl/                  # 服务实现
│   │   └── entity/                    # 数据实体
│   ├── src/main/resources/
│   │   └── application.properties     # 配置文件
│   └── pom.xml                        # Maven配置
└── hospital-frontend/                 # 前端项目
    ├── src/
    │   ├── api/                       # API接口封装
    │   ├── views/                     # 页面组件
    │   ├── router/                    # 路由配置
    │   └── utils/                     # 工具函数
    ├── public/                        # 静态资源
    └── package.json                   # 项目配置
```

## API文档

### 认证相关
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 用户接口
- `GET /api/user/profile` - 获取个人信息
- `PUT /api/user/profile` - 更新个人信息

### 医生接口
- `GET /api/doctor/schedules` - 获取医生排班
- `GET /api/doctor/profile` - 获取医生信息

### 管理员接口
- `GET /api/admin/doctors` - 医生管理
- `GET /api/admin/schedules` - 排班管理
- `GET /api/admin/users` - 用户管理

## 数据库分片配置

项目使用Apache ShardingSphere实现数据分片，按医院ID进行水平分片：

```yaml
# sharding.yaml 配置示例
dataSources:
  ds_0:
    url: jdbc:mysql://localhost:3306/hospital_1
  ds_1:
    url: jdbc:mysql://localhost:3306/hospital_2

shardingRule:
  tables:
    schedule:
      actualDataNodes: ds_${0..1}.schedule_${0..1}
      databaseStrategy:
        inline:
          shardingColumn: hospital_id
          algorithmExpression: ds_${hospital_id % 2}
```

## 开发指南

### 后端开发
1. 遵循RESTful API设计规范
2. 使用DTO进行数据传输
3. 统一异常处理
4. JWT认证拦截器

### 前端开发
1. 使用Vue 3 Composition API
2. 遵循组件化开发模式
3. 统一API调用封装
4. 响应式设计适配

## 部署说明

### 单机部署
1. 启动MySQL数据库
2. 执行数据初始化脚本
3. 启动后端服务
4. 构建并部署前端

### 分布式部署
1. 配置ShardingSphere Proxy
2. 设置多数据源配置
3. 配置分片规则
4. 启动各服务节点

## 团队成员

- 项目负责人: [姓名]
- 后端开发: [姓名]
- 前端开发: [姓名]
- 数据库设计: [姓名]

## 许可证

本项目仅用于课程设计学习目的。

## 致谢

感谢所有参与项目开发的同学，感谢老师的指导和帮助。
