/*
 * Copyright 2023-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.army.criteria.impl;

import io.army.annotation.*;
import io.army.generator.FieldGenerator;
import io.army.lang.Nullable;
import io.army.mapping.MappingType;
import io.army.mapping.NameEnumType;
import io.army.mapping.UUIDType;
import io.army.mapping.optional.CompositeField;
import io.army.meta.*;
import io.army.modelgen._MetaBridge;
import io.army.util.*;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;


/// Core utility class for resolving and constructing **table-level** and **field-level** metadata
/// from Army ORM annotations and external `TableMeta.properties` configuration files.
///
/// This class serves as the foundational metadata resolution engine within the Army framework.
/// It bridges the gap between compile-time annotations (e.g., `@Table`, `@Column`, `@Index`)
/// and runtime metadata objects (e.g., `TableMeta`, `FieldMeta`, `IndexMeta`).
///
/// ## Placeholder Expressions
///
/// Army supports a set of **placeholder expressions** in annotation attributes, enabling
/// flexible, environment-aware metadata resolution:
///
/// | Expression         | Behavior                                                              |
/// |--------------------|-----------------------------------------------------------------------|
/// | `${DEFAULT}`       | Use properties file override if present; otherwise apply built-in default |
/// | `${RUNTIME}`       | Must be resolved from `TableMeta.properties` at runtime; throws if missing |
/// | `${OPTIONAL}`      | Optional override from properties; skips the element if unresolved    |
/// | `${DEFAULT_VALUE}` | Use a convention-based default (e.g., generated index names)         |
///
/// ### Example: Column name resolution
/// ```java
/// @Column(name = "${DEFAULT}")
/// public String userName;
/// // Resolved as "user_name" (camelCase → snake_case)
/// // Or overridden via: com.example.User.userName.Column.name=user_name
/// ```
///
/// ## Property Key Convention
///
/// External overrides in `TableMeta.properties` follow the naming pattern:
/// ```
/// {fully.qualified.ClassName}.{fieldName}.{Attribute}.{property}
/// ```
///
/// @see FieldMetaUtils
/// @see io.army.meta.TableMeta
/// @see io.army.annotation.Table
/// @see io.army.annotation.Column
public abstract class TableMetaUtils {

    TableMetaUtils() {
        throw new UnsupportedOperationException();
    }


    /// Placeholder expression that resolves from `TableMeta.properties` if present,
    /// otherwise falls back to a **built-in default** convention.
    ///
    /// For column names, the default is camelCase-to-snake_case conversion.
    /// For table names, it converts the simple class name to lowercase with underscores.
    public static final String DEFAULT_EXP = "${DEFAULT}";

    /// Placeholder expression that **must** be resolved from `TableMeta.properties` at runtime.
    ///
    /// Throws `MetaException` if no corresponding property entry is found.
    /// Use this when the value is environment-specific and has no sensible built-in default.
    public static final String RUNTIME_EXP = "${RUNTIME}";

    /// Placeholder expression for **optional** metadata overrides.
    ///
    /// If the property is not found, the annotated element (e.g., an index) is silently skipped
    /// rather than causing an error. Not all attributes support this expression.
    public static final String OPTIONAL_EXP = "${OPTIONAL}";

    /// Placeholder expression that resolves to a **convention-based default value**.
    ///
    /// Currently used primarily for index name generation, where the framework
    /// automatically constructs names like `uni_{table}_{column}` or `idx_{table}_{column}`.
    public static final String DEFAULT_VALUE_EXP = "${DEFAULT_VALUE}";


    /// Resolves the **database column name** for a given field based on the `@Column` annotation
    /// and optional `TableMeta.properties` overrides.
    ///
    /// Resolution order:
    /// 1. If `Column.name()` is `${DEFAULT}` or `${RUNTIME}`, look up the property key
    ///    `{domainClass}.{fieldName}.Column.name`
    /// 2. If `${DEFAULT}` and no property found → convert field name from camelCase to snake_case
    /// 3. If `${RUNTIME}` and no property found → throw `MetaException`
    /// 4. If `${OPTIONAL}` → unsupported, throws `MetaException`
    /// 5. If literal text → use as-is (or convert to snake_case if empty)
    ///
    /// ### Example
    /// ```java
    /// // Field "userName" → column "user_name"
    /// TableMetaUtils.columnName(User.class, column, field, context);
    /// ```
    ///
    /// @param domainClass the domain entity class that owns the field
    /// @param column      the `@Column` annotation instance on the field
    /// @param field       the Java reflection `Field` object
    /// @param context     the metadata resolution context providing properties and utilities
    /// @return the resolved database column name (snake_case by convention)
    /// @throws MetaException if the column name cannot be resolved or is invalid
    public static String columnName(Class<?> domainClass, Column column, Field field, MetaContext context) {
        final String value = column.name(), fieldName = field.getName();
        final String finalValue;
        switch (value) {
            case DEFAULT_EXP:
            case RUNTIME_EXP: {
                final String key, configValue;
                key = context.tempBuilderAndClear().append(domainClass.getName())
                        .append('.')
                        .append(fieldName)
                        .append('.')
                        .append("Column")
                        .append('.')
                        .append("name")
                        .toString();

                configValue = context.tableMetaProperties().getProperty(key);
                if (_StringUtils.hasText(configValue)) {
                    finalValue = configValue.trim();
                } else switch (value) {
                    case DEFAULT_EXP: {
                        finalValue = _MetaBridge.camelToLowerCase(fieldName, context.tempBuilderAndClear());
                    }
                    break;
                    case RUNTIME_EXP:
                        throw new MetaException(String.format("%s no config", key));
                    default:
                        throw new IllegalStateException("bug");
                }
            }
            break;
            case OPTIONAL_EXP: {
                String m = String.format("%s in %s.%s %s.%s is unsupported", value, domainClass.getName(), field.getName(),
                        "Column", "name");
                throw new MetaException(m);
            }
            default: {
                if (_StringUtils.hasText(value)) {
                    finalValue = value;
                } else {
                    finalValue = _MetaBridge.camelToLowerCase(fieldName, context.tempBuilderAndClear());
                }
            } // default

        } // switch
        context.validateColumnName(domainClass, finalValue);
        return finalValue;
    }


    /// Resolves the **column precision** (total digit count or length) for a field.
    ///
    /// Delegates to `columnIntValue` with built-in defaults based on mapping type:
    /// - `SqlDecimal` → 16
    /// - `SqlString` → 255
    /// - `SqlEnum` → max label length + 5
    /// - `UUID` → 36
    /// - `Vector` → 1024
    ///
    /// @param column  the `@Column` annotation instance
    /// @param field   the database field object (either `FieldMeta` or `CompositeField`)
    /// @param context the metadata resolution context
    /// @return the resolved precision value, or `-1` if not applicable
    public static int columnPrecision(Column column, DatabaseObject.FieldObject field, MetaContext context) {
        return columnIntValue(column.precision(), "precision", field, context);
    }

    /// Resolves the **column scale** (decimal digit count or fractional seconds) for a field.
    ///
    /// Built-in defaults by mapping type:
    /// - `SqlDecimal` → 3
    /// - `SqlLocalDateTime` / `SqlOffsetDateTime` / time types → 6 (microsecond precision)
    ///
    /// @param column  the `@Column` annotation instance
    /// @param field   the database field object
    /// @param context the metadata resolution context
    /// @return the resolved scale value, or `-1` if not applicable
    public static int columnScale(Column column, DatabaseObject.FieldObject field, MetaContext context) {
        return columnIntValue(column.scale(), "scale", field, context);
    }

    /// Resolves the **column collation** string (e.g., `"en_US.utf8"`) for a field.
    ///
    /// Collation can be overridden via `TableMeta.properties` using the key:
    /// `{className}.{fieldName}.Column.collation`.
    ///
    /// @param column  the `@Column` annotation instance
    /// @param field   the database field object
    /// @param context the metadata resolution context
    /// @return the resolved collation string, or empty string if not specified
    public static String columnCollation(Column column, DatabaseObject.FieldObject field, MetaContext context) {
        return columnStringValue(column.collation(), "collation", field, context);
    }


    /// Resolves the **default value** expression for a column.
    ///
    /// When `${DEFAULT}` is used and no property override is found, the framework generates
    /// a type-appropriate zero value (e.g., `0` for numbers, `''` for strings,
    /// `'1970-01-01'` for dates).
    ///
    /// @param column  the `@Column` annotation instance
    /// @param field   the database field object
    /// @param context the metadata resolution context
    /// @return the resolved default value expression as a SQL literal string
    static String columnDefault(Column column, DatabaseObject.FieldObject field, MetaContext context) {
        return columnStringValue(column.defaultValue(), "defaultValue", field, context);
    }


    /// Resolves the **schema metadata** (catalog and schema) from the `@Table` annotation.
    ///
    /// Both `catalog` and `schema` support `${DEFAULT}` and `${RUNTIME}` placeholders.
    /// Property keys follow the pattern: `{className}.Table.catalog` and `{className}.Table.schema`.
    ///
    /// @param table       the `@Table` annotation instance
    /// @param domainClass the domain entity class
    /// @param context     the metadata resolution context
    /// @return the resolved `SchemaMeta` containing catalog and schema information
    static SchemaMeta schemaMeta(Table table, Class<?> domainClass, MetaContext context) {
        final String[] methods = {"catalog", "schema"};
        final String[] values = {table.catalog(), table.schema()};

        String key, value, configValue;
        for (int i = 0; i < methods.length; i++) {
            value = values[i];
            switch (value) {
                case DEFAULT_EXP:
                case RUNTIME_EXP: {
                    key = context.tempBuilderAndClear()
                            .append(domainClass.getName())
                            .append('.')
                            .append("Table")
                            .append('.')
                            .append(methods[i])
                            .toString();
                    configValue = context.tableMetaProperties().getProperty(key);
                    if (_StringUtils.hasText(configValue)) {
                        values[i] = configValue.trim();
                    } else switch (value) {
                        case DEFAULT_EXP:
                            values[i] = "";
                            break;
                        case RUNTIME_EXP:
                            throw new MetaException(String.format("%s no config", key));
                        default:
                            throw new IllegalStateException("bug");
                    }
                }
                break;
                case OPTIONAL_EXP: {
                    String m = String.format("%s in %s %s.%s is unsupported", value, domainClass.getName(),
                            "Table", methods[i]);
                    throw new MetaException(m);
                }
                default:
                    // no-op

            } // switch

        } // loop
        return _SchemaMetaFactory.getSchema(values[0], values[1]);
    }

    /// Resolves the **database table name** from the `@Table` annotation.
    ///
    /// The table name is **required** and must not be in camelCase format.
    /// Supports `${DEFAULT}` (auto-convert class simple name to snake_case) and
    /// `${RUNTIME}` (must be provided in properties) placeholders.
    ///
    /// ### Example
    /// ```java
    /// @Table(name = "stock_quotes")
    /// public class StockQuotes { ... }
    /// // Or use @Table(name = "${DEFAULT}") → "stock_quotes" (auto-derived)
    /// ```
    ///
    /// @param table       the `@Table` annotation instance
    /// @param domainClass the domain entity class
    /// @param context     the metadata resolution context
    /// @return the resolved table name in snake_case
    /// @throws MetaException if the table name is empty, camelCase, or cannot be resolved
    static String tableName(Table table, Class<?> domainClass, MetaContext context) {
        final String tableName = table.name();
        if (!_StringUtils.hasText(tableName)) {
            String m = String.format("Domain[%s] table name required", domainClass.getName());
            throw new MetaException(m);
        }
        final String finalName;
        switch (tableName) {
            case DEFAULT_EXP:
            case RUNTIME_EXP: {
                final String key, configValue;
                key = context.tempBuilderAndClear()
                        .append(domainClass.getName())
                        .append('.')
                        .append("Table")
                        .append('.')
                        .append("name")
                        .toString();

                configValue = context.tableMetaProperties().getProperty(key);
                if (_StringUtils.hasText(configValue)) {
                    if (_StringUtils.isCamelCase(configValue)) {
                        throw tableNameIsCamel(domainClass);
                    }
                    finalName = configValue.trim();
                } else finalName = switch (tableName) {
                    case DEFAULT_EXP ->
                            _MetaBridge.camelToLowerCase(domainClass.getSimpleName(), context.tempBuilderAndClear());
                    case RUNTIME_EXP -> throw new MetaException(String.format("%s no config", key));
                    default -> throw new IllegalStateException("bug");
                };
            }
            break;
            case OPTIONAL_EXP: {
                String m = String.format("%s in %s %s.%s is unsupported", tableName, domainClass.getName(),
                        "Table", "name");
                throw new MetaException(m);
            }
            default: {
                finalName = tableName;
            }
        }
        if (_StringUtils.isCamelCase(finalName)) {
            throw tableNameIsCamel(domainClass);
        }
        return finalName;
    }


    /// Resolves the **table comment** from the `@Table` annotation.
    ///
    /// The comment is used for DDL generation and database documentation.
    /// Supports `${DEFAULT}` (auto-convert class simple name to spaced comment)
    /// and `${RUNTIME}` placeholders.
    ///
    /// @param table       the `@Table` annotation instance
    /// @param domainClass the domain entity class
    /// @param context     the metadata resolution context
    /// @return the resolved table comment string
    /// @throws MetaException if the comment is empty or cannot be resolved
    static String tableComment(Table table, Class<?> domainClass, MetaContext context) {
        final String comment = table.comment();

        final String finalComment;
        switch (comment) {
            case DEFAULT_EXP:
            case RUNTIME_EXP: {
                final String key, configValue;
                key = context.tempBuilderAndClear()
                        .append(domainClass.getName())
                        .append('.')
                        .append("Table")
                        .append('.')
                        .append("comment")
                        .toString();

                configValue = context.tableMetaProperties().getProperty(key);
                if (_StringUtils.hasText(configValue)) {
                    finalComment = configValue.trim();
                } else finalComment = switch (comment) {
                    case DEFAULT_EXP ->
                            _MetaBridge.camelToComment(domainClass.getSimpleName(), context.tempBuilderAndClear());
                    case RUNTIME_EXP -> throw new MetaException(String.format("%s no config", key));
                    default -> throw new IllegalStateException("bug");
                };
            }
            break;
            case OPTIONAL_EXP: {
                String m = String.format("%s in %s %s.%s is unsupported", comment, domainClass.getName(),
                        "Table", "comment");
                throw new MetaException(m);
            }
            default: {
                if (!_StringUtils.hasText(comment)) {
                    String m = String.format("%s %s.%s no text", domainClass.getName(), "Table", "comment");
                    throw new MetaException(m);
                }
                finalComment = comment;
            }
        }
        return finalComment;
    }

    /// Determines whether the table is **immutable** (read-only).
    ///
    /// An immutable table does not support `UPDATE` operations on its fields.
    /// Parent tables with `@Inheritance` cannot be marked as immutable.
    ///
    /// @param table       the `@Table` annotation instance
    /// @param domainClass the domain entity class
    /// @return `true` if the table is immutable
    /// @throws MetaException if an immutable table also has `@Inheritance`
    static boolean immutable(Table table, Class<?> domainClass) {
        final boolean immutable = table.immutable();
        if (immutable && domainClass.getAnnotation(Inheritance.class) != null) {
            String m = String.format("Parent Domain[%s] couldn't be immutable.", domainClass.getName());
            throw new MetaException(m);
        }
        return immutable;
    }

    /// Determines whether the framework should **generate DDL CREATE statements** for the table.
    ///
    /// Resolution is based on the `DdlMode` specified in `@Table.ddlMode()`:
    /// - `CREATE` → always generate DDL
    /// - `NONE` → never generate DDL
    /// - `DEFAULT` → check `TableMeta.properties` key `{className}.Table.ddlMode`
    ///
    /// @param table       the `@Table` annotation instance
    /// @param domainClass the domain entity class
    /// @param context     the metadata resolution context
    /// @return `true` if DDL CREATE should be generated for this table
    static boolean tableCreateDdl(Table table, Class<?> domainClass, MetaContext context) {
        final DdlMode mode = table.ddlMode();
        boolean createDdl;
        switch (mode) {
            case CREATE:
                createDdl = true;
                break;
            case NONE:
                createDdl = false;
                break;
            case DEFAULT: {
                final String key, configValue;
                key = context.tempBuilderAndClear()
                        .append(domainClass.getName())
                        .append('.')
                        .append("Table")
                        .append('.')
                        .append("ddlMode")
                        .toString();

                configValue = context.tableMetaProperties().getProperty(key);

                if (_StringUtils.hasText(configValue)) {
                    createDdl = Boolean.parseBoolean(configValue);
                } else {
                    createDdl = true;
                }
            }
            break;
            default:
                throw _Exceptions.unexpectedEnum(mode);
        }
        return createDdl;
    }

    /// Validates that the given parent table meta is a `DefaultTableMeta` instance
    /// and its Java type is assignable from the specified domain class.
    ///
    /// @param parentTableMeta the parent table metadata to validate
    /// @param domainClass     the child domain class
    /// @throws MetaException if validation fails
    static <T> void assertParentTableMeta(ParentTableMeta<? super T> parentTableMeta
            , Class<T> domainClass) {
        if (!(parentTableMeta instanceof DefaultTableMeta)) {
            String m = String.format("%s isn't instance of %s", TableMeta.class.getName()
                    , DefaultTableMeta.class.getName());
            throw new MetaException(m);
        }
        if (!parentTableMeta.javaType().isAssignableFrom(domainClass)) {
            String m = String.format("%s java type[%s] isn't isAssignable from of %s", TableMeta.class.getName()
                    , parentTableMeta.javaType().getName()
                    , domainClass.getName());
            throw new MetaException(m);
        }
    }

    /// Resolves the **discriminator value** for a subclass in a table inheritance hierarchy.
    ///
    /// The discriminator value is obtained from the `@DiscriminatorValue` annotation
    /// on the domain class and must match a valid enum constant of the discriminator field type.
    ///
    /// @param fieldJavaClass the enum type of the discriminator field
    /// @param domainClass    the child domain class annotated with `@DiscriminatorValue`
    /// @return the resolved discriminator enum value
    /// @throws MetaException if `@DiscriminatorValue` is missing or the value is invalid
    static Enum<?> discriminatorValue(final Class<?> fieldJavaClass, final Class<?> domainClass) {

        final DiscriminatorValue discriminatorValue;
        discriminatorValue = domainClass.getAnnotation(DiscriminatorValue.class);
        if (discriminatorValue == null) {
            String m = String.format("Domain[%s] isn't annotated by %s."
                    , domainClass.getName(), DiscriminatorValue.class.getName());
            throw new MetaException(m);
        }

        try {
            return NameEnumType.valueOf(fieldJavaClass, discriminatorValue.value());
        } catch (IllegalArgumentException e) {
            String m = String.format("Domain %s DiscriminatorValue[%s] error",
                    domainClass.getName(), discriminatorValue.value());
            throw new MetaException(m, e);
        }
    }

    /// Locates the **discriminator field** from the field metadata map based on the
    /// `@Inheritance` annotation's value (which specifies the discriminator field name).
    ///
    /// The discriminator field must be of **enum type** and present in the field map.
    ///
    /// @param fieldMetaMap the map of field name to `FieldMeta`
    /// @param domainClass  the parent domain class annotated with `@Inheritance`
    /// @param <T>          the domain type
    /// @return the discriminator `FieldMeta`
    /// @throws MetaException if the discriminator field is not found or is not an enum type
    static <T> FieldMeta<T> discriminator(final Map<String, FieldMeta<T>> fieldMetaMap,
                                          final Class<T> domainClass) {
        final Inheritance inheritance = domainClass.getAnnotation(Inheritance.class);
        Objects.requireNonNull(inheritance);
        final String fieldName = inheritance.value();
        final FieldMeta<T> discriminator = fieldMetaMap.get(fieldName);
        if (discriminator == null) {
            throw notFoundDiscriminator(fieldName, domainClass);
        }
        final Class<?> fieldJavaType = discriminator.javaType();
        if (!fieldJavaType.isEnum()) {
            String m = String.format("Discriminator[%s] in domain[%s] isn't enum type.", fieldName, domainClass.getName());
            throw new MetaException(m);
        }
        return discriminator;
    }


    /// Builds the **mapped class pair** for a domain class by traversing its inheritance hierarchy.
    ///
    /// This method collects all classes in the extends chain that are annotated with
    /// `@MappedSuperclass` or `@Table`, and identifies the parent class annotated with
    /// `@Inheritance` (if any). Only one `@Inheritance` is allowed per hierarchy.
    ///
    /// @param domainClass the domain entity class to analyze
    /// @return a `DomainPair` containing the ordered list of mapped classes and the optional parent class
    /// @throws MetaException if multiple `@Inheritance` annotations are found in the hierarchy
    static DomainPair mappedClassPair(final Class<?> domainClass) throws MetaException {
        List<Class<?>> list = new ArrayList<>(6);
        // add entity class firstly
        list.add(domainClass);
        Class<?> parentDomainClass = null;

        final boolean inheritance = domainClass.getAnnotation(Inheritance.class) != null;
        for (Class<?> superClass = domainClass.getSuperclass(); superClass != null;
             superClass = superClass.getSuperclass()) {
            if (superClass.getAnnotation(Inheritance.class) != null) {
                if (inheritance) {
                    throw inheritanceDuplication(domainClass);
                }
                parentDomainClass = superClass;
                break;
            }
            if (superClass.getAnnotation(MappedSuperclass.class) != null
                    || superClass.getAnnotation(Table.class) != null) {
                list.add(superClass);
            } else {
                break;
            }
        }

        if (list.size() == 1) {
            list = Collections.singletonList(list.getFirst());
        } else {
            // reverse class list
            Collections.reverse(list);
            list = Collections.unmodifiableList(list);
        }
        return new DomainPair(list, parentDomainClass);
    }


    /// Creates the **ordered list of field metadata** for all `@Column`-annotated fields
    /// in the domain class and its mapped superclasses.
    ///
    /// Fields are collected from the topmost superclass down to the domain class itself,
    /// preserving declaration order. Static fields and fields without `@Column` are skipped.
    ///
    /// @param tableMeta the resolved table metadata
    /// @param context   the metadata resolution context
    /// @param <T>       the domain type
    /// @return an immutable list of `FieldMeta` instances in declaration order
    static <T> List<FieldMeta<T>> createFieldMetaList(final TableMeta<T> tableMeta, final MetaContext context) {
        final Class<?> domainClass = tableMeta.javaType();

        final List<FieldMeta<T>> list = new ArrayList<>(20);
        final boolean inheritance = domainClass.getAnnotation(Inheritance.class) != null;
        Field[] fieldArray;
        Field field;
        Column column;
        for (Class<?> clazz = domainClass; clazz != null; clazz = clazz.getSuperclass()) {

            if (clazz != domainClass && clazz.getAnnotation(Inheritance.class) != null) {
                if (inheritance) {
                    throw inheritanceDuplication(domainClass);
                }
                list.add(TableFieldMeta.createFieldMeta(tableMeta, findIdField(clazz), context));
                break;
            }

            if (clazz != domainClass
                    && clazz.getAnnotation(MappedSuperclass.class) == null
                    && clazz.getAnnotation(Table.class) == null) {
                break;
            }

            fieldArray = clazz.getDeclaredFields();
            for (int i = fieldArray.length - 1; i > -1; i--) {
                field = fieldArray[i];
                if (Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                column = field.getAnnotation(Column.class);
                if (column == null) {
                    continue;
                }
                list.add(TableFieldMeta.createFieldMeta(tableMeta, field, context));
            } // field loop

        } // class loop

        Collections.reverse(list);
        return List.copyOf(list);
    }


    /// Creates an **immutable map** from field name to `FieldMeta` for fast lookup.
    ///
    /// @param list the list of field metadata to index
    /// @param <T>  the domain type
    /// @return an unmodifiable map keyed by field name
    /// @throws MetaException if duplicate field names are detected
    static <T> Map<String, FieldMeta<T>> createFieldMap(final List<FieldMeta<T>> list) {
        final Map<String, FieldMeta<T>> map = _Collections.hashMapForSize(list.size());
        for (FieldMeta<T> field : list) {
            if (map.putIfAbsent(field.fieldName(), field) != null) {
                throw fieldMetaDuplication(field);
            }
        }
        return Map.copyOf(map);
    }


    /// Creates the **index metadata list** for a table based on `@Index` annotations.
    ///
    /// Each `@Index` is resolved for name, type, fields, collation, and opclass.
    /// If no primary key index is found among the declared indexes, an automatic
    /// unique index is appended for the `id` field.
    ///
    /// ### Example
    /// ```java
    /// @Table(name = "stock",
    ///     indexes = @Index(name = "${DEFAULT}", unique = true, fieldList = {"exchange", "code"}))
    /// public class Stock { ... }
    /// // Generates index name: "uni_stock_exchange_code"
    /// ```
    ///
    /// @param tableMeta    the resolved table metadata
    /// @param context      the metadata resolution context
    /// @param fieldMetaMap the field name to `FieldMeta` map for resolving index columns
    /// @param <T>          the domain type
    /// @return an immutable list of `IndexMeta` instances
    static <T> List<IndexMeta<T>> createIndexList(TableMeta<T> tableMeta, MetaContext context,
                                                  Map<String, FieldMeta<T>> fieldMetaMap) {
        final Class<?> domainClass = tableMeta.javaType();
        final Table table = domainClass.getAnnotation(Table.class);
        final Index[] indexArray = table.indexes();
        final List<IndexMeta<T>> indexList = new ArrayList<>(indexArray.length);

        IndexField[] fieldArray;
        List<IndexColumnMeta> metaList;
        List<FieldMeta<T>> fieldList;
        String indexName, indexType;
        String[] fieldNameArray;
        Index index;
        boolean containIdIndex = false;
        for (int i = 0; i < indexArray.length; i++) {
            index = indexArray[i];
            indexType = parseIndexType(domainClass, index.type(), i, context);
            if (indexType == null) {
                continue;
            }
            indexName = parseIndexName(tableMeta, domainClass, index, i, context);
            if (indexName == null) {
                continue;
            }

            fieldArray = index.fields();
            if (fieldArray.length == 0) {
                fieldNameArray = index.fieldList();
                fieldList = new ArrayList<>(fieldNameArray.length);
                metaList = createIndexColumnList(index.fieldList(), tableMeta, i, fieldList, context, fieldMetaMap);
            } else {
                fieldList = new ArrayList<>(fieldArray.length);
                metaList = createIndexColumnMetaList(i, fieldArray, tableMeta, fieldList, indexName, context, fieldMetaMap);
            }

            _Assert.isTrue(metaList.size() == fieldList.size(), "");

            if (fieldList.size() == 1 && fieldList.getFirst() instanceof PrimaryFieldMeta<T>) {
                if (!index.unique()) {
                    String m = String.format("%s %s[%s].%s error", domainClass.getName(), "Index", i, "unique");
                    throw new MetaException(m);
                }
                containIdIndex = true;
            }

            indexList.add(new DefaultIndexMeta<>(tableMeta, indexName, index.unique(), fieldList, metaList, indexType));
        }

        if (!containIdIndex) {
            indexName = context.tempBuilderAndClear()
                    .append("uni")
                    .append('_')
                    .append(tableMeta.tableName().toLowerCase(Locale.ROOT))
                    .append('_')
                    .append(tableMeta.id().columnName().toLowerCase(Locale.ROOT))
                    .toString();

            metaList = context.minIndexColumnMetaList(List.of(MinIndexColumnMeta.INSTANCE));
            fieldList = List.of(tableMeta.id());
            indexList.add(new DefaultIndexMeta<>(tableMeta, indexName, true, fieldList, metaList, ""));
        }
        return List.copyOf(indexList);
    }


    @Nullable
    private static String parseIndexType(Class<?> domainClass, final String indexType, int indexOfIndex, MetaContext context) {
        if (indexType.isEmpty()) {
            return "";
        }
        final String finalIndexType;
        switch (indexType) {
            case DEFAULT_EXP:
            case RUNTIME_EXP:
            case OPTIONAL_EXP: {
                final String key, configIndexType;
                key = context.tempBuilderAndClear()
                        .append(domainClass.getName())
                        .append('.')
                        .append("IndexMeta")
                        .append('[')
                        .append(indexOfIndex)
                        .append(']')
                        .append('.')
                        .append("type")
                        .toString();

                configIndexType = context.tableMetaProperties().getProperty(key);

                if (_StringUtils.hasText(configIndexType)) {
                    finalIndexType = configIndexType.trim();
                } else finalIndexType = switch (indexType) {
                    case DEFAULT_EXP -> "";
                    case RUNTIME_EXP -> throw new MetaException(String.format("%s no config", key));
                    case OPTIONAL_EXP -> null;
                    default -> throw new IllegalStateException("bug");
                };
            }
            break;
            default:
                finalIndexType = indexType;
        }
        return finalIndexType;
    }

    @Nullable
    private static String parseIndexName(TableMeta<?> tableMeta, Class<?> domainClass, final Index index,
                                         int indexOfIndex, MetaContext context) {
        final String indexName = index.name();
        final String finalIndexName;
        switch (indexName) {
            case DEFAULT_VALUE_EXP:
                finalIndexName = defaultIndexName(tableMeta, index, indexOfIndex, context);
                break;
            case DEFAULT_EXP:
            case RUNTIME_EXP:
            case OPTIONAL_EXP: {
                final String key, configIndexName;
                key = context.tempBuilderAndClear()
                        .append(domainClass.getName())
                        .append('.')
                        .append("IndexMeta")
                        .append('[')
                        .append(indexOfIndex)
                        .append(']')
                        .append('.')
                        .append("name")
                        .toString();

                configIndexName = context.tableMetaProperties().getProperty(key);

                if (_StringUtils.hasText(configIndexName)) {
                    finalIndexName = configIndexName.trim();
                } else finalIndexName = switch (indexName) {
                    case DEFAULT_EXP -> defaultIndexName(tableMeta, index, indexOfIndex, context);
                    case RUNTIME_EXP -> throw new MetaException(String.format("%s no config", key));
                    case OPTIONAL_EXP -> null;
                    default -> throw new IllegalStateException("bug");
                };
            }
            break;
            default: {
                if (!_StringUtils.hasText(indexName)) {
                    String m = String.format("%s %s[%s].%s error", domainClass.getName(), "Index", indexOfIndex, "name");
                    throw new MetaException(m);
                }
                finalIndexName = indexName;
            } // default
        } // switch
        return finalIndexName;
    }


    /// @param propName collation or opclass
    private static String parseIndexColumnProperty(Class<?> domainClass, final String propValue, int indexOfIndex,
                                                   String fieldName, MetaContext context, final String propName) {
        if (propValue.isEmpty()) {
            return "";
        }
        final String finalOpclass;
        switch (propValue) {
            case DEFAULT_EXP:
            case RUNTIME_EXP: {
                final String key, configOpclass;
                key = context.tempBuilderAndClear()
                        .append(domainClass.getName())
                        .append('.')
                        .append("IndexMeta")
                        .append('[')
                        .append(indexOfIndex)
                        .append(']')
                        .append('.')
                        .append("field")
                        .append('.')
                        .append(fieldName)
                        .append('.')
                        .append(propName)
                        .toString();

                configOpclass = context.tableMetaProperties().getProperty(key);

                if (_StringUtils.hasText(configOpclass)) {
                    finalOpclass = configOpclass.trim();
                } else finalOpclass = switch (propValue) {
                    case DEFAULT_EXP -> "";
                    case RUNTIME_EXP -> throw new MetaException(String.format("%s no config", key));
                    default -> throw new IllegalStateException("bug");
                };
            }
            break;
            case OPTIONAL_EXP:
                throw new MetaException(String.format("%s in %s index[%s] field[%s] property[%s] is unsupported", propValue,
                        domainClass.getName(), indexOfIndex, fieldName, propName));
            default:
                finalOpclass = propValue;
        }
        return finalOpclass;
    }

    /// @see #parseIndexName(TableMeta, Class, Index, int, MetaContext)
    private static String defaultIndexName(final TableMeta<?> tableMeta, final Index index, int indexOfIndex,
                                           final MetaContext context) {
        final StringBuilder builder = context.tempBuilderAndClear();
        if (index.unique()) {
            builder.append("uni");
        } else {
            builder.append("idx");
        }
        builder.append('_')
                .append(tableMeta.tableName().toLowerCase(Locale.ROOT));

        FieldMeta<?> fieldMeta;
        final IndexField[] fieldArray = index.fields();
        if (fieldArray.length > 0) {
            for (IndexField field : fieldArray) {
                fieldMeta = tableMeta.tryField(field.name()); // FieldMeta is created before IndexMeta
                if (fieldMeta == null) {
                    throw notFoundIndexField(tableMeta, indexOfIndex, field.name());
                }
                builder.append('_')
                        .append(fieldMeta.columnName().toLowerCase(Locale.ROOT));
            }
        } else {
            final String[] fieldNameArray = index.fieldList();
            for (String filedName : fieldNameArray) {
                fieldMeta = tableMeta.tryField(filedName); // FieldMeta is created before IndexMeta
                if (fieldMeta == null) {
                    throw notFoundIndexField(tableMeta, indexOfIndex, filedName);
                }
                builder.append('_')
                        .append(fieldMeta.columnName().toLowerCase(Locale.ROOT));
            }
        }

        return builder.toString();
    }


    /// @see #createIndexList(TableMeta, MetaContext, Map)
    private static <T> List<IndexColumnMeta> createIndexColumnMetaList(final int indexOfIndex, IndexField[] fieldArray,
                                                                       TableMeta<T> tableMeta, List<FieldMeta<T>> fieldList,
                                                                       String indexName, MetaContext context,
                                                                       Map<String, FieldMeta<T>> fieldMetaMap) {
        final Class<?> domainClass = tableMeta.javaType();
        final List<IndexColumnMeta> list = new ArrayList<>(fieldArray.length);
        String fieldName, collation, opclass;
        FieldMeta<T> fieldMeta;
        for (IndexField field : fieldArray) {
            fieldName = field.name();

            fieldMeta = fieldMetaMap.get(fieldName);
            if (fieldMeta == null) {
                throw notFoundIndexField(tableMeta, indexOfIndex, fieldName);
            }
            collation = parseIndexColumnProperty(domainClass, field.collation(), indexOfIndex, fieldName, context, "collation");

            opclass = parseIndexColumnProperty(domainClass, field.opclass(), indexOfIndex, fieldName, context, "opclass");

            list.add(new DefaultIndexColumnMeta(collation, opclass, field.order(), field.nulls()));
            fieldList.add(fieldMeta);
        }
        return context.indexColumnMetaList(list);
    }

    /// @see #createIndexList(TableMeta, MetaContext, Map)
    private static <T> List<IndexColumnMeta> createIndexColumnList(String[] fieldNameArray, TableMeta<T> tableMeta,
                                                                   int indexOfIndex, List<FieldMeta<T>> fieldList,
                                                                   MetaContext context,
                                                                   Map<String, FieldMeta<T>> fieldMetaMap) {
        if (fieldNameArray.length == 0) {
            throw noIndexField(tableMeta, indexOfIndex);
        }
        final List<IndexColumnMeta> list = new ArrayList<>(fieldNameArray.length);

        FieldMeta<T> fieldMeta;
        for (String fieldName : fieldNameArray) {
            fieldMeta = fieldMetaMap.get(fieldName);
            if (fieldMeta == null) {
                throw notFoundIndexField(tableMeta, indexOfIndex, fieldName);
            }
            list.add(MinIndexColumnMeta.INSTANCE);
            fieldList.add(fieldMeta);
        }
        return context.minIndexColumnMetaList(list);
    }


    /// Builds the **generator execution chain** by resolving field-level dependencies.
    ///
    /// Fields with `GeneratorMeta` are sorted by dependency depth so that
    /// generators with no dependencies execute first. The dependency is declared
    /// via the `FieldGenerator.DEPEND_FIELD_NAME` parameter.
    ///
    /// @param nameToField the map of field name to `FieldMeta`
    /// @param <T>         the domain type
    /// @return an immutable list of fields ordered by generator dependency depth
    /// @throws MetaException if a referenced dependent field is not found
    static <T> List<FieldMeta<?>> createGeneratorChain(final Map<String, FieldMeta<T>> nameToField)
            throws MetaException {

        final List<Pair<FieldMeta<T>, Integer>> levelList = new ArrayList<>(4);
        GeneratorMeta generatorMeta;
        String dependName;
        for (FieldMeta<T> field : nameToField.values()) {

            generatorMeta = field.generator();
            if (generatorMeta == null) {
                continue;
            }
            dependName = generatorMeta.params().get(FieldGenerator.DEPEND_FIELD_NAME);
            int level = 0;
            for (FieldMeta<?> dependField; dependName != null; ) {
                dependField = nameToField.get(dependName);
                if (dependField == null) {
                    String m = String.format("%s depend %s,but not found dependent field[%s] in %s"
                            , field, dependName, dependName, field.tableMeta());
                    throw new MetaException(m);
                }
                level++;
                generatorMeta = dependField.generator();
                if (generatorMeta == null) {
                    break;
                }
                dependName = generatorMeta.params().get(FieldGenerator.DEPEND_FIELD_NAME);
            }
            levelList.add(Pair.create(field, level));
        }

        final List<FieldMeta<?>> generatorChain;
        switch (levelList.size()) {
            case 0:
                generatorChain = List.of();
                break;
            case 1:
                generatorChain = List.of(levelList.getFirst().getFirst());
                break;
            default: {
                levelList.sort(Comparator.comparingInt(Pair::getSecond));
                final List<FieldMeta<T>> list = new ArrayList<>(levelList.size());
                for (Pair<FieldMeta<T>, Integer> f : levelList) {
                    list.add(f.getFirst());
                }
                generatorChain = List.copyOf(list);
            }
        }
        return generatorChain;
    }


    private static Class<?> getTypeObjetClass(DatabaseObject.FieldObject field) {
        final Class<?> clazz;
        if (field instanceof FieldMeta<?>) {
            clazz = ((FieldMeta<?>) field).tableMeta().javaType();
        } else if (field instanceof CompositeField) {
            clazz = ((CompositeField) field).compositeType().javaType();
        } else {
            throw new IllegalArgumentException("bug");
        }
        return clazz;

    }

    /// @see #createFieldMetaList(TableMeta, MetaContext)
    private static Field findIdField(final Class<?> domainClass) {
        Field targetField = null;
        topLoop:
        for (Class<?> clazz = domainClass; clazz != null; clazz = clazz.getSuperclass()) {
            for (Field field : clazz.getDeclaredFields()) {
                if (!field.getName().equals(_MetaBridge.ID)) {
                    continue;
                }
                if (field.getAnnotation(Column.class) == null) {
                    continue;
                }
                targetField = field;
                break topLoop;
            } // inner loop
        } // top loop
        if (targetField == null) {
            String m = String.format("Not found %s in %s", _MetaBridge.ID, domainClass.getName());
            throw new MetaException(m);
        }
        return targetField;
    }

    /// @see #columnCollation(Column, DatabaseObject.FieldObject, MetaContext)
    private static String columnStringValue(final String value, String method, DatabaseObject.FieldObject field,
                                            MetaContext context) {
        final String finalValue;
        switch (value) {
            case DEFAULT_EXP:
            case RUNTIME_EXP: {
                final String key, configValue;
                key = context.tempBuilderAndClear()
                        .append(getTypeObjetClass(field).getName())
                        .append('.')
                        .append(field.fieldName())
                        .append('.')
                        .append("Column")
                        .append('.')
                        .append(method)
                        .toString();

                configValue = context.tableMetaProperties().getProperty(key);
                if (_StringUtils.hasText(configValue)) {
                    finalValue = configValue.trim();
                } else switch (value) {
                    case DEFAULT_EXP: {
                        if (method.equals("defaultValue")) {
                            finalValue = zeroValue(field);
                        } else {
                            finalValue = "";
                        }
                    }
                    break;
                    case RUNTIME_EXP:
                        throw new MetaException(String.format("%s no config", key));
                    default:
                        throw new IllegalStateException("bug");
                }
            }
            break;
            default:
                finalValue = value;

        } // switch
        return finalValue;
    }


    /// @see #columnStringValue(String, String, DatabaseObject.FieldObject, MetaContext)
    private static String zeroValue(DatabaseObject.FieldObject field) {
        final MappingType type;
        final String value;
        if (field instanceof PrimaryFieldMeta<?>
                || (type = field.mappingType()) instanceof MappingType.SqlEnum
                || Enum.class.isAssignableFrom(field.javaType())) {
            value = "";
        } else if (type instanceof MappingType.SqlString) {
            value = "''";
        } else if (type instanceof MappingType.SqlNumber) {
            value = "0";
        } else if (type instanceof MappingType.SqlBoolean) {
            value = "false";
        } else if (type instanceof MappingType.SqlLocalTime) {
            value = "'00:00:00'";
        } else if (type instanceof MappingType.SqlLocalDate) {
            value = "'1970-01-01'";
        } else if (type instanceof MappingType.SqlLocalDateTime) {
            value = "'1970-01-01 00:00:00'";
        } else if (type instanceof MappingType.SqlOffsetDateTime) {
            value = "'1970-01-01 00:00:00+00:00'";
        } else if (type instanceof MappingType.SqlOffsetTime) {
            value = "'00:00:00+00:00'";
        } else {
            value = "";
        }
        return value;
    }

    /// @see #columnIntValue(int, String, DatabaseObject.FieldObject, MetaContext)
    private static int precisionDefault(DatabaseObject.FieldObject field) {
        final MappingType type;
        final Class<?> javaType;
        final int value;
        if (field instanceof PrimaryFieldMeta<?>) {
            value = -1;
        } else if ((type = field.mappingType()) instanceof MappingType.SqlDecimal) {
            value = 16;
        } else if (type instanceof MappingType.SqlEnum t) {
            int maxLength = -1, length;
            for (String label : t.enumLabelList()) {
                length = label.length();
                if (length > maxLength) {
                    maxLength = length;
                }
            }
            value = maxLength + 5;
        } else if (type instanceof MappingType.SqlString) {
            value = 255;
        } else if (Enum.class.isAssignableFrom(javaType = field.javaType())) {
            int maxLength = -1, length;
            for (Enum<?> e : ClassUtils.getEnumConstants(javaType)) {
                length = e.name().length();
                if (length > maxLength) {
                    maxLength = length;
                }
            }
            value = maxLength + 5;
        } else if (type instanceof UUIDType || javaType == UUID.class) {
            value = 36;
        } else if (type instanceof MappingType.SqlVector) {
            value = 1024;
        } else {
            value = -1;
        }
        return value;
    }

    /// @see #columnIntValue(int, String, DatabaseObject.FieldObject, MetaContext)
    private static int scaleDefault(DatabaseObject.FieldObject field) {
        final MappingType type;
        final int value;
        if (field instanceof PrimaryFieldMeta<?>) {
            value = -1;
        } else if ((type = field.mappingType()) instanceof MappingType.SqlDecimal) {
            value = 3;
        } else if (type instanceof MappingType.SqlLocalDateTime
                || type instanceof MappingType.SqlOffsetDateTime
                || type instanceof MappingType.SqlLocalTime
                || type instanceof MappingType.SqlOffsetTime) {
            value = 6;
        } else {
            value = -1;
        }
        return value;
    }


    /// @see #columnPrecision(Column, DatabaseObject.FieldObject, MetaContext)
    /// @see #columnScale(Column, DatabaseObject.FieldObject, MetaContext)
    private static int columnIntValue(final int value, String method, DatabaseObject.FieldObject field, MetaContext context) {
        final int finalValue;
        switch (value) {
            case Column.DEFAULT_EXP:
            case Column.RUNTIME_EXP: {
                final String key, configValue;
                key = context.tempBuilderAndClear()
                        .append(getTypeObjetClass(field).getName())
                        .append('.')
                        .append(field.fieldName())
                        .append('.')
                        .append("Column")
                        .append('.')
                        .append(method)
                        .toString();

                configValue = context.tableMetaProperties().getProperty(key);
                if (_StringUtils.hasText(configValue)) {
                    try {
                        finalValue = Integer.parseInt(configValue.trim());
                    } catch (NumberFormatException e) {
                        throw new MetaException(String.format("%s config error", key), e);
                    }
                } else switch (value) {
                    case Column.DEFAULT_EXP: {
                        if (method.equals("precision")) {
                            finalValue = precisionDefault(field);
                        } else if (method.equals("scale")) {
                            finalValue = scaleDefault(field);
                        } else {
                            finalValue = -1;
                        }
                    }
                    break;
                    case Column.RUNTIME_EXP:
                        throw new MetaException(String.format("%s no config", key));
                    default:
                        throw new IllegalStateException("bug");
                }
            }
            break;
            default: {
                finalValue = Math.max(value, -1);
            } // default
        } // switch
        return finalValue;
    }


    static MetaException notFoundDiscriminator(String fieldName, Class<?> domainClass) {
        String m = String.format("Not found discriminator[%s] in domain[%s].", fieldName, domainClass.getName());
        return new MetaException(m);
    }


    /// @see #mappedClassPair(Class)
    private static MetaException inheritanceDuplication(Class<?> domainClass) {
        String m = String.format("Domain[%s] extends link %s count great than 1 in link of extends",
                domainClass.getName(),
                Inheritance.class.getName());
        return new MetaException(m);
    }


    private static MetaException notFoundIndexField(TableMeta<?> tableMeta, int indexOfIndex, String fieldName) {
        String m = String.format("Not found index field[%s] for domain[%s] index[%s]",
                fieldName, tableMeta.javaType().getName(), indexOfIndex);
        return new MetaException(m);
    }

    private static MetaException noIndexField(TableMeta<?> tableMeta, int indexOfIndex) {
        String m = String.format("Not found index field for domain[%s] index[%s]",
                tableMeta.javaType().getName(), indexOfIndex);
        return new MetaException(m);
    }

    /// @see #createFieldMap(List)
    private static MetaException fieldMetaDuplication(FieldMeta<?> fieldMeta) {
        String m = String.format("%s.%s duplication.",
                fieldMeta.tableMeta().javaType().getName(), fieldMeta.fieldName());
        return new MetaException(m);
    }

    private static MetaException tableNameIsCamel(Class<?> domainClass) {
        String m = String.format("%s %s.%s is came", domainClass.getName(), "Table", "name");
        return new MetaException(m);
    }


    /// Default implementation of `IndexMeta` that holds resolved index metadata
    /// including name, uniqueness, field list, column list, and index type.
    ///
    /// @param <T> the domain type
    private static final class DefaultIndexMeta<T> implements IndexMeta<T> {

        private final TableMeta<T> table;

        private final String name;

        private final boolean unique;

        private final List<FieldMeta<T>> fieldList;

        private final List<IndexColumnMeta> columnList;

        private final String indexType;

        private DefaultIndexMeta(TableMeta<T> table, String name, boolean unique, List<FieldMeta<T>> fieldList,
                                 List<IndexColumnMeta> columnList, String indexType) {
            if (fieldList.size() != columnList.size()) {
                throw new IllegalArgumentException();
            }
            this.table = table;
            this.name = name;
            this.unique = unique;
            this.fieldList = List.copyOf(fieldList);
            this.columnList = List.copyOf(columnList);
            this.indexType = indexType;
        }

        @Override
        public TableMeta<T> tableMeta() {
            return this.table;
        }

        @Override
        public String name() {
            return this.name;
        }

        @Override
        public List<FieldMeta<T>> fieldList() {
            return this.fieldList;
        }

        @Override
        public List<IndexColumnMeta> columnList() {
            return this.columnList;
        }

        @Override
        public boolean isPrimaryKey() {
            return this.unique
                    && this.fieldList.size() == 1
                    && this.fieldList.getFirst() instanceof PrimaryFieldMeta<T>;
        }

        @Override
        public boolean isUnique() {
            return this.unique;
        }

        @Override
        public String type() {
            return this.indexType;
        }


    } // DefaultIndexMeta


    /// Represents a pair of the ordered mapped class hierarchy and the optional
    /// parent class annotated with `@Inheritance`.
    ///
    /// The `mappedList` contains classes from topmost superclass to the domain class,
    /// while `parent` is non-null only when table inheritance is in use.
    static final class DomainPair {

        final List<Class<?>> mappedList;

        final Class<?> parent;

        private DomainPair(List<Class<?>> mappedList, @Nullable Class<?> parent) {
            this.mappedList = mappedList;
            this.parent = parent;
        }


    }


    /// Minimal `IndexColumnMeta` implementation with all default values.
    ///
    /// Used when index fields are specified via `@Index.fieldList()` (simple string array)
    /// without detailed per-column configuration (no collation, opclass, or sort order).
    private static final class MinIndexColumnMeta implements IndexColumnMeta {

        private static final MinIndexColumnMeta INSTANCE = new MinIndexColumnMeta();

        private MinIndexColumnMeta() {
        }

        @Override
        public String collation() {
            return "";
        }

        @Override
        public String opclass() {
            return "";
        }

        @Override
        public SortOrder order() {
            return SortOrder.DEFAULT;
        }

        @Override
        public NullsOrder nulls() {
            return NullsOrder.DEFAULT;
        }


    } // MinIndexColumnMeta

    /// Full-featured `IndexColumnMeta` implementation that stores collation, opclass,
    /// sort order, and nulls order for each index column.
    ///
    /// Used when index fields are specified via `@Index.fields()` with `@IndexField`
    /// annotations providing detailed per-column configuration.
    private static final class DefaultIndexColumnMeta implements IndexColumnMeta {

        private final String collation;

        private final String opclass;

        private final SortOrder order;

        private final NullsOrder nulls;

        private DefaultIndexColumnMeta(String collation, String opclass, SortOrder order, NullsOrder nulls) {
            this.collation = collation;
            this.opclass = opclass;
            this.order = order;
            this.nulls = nulls;
        }

        @Override
        public String collation() {
            return this.collation;
        }

        @Override
        public String opclass() {
            return this.opclass;
        }

        @Override
        public SortOrder order() {
            return this.order;
        }

        @Override
        public NullsOrder nulls() {
            return this.nulls;
        }


        @Override
        public int hashCode() {
            return Objects.hash(this.collation, this.opclass, this.order, this.nulls);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof DefaultIndexColumnMeta o) {
                match = Objects.equals(this.collation, o.collation)
                        && Objects.equals(this.opclass, o.opclass)
                        && this.order == o.order
                        && this.nulls == o.nulls;
            } else {
                match = false;
            }
            return match;
        }

    } // DefaultIndexColumnMeta


}
