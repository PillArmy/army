---
name: "mysql-loaddata-stmt"
description: "提供 MySQLs.loadDataStmt() 方法链的完整文档、示例和使用指南。Invoke when user needs help with MySQL LOAD DATA statement DSL or method chain documentation."
---

# MySQLs.loadDataStmt() 方法链完整文档

## 概述
`MySQLs.loadDataStmt()` 用于创建 MySQL 的 LOAD DATA 语句，支持从文件批量导入数据到表中，提供了完整的 LOAD DATA 语法支持，包括 LOCAL、REPLACE/IGNORE、字符集、字段/行格式、分区、SET 子句等功能。

## 完整方法链 Diagram

```
MySQLs.loadDataStmt()
├── .loadData(MySQLs.Modifier local)
│   └── .loadData(List<MySQLs.Modifier> modifierList)
│
├── .infile(Path filePath)
│   └── .infile(Supplier<Path> supplier)
│
├── .replace() / .ignore()
│   ├── .ifReplace(BooleanSupplier predicate)
│   └── .ifIgnore(BooleanSupplier predicate)
│
├── .intoTable(SimpleTableMeta<T> table)
│   └── .intoTable(ParentTableMeta<T> table)
│
├── [分区子句 - 可选]
│   ├── .partition(String first, String... rest)
│   ├── .partition(Consumer<Consumer<String>> consumer)
│   └── .ifPartition(Consumer<Consumer<String>> consumer)
│
├── [字符集子句 - 可选]
│   ├── .characterSet(String charsetName)
│   └── .ifCharacterSet(Supplier<String> supplier)
│
├── [字段子句 - 可选]
│   ├── .fields(Consumer<_ColumnTerminatedBySpec> consumer)
│   │   ├── .terminatedBy(String string)
│   │   ├── .terminatedBy(Supplier<String> supplier)
│   │   ├── .ifTerminatedBy(Supplier<String> supplier)
│   │   ├── .enclosedBy(char ch)
│   │   ├── .enclosedBy(String ch)
│   │   ├── .ifEnclosedBy(Supplier<String> supplier)
│   │   ├── .optionallyEnclosedBy(char ch)
│   │   ├── .optionallyEnclosedBy(String ch)
│   │   ├── .ifOptionallyEnclosedBy(Supplier<String> supplier)
│   │   ├── .escapedBy(char ch)
│   │   ├── .escapedBy(String ch)
│   │   └── .ifEscapedBy(Supplier<String> supplier)
│   │
│   ├── .columns(Consumer<_ColumnTerminatedBySpec> consumer)
│   ├── .ifFields(Consumer<_ColumnTerminatedBySpec> consumer)
│   └── .ifColumns(Consumer<_ColumnTerminatedBySpec> consumer)
│
├── [行子句 - 可选]
│   ├── .lines(Consumer<_StartingBySpec> consumer)
│   │   ├── .startingBy(String string)
│   │   ├── .ifStartingBy(Supplier<String> supplier)
│   │   ├── .terminatedBy(String string)
│   │   ├── .terminatedBy(Supplier<String> supplier)
│   │   └── .ifTerminatedBy(Supplier<String> supplier)
│   │
│   └── .ifLines(Consumer<_StartingBySpec> consumer)
│
├── [忽略行子句 - 可选]
│   ├── .ignore(long rowNumber, SQLs.LinesWord word)
│   ├── .ignore(Supplier<Long> supplier, SQLs.LinesWord word)
│   └── .ifIgnore(Supplier<Long> supplier, SQLs.LinesWord word)
│
├── [列/变量列表子句 - 可选]
│   ├── .parens(Consumer<_VariadicExprSpaceClause> consumer)
│   ├── .ifParens(Consumer<_VariadicExprSpaceClause> consumer)
│   ├── .parens(SQLs.SymbolSpace space, Consumer<Consumer<Expression>> consumer)
│   └── .ifParens(SQLs.SymbolSpace space, Consumer<Consumer<Expression>> consumer)
│
├── [SET 子句 - 可选]
│   ├── .set(FieldMeta<T> field, Object value)
│   ├── .set(FieldMeta<T> field, BiFunction<FieldMeta<T>, Object, Expression> func, Object value)
│   └── [可多次调用 .set() 来设置多个字段]
│
├── .asCommand()
│
└── [父子表支持 - 可选]
    └── .child()
        └── [重复上述完整的 LOAD DATA 方法链用于子表]
```

