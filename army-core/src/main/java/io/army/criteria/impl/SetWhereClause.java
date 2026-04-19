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

import io.army.criteria.*;
import io.army.criteria.impl.inner._DomainUpdate;
import io.army.criteria.impl.inner._ItemPair;
import io.army.criteria.impl.inner._Statement;
import io.army.dialect.Dialect;
import io.army.lang.Nullable;
import io.army.meta.ChildTableMeta;
import io.army.meta.TableMeta;
import io.army.util._Collections;
import io.army.util._Exceptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @see JoinableUpdate
 */

@SuppressWarnings("unchecked")
abstract class SetWhereClause<F extends TableField, SR, WR, WA, OR, OD, LR, LO, LF>
        extends WhereClause<WR, WA, OR, OD, LR, LO, LF>
        implements UpdateStatement._StaticBatchSetClause<F, SR>,
        UpdateStatement._StaticBatchRowSetClause<F, SR>,
        _Statement._ItemPairList,
        _Statement._TableMetaSpec {

    private List<_ItemPair> itemPairList = _Collections.arrayList();

    final TableMeta<?> updateTable;

    final String tableAlias;

    /**
     * @param tableAlias for {@link SingleUpdateStatement} non-null and  non-empty,for other non-null
     */
    SetWhereClause(CriteriaContext context, TableMeta<?> updateTable, String tableAlias) {
        super(context);
        ContextStack.assertNonNull(updateTable);
        ContextStack.assertNonNull(tableAlias);
        this.updateTable = updateTable;
        this.tableAlias = tableAlias;

    }


    @Override
    public final SR set(F field, @Nullable Object value) {
        return this.onAddItemPair(SQLs._itemPair(field, null, Expressions.wrapRight(field, value)));
    }

    @Override
    public final <E> SR set(final F field, final BiFunction<F, E, AssignmentItem> valueOperator,
                                                      final @Nullable E value) {
        return this.onAddAssignmentItemPair(field, valueOperator.apply(field, value));
    }

    @Override
    public final <E> SR set(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator,
                            BiFunction<F, E, Expression> valueOperator, E value) {
        return this.onAddAssignmentItemPair(field, fieldOperator.apply(field, valueOperator.apply(field, value)));
    }

    @Override
    public final SR ifSet(final F field, @Nullable Object value) {
        if (value != null) {
            this.onAddAssignmentItemPair(field, Expressions.wrapRight(field, value));
        }
        return (SR) this;
    }


    @Override
    public final <E> SR ifSet(F field, BiFunction<F, E, AssignmentItem> valueOperator, @Nullable E value) {
        if (value != null) {
            this.onAddAssignmentItemPair(field, valueOperator.apply(field, value));
        }
        return (SR) this;
    }

    @Override
    public final <E> SR ifSet(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator,
                              BiFunction<F, E, Expression> valueOperator, @Nullable E value) {
        if (value != null) {
            this.onAddAssignmentItemPair(field, fieldOperator.apply(field, valueOperator.apply(field, value)));
        }
        return (SR) this;
    }

    @Override
    public final SR setSpace(F field, BiFunction<F, String, Expression> valueOperator) {
        return this.onAddItemPair(SQLs._itemPair(field, null, valueOperator.apply(field, field.fieldName())));
    }

    @Override
    public final SR setSpace(F field, BiFunction<F, Expression, AssignmentItem> fieldOperator, BiFunction<F, String, Expression> valueOperator) {
        return this.onAddAssignmentItemPair(field, fieldOperator.apply(field, valueOperator.apply(field, field.fieldName())));
    }

    @Override
    public final SR setRow(F field1, F field2, SubQuery subQuery) {
        final List<F> fieldList;
        fieldList = List.of(field1, field2);
        return this.onAddItemPair(SQLs._itemPair(fieldList, subQuery));
    }

    @Override
    public final SR setRow(F field1, F field2, F field3, SubQuery subQuery) {
        final List<F> fieldList;
        fieldList = List.of(field1, field2, field3);
        return this.onAddItemPair(SQLs._itemPair(fieldList, subQuery));
    }

    @Override
    public final SR setRow(F field1, F field2, F field3, F field4, SubQuery subQuery) {
        final List<F> fieldList;
        fieldList = List.of(field1, field2, field3, field4);
        return this.onAddItemPair(SQLs._itemPair(fieldList, subQuery));
    }

    @Override
    public final SR setRow(List<F> fieldList, SubQuery subQuery) {
        return this.onAddItemPair(SQLs._itemPair(List.copyOf(fieldList), subQuery));
    }

    @Override
    public final SR setRow(Consumer<Consumer<F>> consumer, SubQuery subQuery) {
        final List<F> fieldList = _Collections.arrayList();
        ClauseUtils.invokeConsumer(fieldList::add, consumer);
        return this.onAddItemPair(SQLs._itemPair(fieldList, subQuery));
    }

    @Override
    public final SR ifSetRow(F field1, F field2, Supplier<SubQuery> supplier) {
        final SubQuery query;
        if ((query = supplier.get()) != null) {
            final List<F> fieldList;
            fieldList = List.of(field1, field2);
            this.onAddItemPair(SQLs._itemPair(fieldList, query));
        }
        return (SR) this;
    }

    @Override
    public final SR ifSetRow(F field1, F field2, F field3, Supplier<SubQuery> supplier) {
        final SubQuery query;
        if ((query = supplier.get()) != null) {
            final List<F> fieldList;
            fieldList = Arrays.asList(field1, field2, field3);
            this.onAddItemPair(SQLs._itemPair(fieldList, query));
        }
        return (SR) this;
    }

    @Override
    public final SR ifSetRow(F field1, F field2, F field3, F field4, Supplier<SubQuery> supplier) {
        final SubQuery query;
        if ((query = supplier.get()) != null) {
            final List<F> fieldList;
            fieldList = Arrays.asList(field1, field2, field3, field4);
            this.onAddItemPair(SQLs._itemPair(fieldList, query));
        }
        return (SR) this;
    }


    @Override
    public final SR ifSetRow(List<F> fieldList, Supplier<SubQuery> supplier) {
        final SubQuery query;
        if (!fieldList.isEmpty() && (query = supplier.get()) != null) {
            this.onAddItemPair(SQLs._itemPair(List.copyOf(fieldList), query));
        }
        return (SR) this;
    }

    @Override
    public final SR ifSetRow(Consumer<Consumer<F>> consumer, Supplier<SubQuery> supplier) {
        final List<F> fieldList = _Collections.arrayList();
        consumer.accept(fieldList::add);
        final SubQuery query;
        if (!fieldList.isEmpty() && (query = supplier.get()) != null) {
            this.onAddItemPair(SQLs._itemPair(fieldList, query));
        }
        return (SR) this;
    }

    @Override
    public final TableMeta<?> table() {
        return this.updateTable;
    }

    @Override
    public final List<_ItemPair> itemPairList() {
        final List<_ItemPair> itemPairList = this.itemPairList;
        if (itemPairList == null || itemPairList instanceof ArrayList) {
            throw ContextStack.clearStackAndCastCriteriaApi();
        }
        return itemPairList;
    }

    void onAddChildItemPair(SQLs.ArmyItemPair pair) {
        throw new UnsupportedOperationException();
    }

    boolean isNoChildItemPair() {
        throw new UnsupportedOperationException();
    }

    final List<_ItemPair> endUpdateSetClause() {
        List<_ItemPair> itemPairList = this.itemPairList;
        if (itemPairList == null || itemPairList.size() == 0) {
            if (!(this instanceof _DomainUpdate) || this.isNoChildItemPair()) {
                throw ContextStack.criteriaError(this.context, _Exceptions::setClauseNotExists);
            }
            itemPairList = Collections.emptyList();
            this.itemPairList = itemPairList;
        } else if (itemPairList instanceof ArrayList) {
            itemPairList = _Collections.unmodifiableList(itemPairList);
            this.itemPairList = itemPairList;
        } else {
            throw ContextStack.clearStackAndCastCriteriaApi();
        }
        return itemPairList;

    }


    final SR onAddItemPair(final ItemPair pair) {
        final List<_ItemPair> itemPairList = this.itemPairList;
        if (!(itemPairList instanceof ArrayList)) {
            throw ContextStack.clearStackAndCastCriteriaApi();
        }

        final SQLs.FieldItemPair fieldPair;
        final TableField field;
        if (pair instanceof SQLs.RowItemPair) {
            assert !(this instanceof _DomainUpdate);
            itemPairList.add((SQLs.RowItemPair) pair);
        } else if (!(pair instanceof SQLs.FieldItemPair)) {
            throw ContextStack.criteriaError(this.context, String.format("unknown %s", ItemPair.class.getName()));
        } else if (!((fieldPair = (SQLs.FieldItemPair) pair).field instanceof TableField)) {
            throw ContextStack.clearStackAndCastCriteriaApi();
        } else if (!(field = (TableField) fieldPair.field).updatable()) {
            throw ContextStack.criteriaError(this.context, _Exceptions::immutableField, field);
        } else if (field.notNull() && ((ArmyExpression) fieldPair.right).isNullValue()) {
            throw ContextStack.criteriaError(this.context, _Exceptions::nonNullField, field);
        } else if (!(this instanceof _DomainUpdate)) {
            if (field instanceof QualifiedField
                    && !this.tableAlias.equals(((QualifiedField<?>) field).tableAlias())) {
                throw ContextStack.criteriaError(this.context, _Exceptions::unknownColumn, field);
            }
            itemPairList.add(fieldPair);
        } else if (!this.updateTable.isComplexField(field.fieldMeta())) {
            throw ContextStack.criteriaError(this.context, _Exceptions::unknownColumn, field);
        } else if (field.tableMeta() instanceof ChildTableMeta) {
            this.onAddChildItemPair(fieldPair);
        } else {
            itemPairList.add(fieldPair);
        }
        return (SR) this;
    }

    private SR onAddAssignmentItemPair(final F field, final @Nullable AssignmentItem item) {
        final ItemPair pair;
        if (item == null) {
            throw ContextStack.nullPointer(this.context);
        } else if (item instanceof Expression) {
            pair = SQLs._itemPair(field, null, (Expression) item);
        } else if (item instanceof ItemPair) {
            pair = (ItemPair) item;
        } else {
            throw CriteriaUtils.illegalAssignmentItem(this.context, item);
        }
        return this.onAddItemPair(pair);
    }

    static abstract class SetWhereClauseClause<F extends TableField, SR, WR, WA>
            extends SetWhereClause<F, SR, WR, WA, Object, Object, Object, Object, Object> {

        SetWhereClauseClause(CriteriaContext context, TableMeta<?> updateTable, String tableAlias) {
            super(context, updateTable, tableAlias);
        }

        @Override
        final Dialect statementDialect() {
            throw ContextStack.clearStackAndCastCriteriaApi();
        }

    }//SetWhereClauseClause


}
