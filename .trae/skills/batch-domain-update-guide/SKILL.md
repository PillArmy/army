---
name: "batch-domain-update-guide"
description: "SQLs.batchDomainUpdate() 方法链完整文档，包含 Diagram、参数说明、调用形式、使用场景。Invoke when user needs to use batchDomainUpdate() API."
---

# SQLs.batchDomainUpdate() 方法链完整文档

## 方法链 Diagram

```
SQLs.batchDomainUpdate()
    │
    ├─ update(tableMeta, tableAlias)
    │       │
    │       ├─ Static SET 子句
    │       │   ├─ set(field, value)
    │       │   ├─ set(field, valueOperator, value)
    │       │   ├─ set(field, fieldOperator, valueOperator, value)
    │       │   ├─ setSpace(field, valueOperator)
    │       │   ├─ setSpace(field, fieldOperator, valueOperator)
    │       │   ├─ setRow(field1, field2, subQuery)
    │       │   ├─ setRow(field1, field2, field3, subQuery)
    │       │   ├─ setRow(field1, field2, field3, field4, subQuery)
    │       │   ├─ setRow(fieldList, subQuery)
    │       │   └─ setRow(consumer, subQuery)
    │       │
    │       ├─ Conditional SET 子句 (可重复)
    │       │   ├─ ifSet(field, value)
    │       │   ├─ ifSet(field, valueOperator, value)
    │       │   ├─ ifSet(field, fieldOperator, valueOperator, value)
    │       │   ├─ ifSetRow(field1, field2, supplier)
    │       │   ├─ ifSetRow(field1, field2, field3, supplier)
    │       │   ├─ ifSetRow(field1, field2, field3, field4, supplier)
    │       │   ├─ ifSetRow(fieldList, supplier)
    │       │   └─ ifSetRow(consumer, supplier)
    │       │
    │       └─ Dynamic SET 子句
    │           └─ sets(consumer)
    │
    └─ WHERE 子句
        │
        ├─ where(predicate)
        ├─ where(consumer)
        ├─ where(expOperator, operand)
        ├─ whereIf(supplier)
        ├─ whereIf(expOperator, value)
        ├─ whereIf(expOperator, operator, value)
        ├─ whereIf(expOperator, operator, value)
        ├─ whereIf(expOperator, operator, func, value)
        ├─ whereIf(expOperator, operator, value1, AND, value2)
        └─ whereIf(expOperator, firstFunc, first, AND, secondFunc, second)
            │
            └─ AND 子句 (可重复)
               ├─ and(predicate)
               ├─ and(expOperator, operand)
               ├─ ifAnd(supplier)
               ├─ ifAnd(expOperator, value)
               ├─ ifAnd(expOperator, operator, value)
               ├─ ifAnd(expOperator, operator, value)
               ├─ ifAnd(expOperator, operator, func, value)
               ├─ ifAnd(expOperator, operator, value1, AND, value2)
               ├─ ifAnd(expOperator, firstFunc, first, AND, secondFunc, second)
               ├─ ifAnd(expOperator1, operand1, expOperator2, numberOperand)
               └─ ifAnd(expOperator1, operator, operand1, expOperator2, numberOperand)
                  │
                  └─ asUpdate()
                     │
                     └─ namedParamList(paramList)
```

---

## 详细方法说明

### 一、开始方法

#### `SQLs.batchDomainUpdate()`

**描述**: 开始构建批处理领域更新语句

**返回**: `StandardUpdate._DomainUpdateClause<Statement._BatchUpdateParamSpec>`

**使用场景**: 当需要执行批量更新领域对象（支持父子表关系）时使用

---

### 二、UPDATE 子句 (不可重复)

#### `update(TableMeta<?> table, String tableAlias)`

**参数**:
- `table`: 表元数据
- `tableAlias`: 表别名

**返回**: `_StandardSetClause`

**使用场景**: 指定要更新的表

---

#### `<T> update(SingleTableMeta<T> table, SQLs.WordAs as, String tableAlias)`

**参数**:
- `table`: 单表元数据
- `as`: `SQLs.AS` 关键字
- `tableAlias`: 表别名

**返回**: `_StandardSetClause`

---

#### `<T> update(ChildTableMeta<T> table, SQLs.WordAs as, String tableAlias)`

