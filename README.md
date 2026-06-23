# Campus Dining

校园自助点餐系统，包含学生点餐端、商家管理端和管理员端。

## 技术栈

- Java 17、Spring Boot 3.2
- MyBatis-Plus、MySQL
- JWT、BCrypt
- Vue 3、Vite、Element Plus
- Maven、JUnit 5

## 项目结构

```text
.
├─ src/main/java/com/campus
│  ├─ common       公共配置、异常、返回结构和权限控制
│  ├─ controller   学生、商家、管理员和认证接口
│  ├─ dto          请求参数
│  ├─ entity       数据库实体
│  ├─ mapper       MyBatis-Plus Mapper
│  ├─ service      业务服务
│  ├─ utils        JWT 等工具
│  └─ vo           接口响应模型
├─ src/main/resources
│  ├─ static       构建后的前端资源和学生端页面
│  └─ application.yml
├─ src/test        后端自动化测试
├─ front           Vue 3 商家端和管理员端源码
├─ docs            项目说明文档
└─ image           上传图片目录
```

## 本地运行

要求：JDK 17、Maven 3.9、Node.js、MySQL 8。

先创建数据库：

```sql
CREATE DATABASE campus_dining
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
```

配置环境变量。PowerShell 示例：

```powershell
$env:DB_USERNAME="root"
$env:DB_PASSWORD="你的数据库密码"
$env:JWT_SECRET="请替换为至少32字节的随机密钥"
```

也可以参考 [.env.example](.env.example)，但 Spring Boot 不会自动加载 `.env` 文件，需要把变量设置到操作系统或 IDE 的运行配置中。

启动后端：

```powershell
mvn spring-boot:run
```

访问地址：

- 学生端：`http://localhost:8080/student`
- 商家端：`http://localhost:8080/login`
- 管理员端：`http://localhost:8080/admin/login`
- 接口文档：`http://localhost:8080/doc.html`

开发前端：

```powershell
cd front
npm install
npm run dev
```

## 权限控制

登录后 JWT 包含用户 ID 和角色。后端通过 `@RequireRole` 统一授权：

- `STUDENT`：学生端接口
- `MERCHANT`：商家端接口
- `ADMIN`：管理员端接口

没有有效 Token 返回 HTTP 401；Token 有效但角色不匹配返回 HTTP 403。前端路由守卫只改善页面体验，最终权限以后端校验为准。

## 测试

```powershell
mvn test
```

当前权限测试覆盖 Token 缺失、角色不匹配、同角色放行、方法级权限覆盖和非法角色解析。

## 安全说明

- 不要提交真实数据库密码、JWT 密钥或 `.env` 文件。
- 公开过的密码和密钥应立即更换，删除当前文件不能清除 Git 历史中的旧值。
- 生产环境应限制跨域来源、关闭调试日志，并使用独立的高强度 JWT 密钥。