## 方法可重复性说明

### 可重复的方法
1. **`.set()`** - 可以多次调用来设置多个字段的值
2. **`.child()`** - 在父子表场景中，每个子表可以有一个独立的 LOAD DATA 链

### 不可重复的方法
1. **`.loadData()`** - 每个语句只调用一次
2. **`.infile()`** - 每个语句只指定一个文件
3. **`.replace()`/.ignore()** - 只能选择一个策略
4. **`.intoTable()`** - 每个语句只能导入到一个表
5. **`.partition()`** - 分区列表只能指定一次
6. **`.characterSet()`** - 字符集只能指定一次
7. **`.fields()`/.columns()** - 字段格式只能配置一次
8. **`.lines()`** - 行格式只能配置一次
9. **`.ignore()`** - 忽略行数只能指定一次
10. **`.parens()`** - 列/变量列表只能指定一次
11. **`.asCommand()`** - 构建最终语句，每个链调用一次

## 各子句的多种形式和使用场景

### 1. loadData 子句 - 两种形式
#### 形式 A：单个 Modifier
```java
.loadData(MySQLs.LOCAL)
```
**场景**：使用 LOCAL 关键字，从客户端文件导入

#### 形式 B：Modifier 列表
```java
.loadData(modifierList)
```
**场景**：需要多个修饰符时使用

### 2. infile 子句 - 两种形式
#### 形式 A：直接传入 Path
```java
.infile(Paths.get("data.csv"))
```
**场景**：文件路径已知时

#### 形式 B：Supplier 延迟提供
```java
.infile(() -> resolveDataFile())
```
**场景**：文件路径需要动态计算时

### 3. 策略选项 - REPLACE/IGNORE - 四种形式
#### 形式 A：REPLACE
```java
.replace()
```
**场景**：遇到重复键时替换现有行

#### 形式 B：IGNORE
```java
.ignore()
```
**场景**：遇到重复键时忽略新行

#### 形式 C：条件 REPLACE
```java
.ifReplace(() -> shouldReplace)
```
**场景**：根据条件决定是否使用 REPLACE

#### 形式 D：条件 IGNORE
```java
.ifIgnore(() -> shouldIgnore)
```
**场景**：根据条件决定是否使用 IGNORE

### 4. 分区子句 - 三种形式
#### 形式 A：直接指定分区
```java
.partition("p1", "p2", "p3")
```
**场景**：分区名称已知

#### 形式 B：Consumer 动态构建
```java
.partition(c -> {
    c.accept("p1");
    c.accept("p2");
})
```
**场景**：分区需要动态确定

#### 形式 C：条件分区
```java
.ifPartition(c -> {
    if (usePartition) {
        c.accept("p1");
    }
})
```
**场景**：根据条件决定是否指定分区

### 5. 字符集子句 - 两种形式
#### 形式 A：直接指定字符集
```java
.characterSet("utf8mb4")
```
**场景**：字符集已知

#### 形式 B：条件字符集
```java
.ifCharacterSet(() -> "utf8mb4")
```
**场景**：字符集需要动态确定

### 6. 字段子句 - 四种形式
#### 形式 A：FIELDS
```java
.fields(c -> c.terminatedBy(","))
```
**场景**：使用 FIELDS 关键字

#### 形式 B：COLUMNS
```java
.columns(c -> c.terminatedBy(","))
```
**场景**：使用 COLUMNS 关键字（与 FIELDS 同义）

#### 形式 C：条件 FIELDS
```java
.ifFields(c -> c.terminatedBy(","))
```
**场景**：根据条件决定是否配置字段格式

#### 形式 D：条件 COLUMNS
```java
.ifColumns(c -> c.terminatedBy(","))
```
**场景**：根据条件决定是否配置列格式

### 7. 字段终止符 - 三种形式
#### 形式 A：直接指定
```java
.terminatedBy(",")
```

#### 形式 B：Supplier 延迟
```java
.terminatedBy(() -> ",")
```

#### 形式 C：条件指定
```java
.ifTerminatedBy(() -> ",")
```

### 8. 字段包围符 - 六种形式
#### 形式 A：字符参数
```java
.enclosedBy('"')
```

#### 形式 B：字符串参数
```java
.enclosedBy("\"")
```

#### 形式 C：可选包围
```java
.optionallyEnclosedBy('"')
```