**参数**:
- `table`: 子表元数据
- `as`: `SQLs.AS` 关键字
- `tableAlias`: 表别名

**返回**: `_StandardSetClause`

---

### 三、SET 子句 (静态赋值)

#### `set(F field, Object value)`

**参数**:
- `field`: 字段对象
- `value`: 值

**返回**: `SR` (继续 SET 或进入 WHERE)

**使用场景**: 直接设置字段值

**示例**:
```java
.set(ChinaProvince_.name, "New Name")
```

---

#### `<E> set(F field, BiFunction<F, E, AssignmentItem> valueOperator, E value)`

**参数**:
- `field`: 字段对象
- `valueOperator`: 值操作函数（如 `SQLs::param`, `SQLs::literal`）
- `value`: 值

**返回**: `SR`

**使用场景**: 使用参数或字面量设置字段值

**示例**:
```java
.set(ChinaProvince_.name, SQLs::param, "New Name")
.set(ChinaProvince_.name, SQLs::literal, "Fixed Name")
```

---

#### `<E> set(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator, BiFunction<F, E, Expression> valueOperator, E value)`

**参数**:
- `field`: 字段对象
- `fieldOperator`: 字段操作函数（如 `SQLs::plusEqual`）
- `valueOperator`: 值操作函数
- `value`: 值

**返回**: `SR`

**使用场景**: 执行复合赋值操作（如 `field = field + value`）

**示例**:
```java
.set(ChinaProvince_.regionGdp, SQLs::plusEqual, SQLs::param, 1000)
```

---

### 四、SET SPACE 子句 (批量参数占位符)

#### `setSpace(F field, BiFunction<F, String, Expression> valueOperator)`

**参数**:
- `field`: 字段对象
- `valueOperator`: 值操作函数（通常为 `SQLs::namedParam`）

**返回**: `SR`

**使用场景**: 设置命名参数占位符，用于批量更新

**示例**:
```java
.setSpace(ChinaProvince_.name, SQLs::namedParam)
```

---

#### `setSpace(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator, BiFunction<F, String, Expression> valueOperator)`

**参数**:
- `field`: 字段对象
- `fieldOperator`: 字段操作函数
- `valueOperator`: 值操作函数

**返回**: `SR`

**使用场景**: 复合赋值的批量参数占位符

**示例**:
```java
.setSpace(ChinaProvince_.regionGdp, SQLs::plusEqual, SQLs::namedParam)
```

---

### 五、SET ROW 子句 (子查询赋值)

#### `setRow(F field1, F field2, SubQuery subQuery)`
#### `setRow(F field1, F field2, F field3, SubQuery subQuery)`
#### `setRow(F field1, F field2, F field3, F field4, SubQuery subQuery)`

**参数**:
- `fieldN`: 字段对象
- `subQuery`: 子查询

**返回**: `SR`

**使用场景**: 使用子查询的结果设置多个字段

---

#### `setRow(List<F> fieldList, SubQuery subQuery)`

**参数**:
- `fieldList`: 字段列表
- `subQuery`: 子查询

**返回**: `SR`

---

#### `setRow(Consumer<Consumer<F>> consumer, SubQuery subQuery)`

**参数**:
- `consumer`: 字段消费者
- `subQuery`: 子查询

**返回**: `SR`

---

### 六、条件 SET 子句 (ifSet, 可重复)

#### `ifSet(F field, Object value)`

**参数**: 同 `set()`

**返回**: `SR` (返回自身，可链式调用)

**特点**: 仅当 `value != null` 时才添加 SET 项

**使用场景**: 动态更新，只更新非空字段

---

#### `<E> ifSet(F field, BiFunction<F, E, AssignmentItem> valueOperator, E value)`
#### `<E> ifSet(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator, BiFunction<F, E, Expression> valueOperator, E value)`

**参数**: 同对应的 `set()` 方法

**特点**: 仅当 `value != null` 时才添加 SET 项

---

### 七、条件 SET ROW 子句 (ifSetRow, 可重复)

#### `ifSetRow(F field1, F field2, Supplier<SubQuery> supplier)`
#### `ifSetRow(F field1, F field2, F field3, Supplier<SubQuery> supplier)`
#### `ifSetRow(F field1, F field2, F field3, F field4, Supplier<SubQuery> supplier)`

