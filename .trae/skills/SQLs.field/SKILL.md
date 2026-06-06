---
name: "SQLs.field"
description: "完整学习 SQLs.field() 系列方法的声明、实现与使用，包括 field()、refField()、refTypedField()、refSelection() 等。涵盖 QualifiedField、DerivedField 接口，以及自连接、派生表引用和选择项引用等场景。"
---

# SQLs.field() 系列方法完整指南

## 概述

SQLs.field() 系列方法用于获取和引用字段对象，包括：
- `SQLs.field(String, FieldMeta<T>)` - 获取带表别名限定的字段
- `SQLs.refField(String, String)` - 引用派生表字段
- `SQLs.refTypedField(String, String)` - 引用类型化的派生表字段
- `SQLs.refSelection(String)` - 引用当前语句的选择项（别名）
- `SQLs.refSelection(int)` - 引用当前语句的选择项（序号）

## 一、方法声明与实现

### 1. SQLs.field(String tableAlias, FieldMeta<T> field)

**声明位置**: `SQLSyntax.java:337-338`

```java
public static <T> QualifiedField<T> field(String tableAlias, FieldMeta<T> field) {
    return ContextStack.peek().field(tableAlias, field);
}
```

**返回类型**: `QualifiedField<T>`

**使用场景**: 
- 自连接查询中区分多个同名表
- 多表查询中明确指定字段所属的表
- 需要带表别名前缀的字段引用

---

### 2. SQLs.refField(String derivedAlias, String selectionAlias)

**声明位置**: `SQLSyntax.java:347-348`

```java
public static DerivedField refField(String derivedAlias, String selectionAlias) {
    return ContextStack.peek().refField(derivedAlias, selectionAlias);
}
```

**返回类型**: `DerivedField`

**使用场景**: 
- 引用子查询或派生表中的字段
- CTE（公共表表达式）字段引用

---

### 3. SQLs.refTypedField(String derivedAlias, String selectionAlias)

**声明位置**: `SQLSyntax.java:358-366`

```java
public static TypedDerivedField refTypedField(String derivedAlias, String selectionAlias) {
    final DerivedField field;
    field = ContextStack.peek().refField(derivedAlias, selectionAlias);
    if (!(field instanceof TypedDerivedField)) {
        String m = String.format("derived field(%s.%s) isn't %s", derivedAlias, selectionAlias,
                TypedDerivedField.class.getName());
        throw ContextStack.clearStackAndCriteriaError(m);
    }
    return (TypedDerivedField) field;
}
```

**返回类型**: `TypedDerivedField`

**使用场景**: 
- 需要类型安全地引用派生表字段
- 需要对派生表字段进行类型化操作

---

### 4. SQLs.refSelection(String selectionAlias)

**声明位置**: `SQLSyntax.java:380-381`

```java
public static Expression refSelection(String selectionAlias) {
    return ContextStack.peek().refSelection(selectionAlias);
}
```

**返回类型**: `Expression`

**使用场景**: 
- ORDER BY 子句引用 SELECT 中的别名
- 其他需要引用选择项的场景

---

### 5. SQLs.refSelection(int selectionOrdinal)

**声明位置**: `SQLSyntax.java:394-395`

```java
public static Expression refSelection(int selectionOrdinal) {
    return ContextStack.peek().refSelection(selectionOrdinal);
}
```

**返回类型**: `Expression`

**参数说明**: `selectionOrdinal` - 基于 1 的选择项序号

**使用场景**: 
- 通过位置引用 SELECT 中的选择项

---

## 二、相关接口定义

### 1. QualifiedField<T> 接口

```java
public interface QualifiedField<T> extends TypedTableField<T> {
    String tableAlias();
}
```

**说明**: 代表带表别名限定的字段，输出格式为 `tableAlias.column`

**继承关系**: `QualifiedField<T>` → `TypedTableField<T>` → `TypedField` → ...

---

### 2. DerivedField 接口

```java
public interface DerivedField extends SqlField {
    String tableAlias();
}
```

**说明**: 代表派生表（子查询、CTE）中的字段

