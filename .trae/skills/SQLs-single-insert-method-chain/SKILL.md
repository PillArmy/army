
---
name: SQLs.singleInsert() 方法链
description: 完整的 Army Criteria API SQLs.singleInsert() 方法链知识。用于理解、解释或记录从 SQLs.singleInsert() 到 asInsert() 的完整 INSERT 语句构建流程，包括标准单表插入、父子表插入、子查询插入等多种方式。
---

# SQLs.singleInsert() 方法链 — 完整参考

&gt; **适用范围**: 本 Skill **仅限**用于理解、解释、记录 `SQLs.singleInsert()` 入口的标准 INSERT 语句构建流程。覆盖从 `SQLs.singleInsert()` 到 `asInsert()` 的完整方法链，包括选项设置、表选择、列指定、默认值设置、值插入、子查询插入等全部阶段。**不**涵盖 UPDATE/DELETE/SELECT 语句链。

&gt; **源码依据**: 本 Skill 基于以下核心源文件编写——`InsertStatement.java`（接口定义）、`StandardInsert.java`（标准 INSERT 接口组合）、`StandardInserts.java`（实现类）、`StandardInsertUnitTests.java`（测试用例）。所有描述均以实际源码接口签名和实现为准。

## 核心入口

```java
// SQLs.java line 401-403
public static StandardInsert._PrimaryOptionSpec&lt;Insert&gt; singleInsert() {
    return StandardInserts.singleInsert();
}
```