#### 形式 D：可选包围（字符串）
```java
.optionallyEnclosedBy("\"")
```

#### 形式 E：条件包围
```java
.ifEnclosedBy(() -> "\"")
```

#### 形式 F：条件可选包围
```java
.ifOptionallyEnclosedBy(() -> "\"")
```

### 9. 转义字符 - 三种形式
#### 形式 A：字符参数
```java
.escapedBy('\\')
```

#### 形式 B：字符串参数
```java
.escapedBy("\\")
```

#### 形式 C：条件转义
```java
.ifEscapedBy(() -> "\\")
```

### 10. 行子句 - 两种形式
#### 形式 A：LINES
```java
.lines(c -> c.terminatedBy("\n"))
```

#### 形式 B：条件 LINES
```java
.ifLines(c -> c.terminatedBy("\n"))
```

### 11. 行起始符 - 两种形式
#### 形式 A：直接指定
```java
.startingBy(">>>")
```

#### 形式 B：条件指定
```java
.ifStartingBy(() -> ">>>")
```

### 12. 忽略行 - 三种形式
#### 形式 A：直接指定行数
```java
.ignore(1, SQLs.LINES)
```

#### 形式 B：Supplier 延迟
```java
.ignore(() -> 1L, SQLs.LINES)
```

#### 形式 C：条件忽略
```java
.ifIgnore(() -> 1L, SQLs.LINES)
```

### 13. 列/变量列表 - 四种形式
#### 形式 A：静态列
```java
.parens(c -> c.space(Table_.col1, Table_.col2))
```

#### 形式 B：条件静态列
```java
.ifParens(c -> c.space(Table_.col1))
```

#### 形式 C：动态列
```java
.parens(SQLs.SPACE, c -> {
    c.accept(Table_.col1);
    c.accept(Table_.col2);
})
```

#### 形式 D：条件动态列
```java
.ifParens(SQLs.SPACE, c -> c.accept(Table_.col1))
```

### 14. SET 子句 - 两种形式
#### 形式 A：直接设置值
```java
.set(Table_.status, SQLs::literal, "ACTIVE")
```

#### 形式 B：使用 BiFunction
```java
.set(Table_.status, SQLs::param, statusValue)
```

## 完整使用示例

### 示例 1：简单的 LOAD DATA
```java
final DmlCommand stmt = MySQLs.loadDataStmt()
        .loadData(MySQLs.LOCAL)
        .infile(Paths.get("china_region.csv"))
        .ignore()
        .intoTable(ChinaRegion_.T)
        .characterSet("utf8mb4")
        .columns(s -> s.terminatedBy(","))
        .lines(s -> s.terminatedBy("\n"))
        .ignore(1, SQLs.LINES)
        .asCommand();
```

### 示例 2：带 SET 子句的 LOAD DATA
```java
final DmlCommand stmt = MySQLs.loadDataStmt()
        .loadData(MySQLs.LOCAL)
        .infile(Paths.get("china_region.csv"))
        .ignore()
        .intoTable(ChinaRegion_.T)
        .characterSet("utf8mb4")
        .columns(s -> s.terminatedBy(","))
        .lines(s -> s.terminatedBy("\n"))
        .ignore(1, SQLs.LINES)
        .set(ChinaRegion_.visible, SQLs::literal, true)
        .set(ChinaRegion_.regionType, SQLs::literal, RegionType.NONE)
        .asCommand();
```

### 示例 3：带列列表的 LOAD DATA
```java
final DmlCommand stmt = MySQLs.loadDataStmt()
        .loadData(MySQLs.LOCAL)
        .infile(Paths.get("china_region.csv"))
        .ignore()
        .intoTable(ChinaRegion_.T)
        .characterSet("utf8mb4")
        .columns(s -> s.terminatedBy(","))
        .lines(s -> s.terminatedBy("\n"))
        .ignore(1, SQLs.LINES)
        .parens(s -> s.space(ChinaRegion_.name, ChinaRegion_.regionGdp)
                .comma(ChinaRegion_.parentId))
        .set(ChinaRegion_.visible, SQLs::literal, true)
        .set(ChinaRegion_.regionType, SQLs::literal, RegionType.NONE)
        .asCommand();
```