---

### 3. TypedDerivedField 接口

**说明**: 类型化的派生表字段，提供类型安全的操作

---

## 三、完整示例代码

### 示例 1: 自连接查询使用 SQLs.field()

```java
final Select stmt = SQLs.query()
    .select("u1", PERIOD, PillUser_.id, "u2", PERIOD, PillUser_.nickName)
    .from(PillUser_.T, AS, "u1")
    .join(PillUser_.T, AS, "u2")
    .on(SQLs.field("u1", PillUser_.parentId)::equal, SQLs.field("u2", PillUser_.id))
    .asQuery();
```

**说明**: 在自连接中使用 `SQLs.field()` 区分两个 PillUser 表的字段

---

### 示例 2: 子查询中引用外层表字段

```java
final Select stmt = SQLs.query()
    .select(PillUser_.nickName)
    .from(PillUser_.T, AS, "u")
    .where(PillUser_.nickName.equal(SQLs::param, "蛮吉"))
    .and(SQLs::exists, SQLs.subQuery()
        .select(ChinaProvince_.id)
        .from(ChinaProvince_.T, AS, "p")
        .join(ChinaRegion_.T, AS, "r")
        .on(ChinaProvince_.id::equal, ChinaRegion_.id)
        .where(ChinaProvince_.governor::equal, SQLs.field("u", PillUser_.nickName))
        .asQuery()
    )
    .asQuery();
```

**说明**: 子查询的 WHERE 子句中引用外层查询的 "u" 表字段

---

### 示例 3: 引用派生表字段 (refField)

```java
final Select stmt = SQLs.query()
    .select(s -> s.space(SQLs.refField("us", "one"))
        .comma("us", PERIOD, ASTERISK)
    )
    .from(SQLs.subQuery()
        .select(SQLs.literalValue(1)::as, "one")
        .comma("u", PERIOD, PillUser_.T)
        .from(PillUser_.T, AS, "u")
        .asQuery()
    )
    .as("us")
    .where(SQLs.refField("us", "one").equal(SQLs.parameter(1)))
    .asQuery();
```

---

### 示例 4: UPDATE 语句中使用 field()

```java
final ReturningUpdate stmt = Postgres.singleUpdate()
    .update(ChinaRegion_.T, AS, "c")
    .set(ChinaRegion_.name, SQLs::param, "New Name")
    .from(HistoryChinaRegion_.T, AS, "hc")
    .where(HistoryChinaRegion_.id::equal, ChinaRegion_.id)
    .returning("c", PERIOD, ChinaRegion_.T)
    .comma(HistoryChinaRegion_.id, SQLs.field("hc", HistoryChinaRegion_.regionGdp).as("myGdp"))
    .asReturningUpdate();
```

---

### 示例 5: CTE 中使用 field() 和 refField()

```java
final Update stmt = Postgres.singleUpdate()
    .with("child_cte").as(sw -> sw.update(ChinaProvince_.T, AS, "p")
        .set(ChinaProvince_.governor, SQLs::param, "Governor")
        .from(ChinaRegion_.T, AS, "c")
        .where(ChinaProvince_.id.in(SQLs::rowParam, idList))
        .and(ChinaProvince_.id::equal, ChinaRegion_.id)
        .returning(SQLs.field("p", ChinaProvince_.id).as("myId"))
        .asReturningUpdate()
    )
    .space()
    .update(ChinaRegion_.T, AS, "c")
    .set(ChinaRegion_.updateTime, UPDATE_TIME_PLACEHOLDER)
    .from("child_cte")
    .where(ChinaRegion_.id::equal, SQLs.refField("child_cte", ChinaRegion_.ID))
    .asUpdate();
```

---

### 示例 6: 使用 refSelection 在 ORDER BY 中

```java
final Select stmt = SQLs.query()
    .select(PillUser_.nickName.as("userName"), PillUser_.createTime.as("created"))
    .from(PillUser_.T, AS, "u")
    .orderBy(SQLs.refSelection("created").desc())
    .asQuery();
```

---

