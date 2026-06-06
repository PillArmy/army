---
name: "SQLs-namedLiteral"
description: "完整的 Army Criteria API SQLs.namedLiteral() 方法学习与使用指南。涵盖声明、实现、示例、注意事项和与其他 skill 的配合使用。"
---

# SQLs.namedLiteral 完整指南

## 概述

`SQLs.namedLiteral(TypeInfer, String)` 是 Army Criteria API 中用于创建**命名字面量表达式**的方法，专门用于**批量 VALUES INSERT** 语句，将每行数据的值直接嵌入 SQL 中（而不是作为 `?` 占位符）。

## 声明位置

- 接口声明：`io.army.criteria.impl.SQLSyntax` (抽象基类)
- 实现继承：`io.army.criteria.impl.SQLs` 继承自 `SQLSyntax`
- 内部实现：`io.army.criteria.impl.ArmyLiteralExpression.ArmyNamedLiteral`
- 类型接口：`io.army.criteria.NamedLiteral`

## 方法签名

```java
public static LiteralExpression namedLiteral(TypeInfer type, String name)
```

## 实现原理

1. 在 `SQLSyntax.java` 中声明，由 `ArmyLiteralExpression.named(type, name, true)` 实现
2. `true` 参数表示启用类型前缀（`typeName` flag）
3. 返回 `ArmyNamedLiteral` 实例，该类实现了 `NamedLiteral` 接口
4. `NamedLiteral` 继承自 `SqlValueParam.NamedValue`

## 核心特性

### 1. 适用场景
- **仅用于批量 VALUES INSERT 语句**
- 不能用于批量 UPDATE 或 DELETE 语句（会抛出 `CriteriaException`）
- 在 SQL 生成时从批量行数据中解析值并直接嵌入

### 2. 与 namedParam 的区别
| 特性 | namedParam | namedLiteral |
|------|-----------|-------------|
| 输出 | `?` 占位符 | 实际值（带类型前缀） |
| 值传递 | JDBC 参数绑定 | 直接嵌入 SQL |
| 安全性 | 高（防止 SQL 注入） | 低（值直接嵌入） |
| 适用语句 | INSERT/UPDATE/DELETE | 仅 INSERT |

## 使用方式

### 方式 1：直接调用

```java
// namedLiteral 在批量 VALUES INSERT 中的使用 —— 值直接嵌入 SQL，带类型前缀
SQLs.singleInsert()
    .insertInto(Stock_.T)
    .values()
    .parens(s -> s.space(Stock_.code, SQLs.namedLiteral(StringType.INSTANCE, "code"))
            .comma(Stock_.name, SQLs.namedLiteral(StringType.INSTANCE, "name"))
    )
    .asInsert();
// 对于每一行，namedLiteral 从行数据中读取 "code" 并嵌入：
//   INSERT INTO stock(code, name) VALUES ('AAPL', 'Apple Inc.')
// PostgreSQL 带类型前缀: VALUES ('AAPL'::VARCHAR, 'Apple Inc.'::VARCHAR)
```

### 方式 2：方法引用（推荐）

```java
// 批量 VALUES INSERT：将每行值嵌入 SQL 并带类型前缀
SQLs.singleInsert()
    .insertInto(Stock_.T)
    .values()
    .parens(s -> s.space(Stock_.code, SQLs::namedLiteral, "code")  // funcRef.apply(field, "code") → TypeInfer=field, key="code"
            .comma(Stock_.name, SQLs::namedLiteral, "name")         // funcRef.apply(field, "name") → TypeInfer=field, key="name"
    )
    .asInsert();
// PostgreSQL: INSERT INTO stock(code, name) VALUES ('AAPL'::VARCHAR, 'Apple Inc.'::VARCHAR)
// MySQL:      INSERT INTO stock(code, name) VALUES ('AAPL', 'Apple Inc.')
```

## 参数说明