**参数**:
- `fieldN`: 字段
- `supplier`: 子查询提供者

**特点**: 仅当 `supplier.get() != null` 时才添加

---

#### `ifSetRow(List<F> fieldList, Supplier<SubQuery> supplier)`
#### `ifSetRow(Consumer<Consumer<F>> consumer, Supplier<SubQuery> supplier)`

---

### 八、动态 SET 子句 (sets)

#### `sets(Consumer<_BatchItemPairs<F>> consumer)`

**参数**:
- `consumer`: 消费者，用于动态添加多个 SET 项

**返回**: `_StandardWhereClause`

**使用场景**: 复杂场景下动态构建多个 SET 项

**示例**:
```java
.sets(pairs -> {
    pairs.set(ChinaProvince_.name, SQLs::param, "A");
    pairs.set(ChinaProvince_.governor, SQLs::param, "B");
})
```

---

### 九、WHERE 子句

#### `where(IPredicate predicate)`

**参数**:
- `predicate`: 谓词条件

**返回**: `WA` (继续 AND 或结束)

**使用场景**: 简单条件过滤

**示例**:
```java
.where(ChinaProvince_.id.equal(SQLs::param, 1))
```

---

#### `where(Consumer<Consumer<IPredicate>> consumer)`

**参数**:
- `consumer`: 消费者，可以添加多个谓词（自动用 AND 连接）

**返回**: `WR`

**使用场景**: 添加多个 AND 连接的条件

---

#### `<T> where(Function<T, IPredicate> expOperator, T operand)`

**参数**:
- `expOperator`: 表达式操作函数
- `operand`: 操作数

**返回**: `WA`

---

### 十、条件 WHERE 子句 (whereIf)

#### `whereIf(Supplier<IPredicate> supplier)`

**参数**:
- `supplier`: 谓词提供者

**特点**: 仅当 `supplier.get() != null` 时才添加

---

#### `<T> whereIf(Function<T, IPredicate> expOperator, T value)`
#### `<T> whereIf(ExpressionOperator<TypedExpression, T, IPredicate> expOperator, BiFunction<TypedExpression, T, Expression> operator, T value)`
#### `<T> whereIf(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator, SQLs.BiOperator operator, T value)`
#### `<T> whereIf(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator, SQLs.BiOperator operator, BiFunction<TypedExpression, T, Expression> func, T value)`
#### `<T> whereIf(BetweenValueOperator<T> expOperator, BiFunction<TypedExpression, T, Expression> operator, T value1, SQLs.WordAnd and, T value2)`
#### `<T, U> whereIf(BetweenDualOperator<T, U> expOperator, BiFunction<TypedExpression, T, Expression> firstFunc, T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFunc, U second)`

**特点**: 仅当值不为 null 时才添加条件

---

### 十一、AND 子句 (可重复)

#### `and(IPredicate predicate)`

**参数**:
- `predicate`: 谓词条件

**返回**: `WA` (可继续链式调用)

**使用场景**: 添加额外的 AND 条件

---

#### `<T> and(Function<T, IPredicate> expOperator, T operand)`

---

### 十二、条件 AND 子句 (ifAnd, 可重复)

#### `ifAnd(Supplier<IPredicate> supplier)`
#### `<T> ifAnd(Function<T, IPredicate> expOperator, T value)`
#### `<T> ifAnd(ExpressionOperator<TypedExpression, T, IPredicate> expOperator, BiFunction<TypedExpression, T, Expression> operator, T value)`
#### `<T> ifAnd(BiFunction<SQLs.BiOperator, T, IPredicate> expOperator, SQLs.BiOperator operator, T value)`
#### `<T> ifAnd(TeFunction<SQLs.BiOperator, BiFunction<TypedExpression, T, Expression>, T, IPredicate> expOperator, SQLs.BiOperator operator, BiFunction<TypedExpression, T, Expression> func, T value)`
#### `<T> ifAnd(BetweenValueOperator<T> expOperator, BiFunction<TypedExpression, T, Expression> operator, T value1, SQLs.WordAnd and, T value2)`
#### `<T, U> ifAnd(BetweenDualOperator<T, U> expOperator, BiFunction<TypedExpression, T, Expression> firstFunc, T first, SQLs.WordAnd and, BiFunction<TypedExpression, U, Expression> secondFunc, U second)`