### 示例 7: 使用 refSelection(int) 通过序号引用

```java
final Select stmt = SQLs.query()
    .select(PillUser_.nickName, PillUser_.createTime)
    .from(PillUser_.T, AS, "u")
    .orderBy(SQLs.refSelection(2).desc())  // 引用第 2 个选择项 (createTime)
    .asQuery();
```

---

## 四、实际项目代码示例

### army-example 中的 StandardQueryUnitTests

**位置**: `army-example/src/test/java/io/army/criteria/standard/unit/StandardQueryUnitTests.java:218-240`

```java
@Test
public void simpleSubQuery() {
    final Map<String, Object> map = _Collections.hashMap();
    map.put("nickName", "蛮吉");

    final Select stmt;
    stmt = SQLs.query()
        .select(PillUser_.nickName)
        .from(PillUser_.T, AS, "u")
        .where(PillUser_.nickName.equal(SQLs::param, map.get("nickName")))
        .and(SQLs::exists, SQLs.subQuery()
            .select(ChinaProvince_.id)
            .from(ChinaProvince_.T, AS, "p")
            .join(ChinaRegion_.T, AS, "r")
            .on(ChinaProvince_.id::equal, ChinaRegion_.id)
            .where(ChinaProvince_.governor::equal, SQLs.field("u", PillUser_.nickName))
            .asQuery()
        )
        .asQuery();

    printStmt(LOG, stmt);
}
```

---

### Postgres UpdateUnitTests 示例

**位置**: `army-example/src/test/java/io/army/criteria/postgre/statement/UpdateUnitTests.java:68-92`

```java
@Test
public void returningUpdateParent() {
    final ReturningUpdate stmt;
    stmt = Postgres.singleUpdate()
        .update(ChinaRegion_.T, AS, "c")
        .set(ChinaRegion_.name, SQLs::param, this.randomCity())
        .set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::literal, new BigDecimal("100.00"))
        .setRow(ChinaRegion_.regionGdp, ChinaRegion_.population, Postgres.subQuery()
            .select(HistoryChinaRegion_.regionGdp, HistoryChinaRegion_.population)
            .from(HistoryChinaRegion_.T, AS, "h")
            .where(HistoryChinaRegion_.id.equal(SQLs::literal, 1))
            .asQuery()
        )
        .from(HistoryChinaRegion_.T, AS, "hc")
        .where(HistoryChinaRegion_.id::equal, ChinaRegion_.id)
        .returning("c", PERIOD, ChinaRegion_.T)
        .comma(HistoryChinaRegion_.id, SQLs.field("hc", HistoryChinaRegion_.regionGdp).as("myGdp"))
        .asReturningUpdate();

    printStmt(LOG, stmt);
}
```

---

## 五、使用场景总结

| 方法 | 适用场景 | 示例 |
|------|---------|------|
| `SQLs.field(tableAlias, field)` | 自连接、多表查询，区分同名字段 | `SQLs.field("u1", User_.id)` |
| `SQLs.refField(derivedAlias, selectionAlias)` | 引用子查询/CTE 字段 | `SQLs.refField("cte", "user_id")` |
| `SQLs.refTypedField(derivedAlias, selectionAlias)` | 类型化引用派生表字段 | `SQLs.refTypedField("cte", "amount")` |
| `SQLs.refSelection(selectionAlias)` | ORDER BY 等引用 SELECT 别名 | `SQLs.refSelection("total_amount")` |
| `SQLs.refSelection(selectionOrdinal)` | 通过位置引用选择项 | `SQLs.refSelection(2)` |

---

## 六、注意事项

1. **表别名必须与 FROM/JOIN 子句中定义的一致**
2. **field() 方法必须在语句构建上下文中调用**
3. **自连接时必须使用 field() 来区分不同的表**
4. **refSelection(String) 会覆盖同名的选择项**
5. **refSelection(int) 是基于 1 的序号**
6. **refTypedField() 会检查类型，若字段不是 TypedDerivedField 会抛异常**
7. **单表查询通常不需要使用 field()，直接使用 FieldMeta 即可**