**返回类型**: `StandardInsert._PrimaryOptionSpec&lt;Insert&gt; — 实现类是 `PrimaryInsertInto20Clause`。

**内部实现**: `StandardInserts.singleInsert()`:

```java
// StandardInserts.java line 51-53
static StandardInsert._PrimaryOptionSpec&lt;Insert&gt; singleInsert() {
    return new PrimaryInsertInto20Clause&lt;&gt;(null, SQLs::identity);
}
```

---

## 完整方法链 Diagram

```
SQLs.singleInsert()  →  StandardInsert._PrimaryOptionSpec&lt;Insert&gt;
│
├─① 选项设置阶段 (可选)
│  ├─ .migration()                                        → StandardInsert._PrimaryNullOptionSpec&lt;Insert&gt;
│  ├─ .nullMode(NullMode mode)                            → StandardInsert._PrimaryPreferLiteralSpec&lt;Insert&gt;
│  ├─ .literalMode(LiteralMode mode)                        → StandardInsert._PrimaryInsertIntoClause&lt;Insert&gt;
│  ├─ .ignoreReturnIds()                                  → StandardInsert._PrimaryPreferLiteralSpec&lt;Insert&gt;
│  ├─ [WITH 子句] (可选)
│  │  ├─ .with(String name)                               → StandardQuery._StaticCteParensSpec
│  │  ├─ .withRecursive(String name)                     → StandardQuery._StaticCteParensSpec
│  │  ├─ .with(Consumer&lt;StandardCtes&gt;)                → StandardInsert._PrimaryInsertIntoClause&lt;Insert&gt;
│  │  ├─ .withRecursive(Consumer&lt;StandardCtes&gt;)          → StandardInsert._PrimaryInsertIntoClause&lt;Insert&gt;
│  │  └─ [继续后续 CTE 追加和结束 WITH] (参考 SELECT/INSERT]
│
├─② 选择插入表 (必须)
│  ├─ .insertInto(SimpleTableMeta&lt;T&gt; table)              → StandardInsert._ColumnListSpec&lt;T, Insert&gt;
│  └─ .insertInto(ParentTableMeta&lt;P&gt; table)                → StandardInsert._ColumnListSpec&lt;P, InsertStatement._ParentInsert20&lt;Insert, StandardInsert._ChildWithSpec&lt;Insert, P&gt;&gt;&gt;
│
├─③ 指定插入列 (可选)
│  ├─ [静态列列表]
│  │  └─ .parens(Consumer&lt;StandardInsert._StaticColumnSpaceClause&lt;T&gt;)  → StandardInsert._ComplexColumnDefaultSpec&lt;T, Insert&gt;
│  │     └─ [内部: .space(...) / .comma(...)
│  └─ [动态列列表]
│     └─ .parens(SQLs.SymbolSpace, Consumer&lt;Consumer&lt;FieldMeta&lt;T&gt;&gt;&gt;)  → StandardInsert._ComplexColumnDefaultSpec&lt;T, Insert&gt;
│
├─④ 设置默认值 (可选，可多次)
│  ├─ [静态默认值]
│  │  ├─ .defaultValue(FieldMeta&lt;T&gt; field, @Nullable Object value)  → 当前接口
│  │  ├─ .defaultValue(FieldMeta&lt;T&gt; field, BiFunction&lt;FieldMeta&lt;T&gt;, E, Expression&gt; operator, @Nullable E value)  → 当前接口
│  │  ├─ .ifDefault(FieldMeta&lt;T&gt; field, @Nullable Object value)  → 当前接口
│  │  └─ .ifDefault(FieldMeta&lt;T&gt; field, BiFunction&lt;FieldMeta&lt;T&gt;, E, Expression&gt; operator, @Nullable E value)  → 当前接口
│  └─ [动态默认值]
│     ├─ .defaults(Consumer&lt;InsertStatement._ColumnDefaultClause&lt;T&gt;&gt;)  → 当前接口
│     └─ .ifDefaults(Consumer&lt;InsertStatement._ColumnDefaultClause&lt;T&gt;&gt;)  → 当前接口
│
├─⑤ 插入值阶段 (三选一)
│  ├─ [A. Domain 插入模式]
│  │  ├─ .value(T domain)                                  → Statement._DmlInsertClause&lt;Insert&gt;
│  │  └─ .values(List&lt;? extends T&gt; domainList)          → Statement._DmlInsertClause&lt;Insert&gt;
│  │
│  ├─ [B. 静态值插入模式]
│  │  ├─ .values()                                      → StandardInsert._StandardValuesParensClause&lt;T, Insert&gt;
│  │  │  ├─ [第一行值: .parens(Consumer&lt;StandardInsert._StaticValueSpaceClause&lt;T&gt;&gt;)  → StandardInsert._ValuesParensCommaSpec&lt;T, Insert&gt;
│  │  │  │  └─ [内部: .space(...) / .comma(...) / .ifComma(...) / .spaceIf(...)]
│  │  │  ├─ [追加行: .comma()]                             → StandardInsert._StandardValuesParensClause&lt;T, Insert&gt;
│  │  │  └─ [结束: .asInsert()]                          → Insert
│  │  └─ [动态值插入模式]
│  │     └─ .values(Consumer&lt;ValuesConstructor&lt;T&gt;&gt;)          → Statement._DmlInsertClause&lt;Insert&gt;
│  │
│  └─ [C. 子查询插入模式]
│     ├─ .space()                                      → StandardQuery.SelectSpec&lt;Statement._DmlInsertClause&lt;Insert&gt;&gt;
│     ├─ .space(Function&lt;StandardQuery.SelectSpec&lt;...&gt;)      → Statement._DmlInsertClause&lt;Insert&gt;
│     └─ .space(SubQuery subQuery)                          → Statement._DmlInsertClause&lt;Insert&gt;
│
└─⑥ 结束语句
   └─ .asInsert()                                        → Insert
```

### 父子表插入 (Parent-Child Insert)
```
[父表结束后]
│
└─ .child()                                              → StandardInsert._ChildWithSpec&lt;Insert, P&gt;
   │
   ├─ [WITH 子句 (可选)]
   │  ├─ .with(String name)                             → StandardQuery._StaticCteParensSpec
   │  ├─ .withRecursive(String name)                   → StandardQuery._StaticCteParensSpec
   │  └─ [同主表 WITH 流程]
   │
   └─ .insertInto(ComplexTableMeta&lt;P, T&gt; table)       → StandardInsert._ColumnListSpec&lt;T, Insert&gt;
      └─ [同单表插入流程: 列指定、默认值、值插入、子查询插入]
```

---

## 逐层接口详解

### 0. 入口: `StandardInsert._PrimaryOptionSpec&lt;Insert&gt;

```java
// StandardInsert.java line 106-113
interface _PrimaryOptionSpec&lt;I extends Item&gt;
        extends InsertStatement._MigrationOptionClause&lt;_PrimaryNullOptionSpec&lt;I&gt;&gt;,
                InsertStatement._IgnoreReturnIdsOptionClause&lt;_PrimaryNullOptionSpec&lt;I&gt;&gt;,
                _PrimaryNullOptionSpec&lt;I&gt; {
}
```

**功能**: 提供 INSERT 语句的选项设置，包括：
- `migration()` - 迁移模式选项
- `ignoreReturnIds()` - 忽略返回 ID 选项（仅适用于 Domain 插入模式）
- `nullMode(NullMode)` - NULL 值处理选项
- `literalMode(LiteralMode)` - 字面值模式选项
- 直接跳过选项直接进入 WITH 或 INSERT INTO

---

### ① 选项设置阶段

#### 1.1 迁移模式 (migration)

```java
// InsertStatement.java line 51-54
interface _MigrationOptionClause&lt;R&gt; {
    R migration();
}
```

**使用场景**: 用于数据库迁移场景，控制插入已有表数据到新表。

---

#### 1.2 忽略返回 ID (ignoreReturnIds)

```java
// InsertStatement.java line 84-103
interface _IgnoreReturnIdsOptionClause&lt;R&gt; {
    R ignoreReturnIds();
}
```

**使用场景**: 当满足以下条件时必须设置此选项：
- 存在冲突子句（如 MySQL ON DUPLICATE KEY, PostgreSQL ON CONFLICT）
- 非迁移模式
- 主键的 `GeneratorType 为 POST
- 插入单表
- Domain 插入模式
- 插入多行
- 不支持 RETURNING 子句或冲突子句可忽略

---

#### 1.3 NULL 值模式 (nullMode)

```java
// InsertStatement.java line 73-81
interface _NullOptionClause&lt;R&gt; {
    R nullMode(NullMode mode);
}
```

**参数说明**: 设置 NULL 值处理模式。

---

#### 1.4 字面值模式 (literalMode)

```java
// InsertStatement.java line 56-71
interface _PreferLiteralClause&lt;R&gt; {
    R literalMode(LiteralMode mode);
}
```

**使用场景**: 当插入大量数据时，使用字面量模式可以减少占位符数量，避免数据库服务器返回 `prepared statement contains too many placeholders 错误。

---

#### 1.5 WITH 子句 (Common Table Expression)

```java
// StandardInsert.java line 88-93
interface _WithSpec&lt;I extends Item&gt;
        extends _StandardDynamicWithClause&lt;_PrimaryInsertIntoClause&lt;I&gt;&gt;,
                _StandardStaticWithClause&lt;_PrimaryInsertIntoClause&lt;I&gt;&gt;,
                _PrimaryInsertIntoClause&lt;I&gt; {
}
```

**静态 CTE**:
- `.with(String name)` / `.withRecursive(String name)` - 开始定义 CTE
- `.with(Consumer&lt;StandardCtes&gt;) - 动态定义 CTE
- `.withRecursive(Consumer&lt;StandardCtes&gt;) - 动态定义递归 CTE

**注意**: WITH 子句定义后可以继续 `.comma(String name)` 追加更多 CTE，最后用 `.space()` 结束 WITH 进入 INSERT INTO。

---

### ② 选择插入表

#### 2.1 单表插入

```java
// StandardInsert.java line 81-86
interface _PrimaryInsertIntoClause&lt;I extends Item&gt; extends Item {
    &lt;T&gt; _ColumnListSpec&lt;T, I&gt; insertInto(SimpleTableMeta&lt;T&gt; table);
    &lt;P&gt; _ColumnListSpec&lt;P, _ParentInsert20&lt;I, _ChildWithSpec&lt;I, P&gt;&gt;&gt; insertInto(ParentTableMeta&lt;P&gt; table);
}
```

**返回接口说明：
- `SimpleTableMeta&lt;T&gt; - 普通单表
- `ParentTableMeta&lt;P&gt; - 父子表中的父表

---

### ③ 指定插入列

#### 3.1 静态列列表

```java
// InsertStatement.java line 150-163
interface _ColumnListParensClause&lt;T, R&gt; {
    R parens(Consumer&lt;_StaticColumnSpaceClause&lt;T&gt;&gt; consumer);
    R parens(SQLs.SymbolSpace space, Consumer&lt;Consumer&lt;FieldMeta&lt;T&gt;&gt;&gt; consumer);
}
```

**使用示例**:
```java
.parens(s -&gt; s.space(ChinaRegion_.name, ChinaRegion_.regionGdp)
             .comma(ChinaRegion_.parentId))
```

**内部接口**:
```java
// InsertStatement.java line 106-148
interface _StaticColumnSpaceClause&lt;T&gt; {
    _StaticColumnUnaryClause&lt;T&gt; space(FieldMeta&lt;T&gt; field);
    _StaticColumnDualClause&lt;T&gt; space(FieldMeta&lt;T&gt; field1, FieldMeta&lt;T&gt; field2);
    _StaticColumnCommaQuadraClause&lt;T&gt; space(FieldMeta&lt;T&gt; field1, FieldMeta&lt;T&gt; field2, FieldMeta&lt;T&gt; field3, FieldMeta&lt;T&gt; field4);
}
```

---

### ④ 设置默认值

#### 4.1 静态默认值

```java
// InsertStatement.java line 167-221
interface _FullColumnDefaultClause&lt;T, R&gt;
        extends _StaticColumnDefaultClause&lt;T, R&gt;, _DynamicColumnDefaultClause&lt;T, R&gt; {
}

interface _StaticColumnDefaultClause&lt;T, R&gt; {
    R defaultValue(FieldMeta&lt;T&gt; field, @Nullable Object value);
    &lt;E&gt; R defaultValue(FieldMeta&lt;T&gt; field, BiFunction&lt;FieldMeta&lt;T&gt;, E, Expression&gt; operator, @Nullable E value);
    R ifDefault(FieldMeta&lt;T&gt; field, @Nullable Object value);
    &lt;E&gt; R ifDefault(FieldMeta&lt;T&gt; field, BiFunction&lt;FieldMeta&lt;T&gt;, E, Expression&gt; operator, @Nullable E value);
}
```

**defaultValue vs ifDefault**:
- `defaultValue` - 始终设置默认值
- `ifDefault` - 条件设置默认值

**使用示例**:
```java
.defaultValue(ChinaRegion_.regionGdp, SQLs::literal, "88888.88")
.defaultValue(ChinaRegion_.visible, SQLs::literal, true)
.defaultValue(ChinaRegion_.parentId, SQLs::literal, 0)
```

---

#### 4.2 动态默认值

```java
// InsertStatement.java line 210-215
interface _DynamicColumnDefaultClause&lt;T, R&gt; {
    R defaults(Consumer&lt;_ColumnDefaultClause&lt;T&gt;&gt; consumer);
    R ifDefaults(Consumer&lt;_ColumnDefaultClause&lt;T&gt;&gt; consumer);
}
```

---

### ⑤ 插入值阶段

#### 5.1 Domain 插入模式

```java
// InsertStatement.java line 223-237
interface _DomainValuesClause&lt;T, R&gt; extends _DomainValueClause&lt;T, R&gt; {
    &lt;TS extends T&gt; R values(List&lt;TS&gt; domainList);
}

interface _DomainValueClause&lt;T, R&gt; {
    &lt;TS extends T&gt; R value(TS domain);
}
```

**使用场景**: 插入实体对象列表，Army 会自动处理列映射、主键生成等。

**使用示例**:
```java
.values(this.createRegionList())
```

---

#### 5.2 静态值插入模式

```java
// StandardInsert.java line 31-53
interface _StandardValuesParensClause&lt;T, I extends Item&gt;
        extends InsertStatement._ValuesParensClause&lt;T, _ValuesParensCommaSpec&lt;T, I&gt;&gt; {
}

interface _ValuesParensCommaSpec&lt;T, I extends Item&gt;
        extends _CommaClause&lt;_StandardValuesParensClause&lt;T, I&gt;&gt;, _DmlInsertClause&lt;I&gt; {
}
```

**使用示例**:
```java
.values()
    .parens(s -&gt; s.space(ChinaRegion_.name, SQLs::param, r.getName())
                  .comma(ChinaRegion_.regionGdp, SQLs::literal, r.getRegionGdp())
                  .comma(ChinaRegion_.parentId, SQLs::literal, 0))
    .comma()
    .parens(s -&gt; s.space(ChinaRegion_.name, SQLs::param, "光明顶")
                  .comma(ChinaRegion_.parentId, SQLs::literal, 0))
```

**静态值内部接口**:
```java
// InsertStatement.java line 255-293
interface _StaticValueSpaceClause&lt;T&gt; {
    _StaticColumnValueClause&lt;T&gt; space(FieldMeta&lt;T&gt; field, @Nullable Object operand);
    &lt;E&gt; _StaticColumnValueClause&lt;T&gt; space(FieldMeta&lt;T&gt; field, BiFunction&lt;FieldMeta&lt;T&gt;, E, Expression&gt; funcRef, @Nullable E value);
    _StaticColumnValueClause&lt;T&gt; spaceIf(FieldMeta&lt;T&gt; field, @Nullable Object operand);
    &lt;E&gt; _StaticColumnValueClause&lt;T&gt; spaceIf(FieldMeta&lt;T&gt; field, BiFunction&lt;FieldMeta&lt;T&gt;, E, Expression&gt; funcRef, @Nullable E value);
}

interface _StaticColumnValueClause&lt;T&gt; {
    _StaticColumnValueClause&lt;T&gt; comma(FieldMeta&lt;T&gt; field, @Nullable Object operand);
    &lt;E&gt; _StaticColumnValueClause&lt;T&gt; comma(FieldMeta&lt;T&gt; field, BiFunction&lt;FieldMeta&lt;T&gt;, E, Expression&gt; funcRef, @Nullable E value);
    _StaticColumnValueClause&lt;T&gt; ifComma(FieldMeta&lt;T&gt; field, @Nullable Object operand);
    &lt;E&gt; _StaticColumnValueClause&lt;T&gt; ifComma(FieldMeta&lt;T&gt; field, BiFunction&lt;FieldMeta&lt;T&gt;, E, Expression&gt; funcRef, @Nullable E value);
}
```

---

#### 5.3 动态值插入模式

```java
// InsertStatement.java line 294-298
interface _DynamicValuesClause&lt;T, R&gt; {
    R values(Consumer&lt;ValuesConstructor&lt;T&gt;&gt; consumer);
}
```

---

#### 5.4 子查询插入模式

```java
// StandardInsert.java line 50-53
interface _ComplexColumnDefaultSpec&lt;T, I extends Item&gt; extends _ValuesColumnDefaultSpec&lt;T, I&gt;,
        InsertStatement._QueryInsertSpaceClause&lt;StandardQuery.SelectSpec&lt;_DmlInsertClause&lt;I&gt;&gt;, _DmlInsertClause&lt;I&gt;&gt; {
}

// InsertStatement.java line 167-176
interface _QueryInsertSpaceClause&lt;T extends Item, R extends Item&gt; {
    T space();
    R space(Function&lt;T, R&gt; function);
    R space(SubQuery subQuery);
}
```

**使用场景**: 从查询结果插入数据。

**使用示例**:
```java
.parens(s -&gt; s.space(ChinaRegion_.id, ChinaRegion_.createTime, ChinaRegion_.updateTime, ChinaRegion_.version)
             .comma(ChinaRegion_.visible, ChinaRegion_.name, ChinaRegion_.regionGdp, ChinaRegion_.regionType))
    .space()
    .select(HistoryChinaRegion_.id, HistoryChinaRegion_.createTime, HistoryChinaRegion_.updateTime, HistoryChinaRegion_.version)
    .comma(HistoryChinaRegion_.visible, HistoryChinaRegion_.name, HistoryChinaRegion_.regionGdp)
    .comma(SQLs.literalValue(RegionType.NONE)::as, HistoryChinaRegion_.REGION_TYPE)
    .from(HistoryChinaRegion_.T, AS, "r")
    .asQuery()
```

---

### ⑥ 结束语句

```java
// Statement.java
interface _DmlInsertClause&lt;I extends Item&gt; extends Item {
    I asInsert();
}
```

**最终类型**: `Insert` - 可执行的 INSERT 语句

---

## 父子表插入详解

### 父表插入完成后

```java
// StandardInsert.java line 250-252
interface _ParentInsert20&lt;I extends Item, T extends Item&gt; extends _ParentInsert&lt;T&gt;, _StaticSpaceClause&lt;I&gt; {
}

interface _ParentInsert&lt;CT extends Item&gt; extends Insert, _ChildPartClause&lt;CT&gt; {
}

interface _ChildPartClause&lt;CR&gt; {
    CR child();
}
```

**功能**: 调用 `.child()` 进入子表插入阶段。

---

### 子表插入阶段

```java
// StandardInsert.java line 61-72
interface _ChildWithSpec&lt;I extends Item, P&gt; extends _StandardDynamicWithClause&lt;_ChildInsertIntoClause&lt;I, P&gt;&gt;,
        _StandardStaticWithClause&lt;_ChildInsertIntoClause&lt;I, P&gt;&gt;,
        _ChildInsertIntoClause&lt;I, P&gt; {
}

interface _ChildInsertIntoClause&lt;I extends Item, P&gt; extends Item {
    &lt;T&gt; _ColumnListSpec&lt;T, I&gt; insertInto(ComplexTableMeta&lt;P, T&gt; table);
}
```

**注意**: 子表插入流程与单表插入相同。

---

## 使用示例

### Domain 插入示例

```java
// StandardInsertUnitTests.java line 40-58
final Insert stmt = SQLs.singleInsert()
    .literalMode(LiteralMode.PREFERENCE)
    .insertInto(ChinaRegion_.T)
    .parens(s -&gt; s.space(ChinaRegion_.name, ChinaRegion_.regionGdp)
                 .comma(ChinaRegion_.parentId))
    .defaultValue(ChinaRegion_.regionGdp, SQLs::literal, "88888.88")
    .defaultValue(ChinaRegion_.visible, SQLs::literal, true)
    .defaultValue(ChinaRegion_.parentId, SQLs::literal, 0)
    .values(this.createRegionList())
    .asInsert();
```

### 父子表 Domain 插入示例

```java
// StandardInsertUnitTests.java line 62-80
final Insert stmt = SQLs.singleInsert()
    .literalMode(LiteralMode.PREFERENCE)
    .insertInto(ChinaRegion_.T)
    .values(provinceList)
    .asInsert()
    .child()
    .insertInto(ChinaProvince_.T)
    .values(provinceList)
    .asInsert();
```

### 静态值插入示例

```java
// StandardInsertUnitTests.java line 84-110
final Insert stmt = SQLs.singleInsert()
    .literalMode(LiteralMode.PREFERENCE)
    .insertInto(ChinaRegion_.T)
    .defaultValue(ChinaRegion_.regionGdp, SQLs::literal, "88888.88")
    .defaultValue(ChinaRegion_.visible, SQLs::literal, true)
    .values()
    .parens(s -&gt; s.space(ChinaRegion_.name, SQLs::param, r.getName())
                  .comma(ChinaRegion_.regionGdp, SQLs::literal, r.getRegionGdp())
                  .comma(ChinaRegion_.parentId, SQLs::literal, 0))
    .comma()
    .parens(s -&gt; s.space(ChinaRegion_.name, SQLs::param, "光明顶")
                  .comma(ChinaRegion_.parentId, SQLs::literal, 0))
    .asInsert();
```

### 子查询插入示例

```java
// StandardInsertUnitTests.java line 148-165
final Insert stmt = SQLs.singleInsert()
    .migration()
    .insertInto(ChinaRegion_.T)
    .parens(s -&gt; s.space(ChinaRegion_.id, ChinaRegion_.createTime, ChinaRegion_.updateTime, ChinaRegion_.version)
                 .comma(ChinaRegion_.visible, ChinaRegion_.name, ChinaRegion_.regionGdp, ChinaRegion_.regionType))
    .space()
    .select(HistoryChinaRegion_.id, HistoryChinaRegion_.createTime, HistoryChinaRegion_.updateTime, HistoryChinaRegion_.version)
    .comma(HistoryChinaRegion_.visible, HistoryChinaRegion_.name, HistoryChinaRegion_.regionGdp)
    .comma(SQLs.literalValue(RegionType.NONE)::as, HistoryChinaRegion_.REGION_TYPE)
    .from(HistoryChinaRegion_.T, AS, "r")
    .asQuery()
    .asInsert();
```

---

## 实现类继承链

```
SQLs.singleInsert()
├─ PrimaryInsertInto20Clause (标准 INSERT 主入口)
├─ StandardComplexValuesClause (列列表、默认值、值选择)
├─ PrimarySingleDomainInsertStatement (Domain 插入实现)
├─ PrimarySingleValueInsertStatement (值插入实现)
├─ PrimarySingleQueryInsertStatement (子查询插入实现)
└─ 父子表相关类:
   ├─ PrimaryParentDomainInsert20Statement (父表 Domain 插入)
   ├─ PrimaryParentValueInsert20Statement (父表值插入)
   ├─ PrimaryParentQueryInsert20Statement (父表子查询插入)
   ├─ ChildInsertIntoClause (子表入口)
   ├─ PrimaryChildDomainInsertStatement (子表 Domain 插入)
   ├─ PrimaryChildValueInsertStatement (子表值插入)
   └─ PrimaryChildQueryInsertStatement (子表子查询插入)
```

---

## 关键约束和规则

### 选项约束

1. **三选一约束
   - 值插入三选一：Domain 插入、静态值插入、子查询插入，三者只能选择其一
   - 选项设置顺序：migration → nullMode → literalMode → INSERT INTO → [WITH]

2. **父子表约束
   - 父表必须使用 `ParentTableMeta`
   - 子表必须使用 `ComplexTableMeta&lt;P, T&gt;`
   - 父表插入完成后调用 `.child()` 进入子表插入

3. **ignoreReturnIds 约束
   - 仅适用于 Domain 插入模式
   - 仅适用于单表插入
   - 仅适用于非迁移模式
   - 仅适用于主键 POST 生成策略

### 可重复方法

- `.defaultValue()` / `.ifDefault()` - 可多次设置默认值
- `.parens().comma() - 可多次追加值行
- WITH 子句 `.comma()` - 可多次追加 CTE

### 不可重复方法

- `.insertInto()` - 每个表仅一次
- `.migration()` - 仅一次
- `.nullMode()` - 仅一次
- `.literalMode()` - 仅一次
- `.ignoreReturnIds()` - 仅一次
- `.values() (值插入模式进入) - 仅一次
- `.asInsert()` - 仅一次，结束语句

---

## 方法链总结

### 完整流程

```
SQLs.singleInsert()
  → [.migration()]
  → [.nullMode(NullMode)]
  → [.ignoreReturnIds()]
  → [.literalMode(LiteralMode)]
  → [WITH 子句]
  → .insertInto(表)
  → [.parens(列)]
  → [.defaultValue() 多次]
  → (
      .values(实体/列表)
      | .values() .parens(值) [.comma() .parens(值)]*
      | .space() 子查询
    )
  → .asInsert()
  → [.child() 子表插入]
```