**特殊的 Update ifAnd 方法**:
#### `<T> ifAnd(Function<T, Expression> expOperator1, T operand1, BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand)`
#### `<T> ifAnd(ExpressionOperator<TypedExpression, T, Expression> expOperator1, BiFunction<TypedExpression, T, Expression> operator, T operand1, BiFunction<Expression, Object, IPredicate> expOperator2, Object numberOperand)`

---

### 十三、结束语句

#### `asUpdate()`

**返回**: `Statement._BatchUpdateParamSpec`

**使用场景**: 完成 UPDATE 语句构建，准备设置批量参数

---

#### `namedParamList(List<?> paramList)`

**参数**:
- `paramList`: 批量参数列表

**返回**: `BatchUpdate`

**使用场景**: 设置批量更新的参数列表

---

## 完整使用示例

### 示例 1: 简单批量更新

```java
final BatchUpdate stmt;
stmt = SQLs.batchDomainUpdate()
    .update(ChinaProvince_.T, SQLs.AS, "p")
    .setSpace(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::namedParam)
    .setSpace(ChinaProvince_.governor, SQLs::namedParam)
    .where(ChinaProvince_.id::spaceEqual, SQLs::namedParam)
    .and(ChinaRegion_.version.equal(SQLs::param, 0))
    .asUpdate()
    .namedParamList(provinceList);
```

### 示例 2: 使用 set 和 ifSet 的动态更新

```java
final BatchUpdate stmt;
stmt = SQLs.batchDomainUpdate()
    .update(ChinaProvince_.T, SQLs.AS, "p")
    .set(ChinaRegion_.name, SQLs::param, "武侠江湖")
    .set(ChinaRegion_.regionGdp, SQLs::plusEqual, SQLs::param, 1000)
    .ifSet(ChinaProvince_.provincialCapital, SQLs::param, capital) // 条件更新
    .ifSet(ChinaProvince_.governor, SQLs::param, governor)       // 条件更新
    .where(ChinaProvince_.id.equal(SQLs::namedParam, ChinaRegion_.ID))
    .and(ChinaRegion_.name.equal(SQLs::namedParam, ChinaRegion_.NAME))
    .asUpdate()
    .namedParamList(createProvinceList());
```

### 示例 3: 使用动态 sets 方法

```java
final BatchUpdate stmt;
stmt = SQLs.batchDomainUpdate()
    .update(PillPerson_.T, SQLs.AS, "up")
    .sets(pairs -> {
        pairs.set(PillUser_.identityType, SQLs::literal, IdentityType.PERSON);
        pairs.set(PillUser_.identityId, SQLs::literal, 888);
        pairs.set(PillUser_.nickName, SQLs::param, "令狐冲");
    })
    .where(PillPerson_.id.equal(SQLs::literal, "1"))
    .and(PillUser_.nickName.equal(SQLs::param, "zoro"))
    .asUpdate()
    .namedParamList(personList);
```

---

## 方法可重复性总结

| 方法类型 | 可重复 | 说明 |
|---------|--------|------|
| `update()` | ❌ | 只能调用一次 |
| `set() / setSpace() / setRow()` | ✅ | 可以多次调用，添加多个 SET 项 |
| `ifSet() / ifSetRow()` | ✅ | 可以多次条件性调用 |
| `sets()` | ❌ | 只能调用一次（或与静态 set 互斥） |
| `where()` | ❌ | 只能调用一次 |
| `whereIf()` | ❌ | 只能调用一次 |
| `and()` | ✅ | 可以多次添加 AND 条件 |
| `ifAnd()` | ✅ | 可以多次条件性添加 AND |
| `asUpdate()` | ❌ | 只能调用一次，结束 UPDATE 构建 |
| `namedParamList()` | ❌ | 只能调用一次，设置批量参数 |

---

## 领域更新特点

1. **支持父子表**: 可以同时更新父表和子表的字段
2. **自动处理关系**: 框架自动处理表之间的关系
3. **批量安全**: 支持批量操作，提高性能
4. **乐观锁**: 支持版本字段的乐观锁机制
