# Army Type Test 测试模块

这是一个使用 Army ORM 和 Spring Boot 的 PostgreSQL 类型映射测试模块。

## 项目结构

### 核心文件

### 主要功能

1. **Postgre - PostgreSQL 类型测试实体类，支持所有常用的 PostgreSQL 类型
2. **ProductInfo** - 复合类型示例

### Service

- **PostgreService** - Postgre 类型 CRUD 服务

### Web 测试

- **PostgreTypesTest** (TestNG) - 原始 Army 原生 session 测试
- **PostgreServiceTest** (JUnit 5) - 服务层 Spring Boot Test 测试
- **PostgreControllerTest** (JUnit 5) - 控制器层 Spring Boot Test 测试 + MockMvc 测试

## 运行测试

### 使用 TestNG 运行

```bash
mvn test -Dtest=PostgreTypesTest
```

### 使用 JUnit 5 运行

```bash
mvn test
```

### 运行所有测试

```bash
mvn test
```

## 支持的类型

### 基础类型

| PostgreSQL 类型 | Java 类型 |
|---|---|
| boolean | Boolean |
| smallint | Short |
| integer | Integer |
| bigint | Long |
| decimal | BigDecimal |
| real | Float |
| double precision | Double |
| money | String |
| char | String |
| varchar | String |
| text | String |

### 日期时间类型

| PostgreSQL 类型 | Java 类型 |
|---|---|
| time | LocalTime |
| date | LocalDate |
| timestamp | LocalDateTime |
| timetz | OffsetTime |
| timestamptz | ZonedDateTime |

### 其他类型

| PostgreSQL 类型 | Java 类型 |
|---|---|
| bit | BitSet |
| bytea | byte[] |
| uuid | UUID |
| json | String |
| jsonb | String |
| xml | String |
| record | SqlRecord |
| aclitem | String |
| 复合类型 | @DefinedType |

### 数组类型

所有基础类型都支持数组类型。

## 如何使用

```java
// 创建测试数据
POST /api/postgre-types/demo

// 创建自定义记录
POST /api/postgre-types

// 查询记录
GET /api/postgre-types/{id}

// 查询第一条记录
GET /api/postgre-types/first

// 更新记录
PUT /api/postgre-types
```