### 第一个参数：`TypeInfer type`
- 提供类型信息，可以是：
  - `MappingType` 实例（如 `StringType.INSTANCE`, `IntegerType.INSTANCE`）
  - `FieldMeta` 实例（如 `Stock_.code`）
  - 不能是 codec 类型的 `TableField`（会抛出异常）

### 第二个参数：`String name`
- 批量数据中的命名键
- 用于从每行数据中获取对应值
- 必须有非空文本（否则抛出 `CriteriaException`）

## 相关方法

### 1. namedConst (常量版本)

```java
public static LiteralExpression namedConst(TypeInfer type, String name)
```
- 类似 `namedLiteral`，但不输出类型前缀
- 调用：`ArmyLiteralExpression.named(type, name, false)`

### 2. 单行字面量方法
- `SQLs.literal(TypeInfer, Object)` - 单个值字面量
- `SQLs.literalValue(Object)` - 从值推断类型的字面量
- `SQLs.constValue(Object)` - 不输出类型前缀的字面量

### 3. 多行字面量方法
- `SQLs.namedRowLiteral(TypeInfer, String)` - 命名多行字面量
- `SQLs.rowLiteral(TypeInfer, Collection<?>)` - 多行字面量

## 实现细节

### ArmyNamedLiteral 内部类

```java
private static class ArmyNamedLiteral extends ArmyLiteralExpression implements NamedLiteral {
    private final String name;
    
    private ArmyNamedLiteral(String name, TypeMeta type, boolean typeName) {
        super(type, typeName);
        this.name = name;
    }
    
    @Override
    public final String name() {
        return this.name;
    }
    
    @Override
    public void appendSql(StringBuilder sqlBuilder, _SqlContext context) {
        context.appendLiteral(this, this.typeName);
    }
    
    @Override
    public final String toString() {
        return "{LITERAL:" + this.name + "}";
    }
}
```

## 注意事项

1. **仅用于 VALUES INSERT**：在批量 UPDATE/DELETE 中使用会抛出 `CriteriaException`
2. **命名键必须存在**：确保批量数据中包含指定的命名键
3. **SQL 注入风险**：由于值直接嵌入 SQL，确保输入值安全或使用 `namedParam` 替代
4. **类型验证**：`TypeInfer` 不能返回 codec 类型的 `TableField`
5. **名称不能为空**：`name` 参数必须有文本内容

## 与其他 Skill 配合使用

### 相关 Skills

1. **TypedField-complete-guide** - 理解 `TypeInfer` 和 `FieldMeta` 的使用
2. **army-named-param-guide** - 学习 `namedParam` 的使用
3. **army-row-param** - 学习多行参数的使用
4. **MySQLs-values-stmt** - 学习 MySQL VALUES 语句构建
5. **Postgres-values-stmt** - 学习 PostgreSQL VALUES 语句构建

## 示例代码

### 完整示例

```java
import io.army.criteria.Insert;
import io.army.criteria.impl.SQLs;
import io.army.mapping.StringType;
import io.army.example.stock.domain.Stock_;
import java.util.List;

public class NamedLiteralExample {
    
    public Insert buildBatchInsert(List<StockData> stockList) {
        return SQLs.singleInsert()
            .insertInto(Stock_.T)
            .values()
            .parens(s -> s.space(Stock_.code, SQLs::namedLiteral, "code")
                    .comma(Stock_.name, SQLs::namedLiteral, "name")
                    .comma(Stock_.price, SQLs::namedLiteral, "price")
            )
            .values(stockList)  // 提供批量数据
            .asInsert();
    }
    
    public static class StockData {
        public String code;
        public String name;
        public double price;
    }
}
```

## 演进路径

该 skill 设计为可自我进化，当发现以下情况时应更新：
1. Army 框架版本更新导致 API 变更
2. 发现更优的使用模式
3. 发现文档中的错误或不一致
4. 新增相关功能或方法
