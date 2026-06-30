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

package io.army.modelgen;

import io.army.annotation.Column;
import io.army.annotation.MappedSuperclass;
import io.army.annotation.Table;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.TypeMirror;
import java.util.*;
import java.util.function.Predicate;

abstract class FieldHandlerSupport {

    /// Placeholder expression that resolves from `TableMeta.properties` if present,
    /// otherwise falls back to a **built-in default** convention.
    ///
    /// For column names, the default is camelCase-to-snake_case conversion.
    /// For table names, it converts the simple class name to lowercase with underscores.
    static final String DEFAULT_EXP = "${DEFAULT}";

    /// Placeholder expression that **must** be resolved from `TableMeta.properties` at runtime.
    ///
    /// Throws `MetaException` if no corresponding property entry is found.
    /// Use this when the value is environment-specific and has no sensible built-in default.
    static final String RUNTIME_EXP = "${RUNTIME}";

    /// Placeholder expression for **optional** metadata overrides.
    ///
    /// If the property is not found, the annotated element (e.g., an index) is silently skipped
    /// rather than causing an error. Not all attributes support this expression.
    static final String OPTIONAL_EXP = "${OPTIONAL}";

    /// Placeholder expression that resolves to a **convention-based default value**.
    ///
    /// Currently used primarily for index name generation, where the framework
    /// automatically constructs names like `uni_{table}_{column}` or `idx_{table}_{column}`.
    static final String DEFAULT_VALUE_EXP = "${DEFAULT_VALUE}";


    final StringBuilder tempBuilder;

    private Map<String, String> tableMetaMap;

    FieldHandlerSupport(StringBuilder tempBuilder) {
        this.tempBuilder = tempBuilder;
    }

    /// @param endFunc true : end loop
    final List<VariableElement> findFields(final TypeElement targetElement, final boolean createList,
                                           final Predicate<VariableElement> endFunc) {
        final String domainName = MetaUtils.getClassName(targetElement);

        TypeElement superElement = targetElement;


        final List<VariableElement> fieldList;
        final Set<String> fieldNameSet, columnNameSet;

        if (createList) {
            fieldList = new ArrayList<>(10);
            fieldNameSet = ArmyCollections.hashSetForSize(10);
            columnNameSet = ArmyCollections.hashSetForSize(10);
        } else {
            fieldList = List.of();
            fieldNameSet = Set.of();
            columnNameSet = fieldNameSet;
        }

        VariableElement field;
        String fieldName, columnName;
        Column column;

        topLoop:
        for (TypeMirror superMirror = superElement.asType(); ; superMirror = superElement.getSuperclass()) {

            if (superMirror instanceof NoType) {
                break;
            }

            superElement = (TypeElement) ((DeclaredType) superMirror).asElement();

            if (superElement.getNestingKind() != NestingKind.TOP_LEVEL) {
                String m = String.format("Nested class %s is not supported.", MetaUtils.getClassName(superElement));
                addErrorMsg(m);
                break;
            }

            if (superElement != targetElement
                    && superElement.getAnnotation(MappedSuperclass.class) == null
                    && superElement.getAnnotation(Table.class) == null) {
                break;
            }

            for (Element element : superElement.getEnclosedElements()) {

                if (element.getKind() != ElementKind.FIELD
                        || element.getModifiers().contains(Modifier.STATIC)
                        || (column = element.getAnnotation(Column.class)) == null) {
                    continue;
                }

                field = (VariableElement) element;

                if (createList) {
                    fieldName = field.getSimpleName().toString();
                    if (fieldNameSet.add(fieldName)) {
                        fieldList.add(field);
                        columnName = getColumnName(domainName, fieldName, column);
                        if (!columnName.startsWith("${") && !columnNameSet.add(columnName)) {
                            String m = String.format("%s.%s column[%s] duplication", domainName, fieldName, columnName);
                            addErrorMsg(m);
                        }
                    } else {
                        String m = String.format("Field %s.%s is overridden", MetaUtils.getClassName(superElement), fieldName);
                        addErrorMsg(m);
                    }
                }

                if (endFunc.test(field)) {
                    break topLoop;
                }

            } // field loop

        } // TypeMirror  loop

        return fieldList;
    }

    /// @return lower case column
    final String getColumnName(final String domainName, final String fieldName, final Column column) {

        final String value = column.name();
        final String finalValue;
        switch (value) {
            case DEFAULT_EXP:
            case RUNTIME_EXP: {
                final String key, configValue;
                key = getAndClearTempBuilder()
                        .append(domainName)
                        .append('.')
                        .append(fieldName)
                        .append('.')
                        .append("Column")
                        .append('.')
                        .append("name")
                        .toString();

                configValue = getTableMetaMap().get(key);
                if (MetaUtils.hasText(configValue)) {
                    validateColumnName(domainName, fieldName, configValue.trim());
                    finalValue = configValue.trim().toLowerCase(Locale.ROOT);
                } else if (value.equals(DEFAULT_EXP)) {
                    finalValue = _MetaBridge.camelToLowerCase(fieldName, this.tempBuilder);
                } else {
                    finalValue = value;
                }
            }
            break;
            case OPTIONAL_EXP: {
                String m = String.format("%s in %s.%s %s.%s is unsupported", value, domainName, fieldName,
                        "Column", "name");
                addErrorMsg(m);
                finalValue = value;
            }
            break;
            default: {
                if (value.isEmpty()) {
                    finalValue = _MetaBridge.camelToLowerCase(fieldName, this.tempBuilder);
                } else {
                    validateColumnName(domainName, fieldName, value);
                    finalValue = value.toLowerCase(Locale.ROOT);
                }
            } // default

        } // switch


        return finalValue;
    }


    final Map<String, String> getTableMetaMap() {
        Map<String, String> map = this.tableMetaMap;
        if (map != null) {
            return map;
        }
        final Properties properties;
        properties = _MetaBridge.loadArmyProperties("TableMeta");

        final int size = properties.size();

        if (size == 0) {
            map = Map.of();
        } else {
            map = ArmyCollections.hashMapForSize(size);
            for (Object key : properties.keySet()) {
                map.put((String) key, properties.getProperty((String) key));
            }
        }
        this.tableMetaMap = map = Map.copyOf(map);
        return map;
    }

    final StringBuilder getAndClearTempBuilder() {
        final StringBuilder builder = this.tempBuilder;
        builder.setLength(0);
        return builder;
    }


    abstract void addErrorMsg(String msg);


    private void validateColumnName(final String className, final String fieldName, final String columnName) {
        if (_MetaBridge.isCamelCase(columnName)) {
            String m = String.format("Field %s.%s column name[%s] is camel.", className, fieldName, columnName);
            addErrorMsg(m);
        } else if (!columnName.equals(columnName.trim())) {
            String m = String.format("please trim Field %s.%s column name[%s].", className, fieldName, columnName);
            addErrorMsg(m);
        }
    }

}