### 示例 4：完整的字段/行格式配置
```java
final DmlCommand stmt = MySQLs.loadDataStmt()
        .loadData(MySQLs.LOCAL)
        .infile(Paths.get("data.csv"))
        .replace()
        .intoTable(SomeTable_.T)
        .characterSet("utf8mb4")
        .fields(s -> s.terminatedBy(",")
                .optionallyEnclosedBy('"')
                .escapedBy('\\'))
        .lines(s -> s.startingBy(">>>")
                .terminatedBy("\n"))
        .ignore(2, SQLs.LINES)
        .parens(s -> s.space(SomeTable_.col1, SomeTable_.col2)
                .comma(SomeTable_.col3))
        .set(SomeTable_.createTime, SQLs::literal, LocalDateTime.now())
        .asCommand();
```

### 示例 5：父子表 LOAD DATA
```java
final DmlCommand stmt = MySQLs.loadDataStmt()
        .loadData(MySQLs.LOCAL)
        .infile(Paths.get("china_region_parent.csv"))
        .ignore()
        .intoTable(ChinaRegion_.T)
        .characterSet("utf8mb4")
        .columns(s -> s.terminatedBy(","))
        .lines(s -> s.terminatedBy("\n"))
        .ignore(1, SQLs.LINES)
        .parens(s -> s.space(ChinaRegion_.name))
        .set(ChinaRegion_.regionType, SQLs::literal, RegionType.PROVINCE)
        .asCommand()
        .child()
        .loadData(MySQLs.LOCAL)
        .infile(Paths.get("china_province.csv"))
        .ignore()
        .intoTable(ChinaProvince_.T)
        .characterSet("utf8mb4")
        .columns(s -> s.terminatedBy(","))
        .lines(s -> s.terminatedBy("\n"))
        .ignore(1, SQLs.LINES)
        .asCommand();
```

### 示例 6：带条件子句的 LOAD DATA
```java
final DmlCommand stmt = MySQLs.loadDataStmt()
        .loadData(MySQLs.LOCAL)
        .infile(Paths.get("data.csv"))
        .ifReplace(() -> useReplace)
        .intoTable(SomeTable_.T)
        .ifPartition(c -> {
            if (usePartition) {
                c.accept("p1");
                c.accept("p2");
            }
        })
        .ifCharacterSet(() -> useUtf8mb4 ? "utf8mb4" : null)
        .ifFields(c -> {
            if (needFieldsFormat) {
                c.terminatedBy(",");
            }
        })
        .ifLines(c -> {
            if (needLinesFormat) {
                c.terminatedBy("\n");
            }
        })
        .ifIgnore(() -> needSkipHeader ? 1L : null, SQLs.LINES)
        .ifParens(c -> {
            if (needColumnList) {
                c.space(SomeTable_.col1, SomeTable_.col2);
            }
        })
        .set(SomeTable_.status, SQLs::literal, "ACTIVE")
        .asCommand();
```

## 重要注意事项

### 限制条件
1. 服务器端 `local_infile` 系统变量必须开启
2. 客户端 `allowLoadLocalInfile` 属性（JDBC/JDBC）必须为 true
3. 必须使用客户端预编译语句或静态语句
4. 字段/行格式中的字面量使用 `EscapeMode.BACK_SLASH` 解析，忽略 `ArmyKey.LITERAL_ESCAPE_MODE`
5. 必须确保 SQL 模式 `NO_BACKSLASH_ESCAPES` 未启用

### 字符处理
所有字符串参数（terminatedBy、enclosedBy、escapedBy、startingBy）都使用反斜杠转义模式解析。

### 执行示例
```java
final long rows = session.update(stmt, SyncStmtOption.preferServerPrepare(false));
```
注意使用 `preferServerPrepare(false)` 来确保使用客户端预编译语句。

## 参考文件
- MySQLs.java: /Users/zoro/repositories/trae/java/hub/army/army-mysql/src/main/java/io/army/criteria/impl/MySQLs.java
- MySQLLoads.java: /Users/zoro/repositories/trae/java/hub/army/army-mysql/src/main/java/io/army/criteria/impl/MySQLLoads.java
- MySQLLoadData.java: /Users/zoro/repositories/trae/java/hub/army/army-mysql/src/main/java/io/army/criteria/mysql/MySQLLoadData.java
- _MySQLLoadData.java: /Users/zoro/repositories/trae/java/hub/army/army-mysql/src/main/java/io/army/criteria/impl/inner/mysql/_MySQLLoadData.java
