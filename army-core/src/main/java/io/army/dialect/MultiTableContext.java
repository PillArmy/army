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

package io.army.dialect;

import io.army.annotation.UpdateMode;
import io.army.criteria.*;
import io.army.criteria.impl.inner._DerivedTable;
import io.army.criteria.impl.inner._Selection;
import io.army.meta.FieldMeta;
import io.army.meta.TableMeta;
import io.army.modelgen._MetaBridge;
import io.army.util._Collections;
import io.army.util._Exceptions;

import io.army.lang.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

final class MultiTableContext implements _MultiTableContext,
        _DmlContext._SetClauseContextSpec,
        _DmlContext._ConditionFieldsSpec {

    final StatementContext stmtContext;

    final Map<String, TabularItem> aliasToTable;

    final Map<TableMeta<?>, String> tableToSafeAlias;

    private final BiConsumer<String, FieldMeta<?>> outerFieldConsumer;

    private final Consumer<FieldMeta<?>> outerFieldOnlyConsumer;

    private Map<String, String> aliasToSafeAlias;

    private List<SqlField> conditionFieldList;

    private boolean appendedUpdateTime;


    MultiTableContext(StatementContext stmtContext, TableContext tableContext,
                      @Nullable BiConsumer<String, FieldMeta<?>> outerFieldConsumer,
                      @Nullable Consumer<FieldMeta<?>> outerFieldOnlyConsumer) {
        this.stmtContext = stmtContext;
        this.aliasToTable = tableContext.aliasToTable;
        this.tableToSafeAlias = tableContext.tableToSafeAlias;
        this.outerFieldConsumer = outerFieldConsumer;
        this.outerFieldOnlyConsumer = outerFieldOnlyConsumer;

    }

    @Override
    public void appendField(final String tableAlias, final FieldMeta<?> field) {
        final BiConsumer<String, FieldMeta<?>> outerFieldConsumer;
        if (this.aliasToTable.get(tableAlias) == field.tableMeta()) {
            this.appendSafeField(tableAlias, field);
        } else if ((outerFieldConsumer = this.outerFieldConsumer) != null) {
            outerFieldConsumer.accept(tableAlias, field);
        } else {
            throw _Exceptions.unknownColumn(tableAlias, field);
        }
    }


    @Override
    public void appendField(final FieldMeta<?> field) {
        final TableMeta<?> fieldTable = field.tableMeta();
        final String safeTableAlias;
        safeTableAlias = this.tableToSafeAlias.get(fieldTable);
        final BiConsumer<String, FieldMeta<?>> outerFieldConsumer;
        if (safeTableAlias != null) {
            final StringBuilder sqlBuilder = this.stmtContext.sqlBuilder;
            sqlBuilder.append(_Constant.SPACE)
                    .append(safeTableAlias)
                    .append(_Constant.DOT);
            this.stmtContext.parser.safeObjectName(field, sqlBuilder);
        } else if (this.aliasToTable.containsValue(fieldTable)) {
            throw _Exceptions.selfJoinNonQualifiedField(field);
        } else if ((outerFieldConsumer = this.outerFieldConsumer) != null) {
            outerFieldConsumer.accept(null, field);
        } else {
            throw _Exceptions.unknownColumn(field);
        }
    }


    @Override
    public void appendFieldOnly(final FieldMeta<?> field) {
        final TableMeta<?> fieldTable = field.tableMeta();
        final Consumer<FieldMeta<?>> outerFieldOnlyConsumer;
        if (this.tableToSafeAlias.get(fieldTable) != null) {
            this.stmtContext.parser.safeObjectName(field, this.stmtContext.sqlBuilder);
        } else if (this.aliasToTable.containsValue(fieldTable)) {
            throw _Exceptions.selfJoinNonQualifiedField(field);
        } else if ((outerFieldOnlyConsumer = this.outerFieldOnlyConsumer) != null) {
            outerFieldOnlyConsumer.accept(field);
        } else {
            throw _Exceptions.unknownColumn(field);
        }
    }

    @Override
    public String safeTableAlias(final TableMeta<?> table, final String alias) {
        if (this.aliasToTable.get(alias) != table) {
            throw _Exceptions.unknownTable(table, alias);
        }
        String safeAlias;
        safeAlias = this.tableToSafeAlias.get(table);
        if (safeAlias == null) {
            // table self-join
            safeAlias = getAliasToSafeAlias().computeIfAbsent(alias, this.stmtContext.parser::identifier);
        }
        return safeAlias;
    }

    @Override
    public String safeTableAlias(final String alias) {
        if (this.aliasToTable.get(alias) == null) {
            throw _Exceptions.unknownTableAlias(alias);
        }
        return this.getAliasToSafeAlias().computeIfAbsent(alias, this.stmtContext.parser::identifier);
    }

    @Override
    public String saTableAliasOf(final TableMeta<?> table) {
        final String safeAlias;
        safeAlias = this.tableToSafeAlias.get(table);
        if (safeAlias == null) {
            if (this.aliasToTable.containsValue(table)) {
                throw _Exceptions.tableSelfJoin(table);
            } else {
                throw _Exceptions.unknownTable(table, "");
            }
        }
        return safeAlias;
    }

    @Override
    public TabularItem tabularItemOf(final String tableAlias) {
        final TabularItem tableItem;
        tableItem = this.aliasToTable.get(tableAlias);
        if (tableItem == null) {
            throw _Exceptions.unknownTableAlias(tableAlias);
        }
        return tableItem;
    }

    @Nullable
    @Override
    public String trySaTableAliasOf(TableMeta<?> table) {
        return this.tableToSafeAlias.get(table);
    }

    @Override
    public void appendSetLeftItem(final SqlField dataField, final @Nullable Expression updateTimePlaceholder) {
        final ArmyParser parser = this.stmtContext.parser;
        final StringBuilder sqlBuilder = this.stmtContext.sqlBuilder;

        final boolean updatable;
        if (dataField instanceof TableField) {
            updatable = ((TableField) dataField).updatable();
        } else if (parser.supportUpdateDerivedField) {
            final TableField f;
            f = ((_Selection) dataField).tableField();
            if (f == null) {
                throw _Exceptions.immutableField(dataField);
            }
            updatable = f.updatable();
        } else {
            throw _Exceptions.immutableField(dataField);
        }

        if (!updatable) {
            throw _Exceptions.immutableField(dataField);
        }

        final String fieldName = dataField.fieldName();

        if (updateTimePlaceholder == null && dataField instanceof TableField && _MetaBridge.UPDATE_TIME.equals(fieldName)) {
            throw _Exceptions.armyManageField((TableField) dataField);
        }

        if (!(dataField instanceof TableField)) {
            final DerivedField field = (DerivedField) dataField;
            final String tableAlias = field.tableAlias();
            final TabularItem tableItem = this.aliasToTable.get(tableAlias);
            if (!(tableItem instanceof DerivedTable)
                    || ((_DerivedTable) tableItem).refSelection(fieldName) == null) {
                throw _Exceptions.unknownColumn(field);
            }
            final String safeTableAlias;
            safeTableAlias = this.getAliasToSafeAlias().get(tableAlias);
            assert safeTableAlias != null;
            sqlBuilder.append(_Constant.SPACE)
                    .append(safeTableAlias)
                    .append(_Constant.DOT);
            parser.identifier(fieldName, sqlBuilder);
        } else if (dataField instanceof FieldMeta) {
            final FieldMeta<?> field = (FieldMeta<?>) dataField;
            final String safeTableAlias;
            safeTableAlias = this.tableToSafeAlias.get(field.tableMeta());
            if (safeTableAlias == null) {
                //self-join
                throw _Exceptions.selfJoinNonQualifiedField(field);
            }
            sqlBuilder.append(_Constant.SPACE);
            if (!(this.stmtContext instanceof _JoinableMergeContext)) {
                sqlBuilder.append(safeTableAlias)
                        .append(_Constant.DOT);
            }
            parser.safeObjectName(field, sqlBuilder);
        } else if (dataField instanceof QualifiedField) {
            final QualifiedField<?> field = (QualifiedField<?>) dataField;
            final String tableAlias = field.tableAlias();
            if (this.aliasToTable.get(tableAlias) != field.tableMeta()) {
                throw _Exceptions.unknownColumn(field);
            }
            final String safeTableAlias;
            safeTableAlias = this.getAliasToSafeAlias().get(tableAlias);
            assert safeTableAlias != null;
            sqlBuilder
                    .append(_Constant.SPACE)
                    .append(safeTableAlias)
                    .append(_Constant.DOT);
            parser.safeObjectName(field, sqlBuilder);
        } else {
            throw _Exceptions.immutableField(dataField);
        }

        if (updateTimePlaceholder != null) {
            this.appendedUpdateTime = true;
            if (dataField instanceof FieldMeta) {
                this.stmtContext.appendUpdateTimePlaceholder((FieldMeta<?>) dataField, updateTimePlaceholder);
            } else if (dataField instanceof QualifiedField) {
                this.stmtContext.appendUpdateTimePlaceholder(((QualifiedField<?>) dataField).fieldMeta(), updateTimePlaceholder);
            } else {
                // no bug,never here
                throw _Exceptions.illegalExpression(dataField);
            }
        }

    }

    @Override
    public boolean isAppendedUpdateTime() {
        return this.appendedUpdateTime;
    }

    @Override
    public void appendConditionFields() {
        final List<SqlField> conditionFieldList = this.conditionFieldList;
        if (conditionFieldList == null || conditionFieldList.isEmpty()) {
            return;
        }
         throw new UnsupportedOperationException();
    }


    List<SqlField> conditionFieldList() {
        List<SqlField> conditionFieldList = this.conditionFieldList;
        if (conditionFieldList == null) {
            conditionFieldList = Collections.emptyList();
        }
        return conditionFieldList;
    }


    Map<String, String> getAliasToSafeAlias() {
        Map<String, String> aliasToSafeAlias = this.aliasToSafeAlias;
        if (aliasToSafeAlias == null) {
            aliasToSafeAlias = _Collections.hashMap();
            this.aliasToSafeAlias = aliasToSafeAlias;
        }
        return aliasToSafeAlias;
    }

    void appendSafeField(final String tableAlias, final FieldMeta<?> field) {
        String safeTableAlias;
        safeTableAlias = this.tableToSafeAlias.get(field.tableMeta());
        if (safeTableAlias == null) {
            //  self-join
            safeTableAlias = getAliasToSafeAlias().computeIfAbsent(tableAlias, this.stmtContext.parser::identifier);
        }
        final StringBuilder sqlBuilder;
        sqlBuilder = this.stmtContext.sqlBuilder
                .append(_Constant.SPACE)
                .append(safeTableAlias)
                .append(_Constant.DOT);
        this.stmtContext.parser.safeObjectName(field, sqlBuilder);
    }


}
