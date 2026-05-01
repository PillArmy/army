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
import io.army.dialect.Database;
import io.army.dialect._Constant;
import io.army.dialect._SqlContext;
import io.army.util._Exceptions;

import java.util.List;

/// 
/// This class hold the methods that create {@link io.army.criteria.RowExpression}.
/// 
/// Below is chines signature:
/// 当你在阅读这段代码时,我才真正在写这段代码,你阅读到哪里,我便写到哪里.
/// @since 0.6.0
abstract class RowExpressions {

    private RowExpressions() {
        throw new UnsupportedOperationException();
    }


    static RowExpression row(final List<?> columnList) {
        return new RowConstructor(columnList);
    }


    static void validateColumnSize(final SQLColumnList left, final SQLColumnList right) {
        if (right instanceof SubQuery) {
            Expressions.validateSubQueryContext((SubQuery) right);
        }
        doValidateColumnSize(left, right);
    }

    /*-------------------below private method-------------------*/

    /// @return negative : unknown
    private static int rowElementColumnSize(final Object element) {
        final int columnSize;
        if (!(element instanceof RowElement)) {
            columnSize = 1;
        } else if (element instanceof RowExpression) {
            columnSize = ((RowExpression) element).columnSize();
        } else if (element instanceof Expression) {
            columnSize = 1;
        } else if (element instanceof SubQuery) {
            columnSize = ((ArmySubQuery) element).refAllSelection().size();
        } else if (element instanceof SelectionGroups.RowElementGroup) {
            columnSize = ((SelectionGroups.RowElementGroup) element).selectionList().size();
        } else {
            throw ContextStack.clearStackAnd(_Exceptions::unknownRowElement, element);
        }
        return columnSize;
    }


    private static void doValidateColumnSize(final SQLColumnList left, final SQLColumnList right) {
        final int leftSize, rightSize;

        if (left instanceof RowExpression) {
            leftSize = ((RowExpression) left).columnSize();
        } else if (left instanceof SubQuery) {
            leftSize = ((ArmySubQuery) left).refAllSelection().size();
        } else {
            return;
        }

        if (right instanceof RowExpression) {
            rightSize = ((RowExpression) right).columnSize();
        } else if (right instanceof SubQuery) {
            rightSize = ((ArmySubQuery) right).refAllSelection().size();
        } else {
            return;
        }

        if (leftSize > -1 && rightSize > -1 && leftSize != rightSize) {
            String m;
            m = String.format("left operand %s column size[%s] and right operand %s column size[%s] not match",
                    left, leftSize, right, rightSize);
            throw ContextStack.clearStackAndCriteriaError(m);
        }
    }

    private static final class RowConstructor extends OperationRowExpression implements LengthTypedRowExpression {

        private final List<?> elementList;

        private int actualColumnCount = -2;

        private RowConstructor(final List<?> elementList) {
            this.elementList = List.copyOf(elementList);
        }


        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {

            final List<?> elementList = this.elementList;
            final int elementItemSize;
            elementItemSize = elementList.size();
            final Database database;
            database = context.dialectDatabase();
            if (elementItemSize == 0 && database == Database.MySQL) {
                String m = String.format("error ,%s don't support empty row", database);
                throw new CriteriaException(m);
            }


            Object element;

            sqlBuilder.append(_Constant.SPACE);
            if (database != Database.MySQL || columnSize() > 1) {
                // MySQL don't support one column row
                sqlBuilder.append("ROW");
            }

            sqlBuilder.append(_Constant.LEFT_PAREN);
            for (int i = 0; i < elementItemSize; i++) {
                if (i > 0) {
                    sqlBuilder.append(_Constant.SPACE_COMMA);
                }
                element = elementList.get(i);
                if (element == null) {
                    sqlBuilder.append(_Constant.SPACE_NULL);
                } else if (!(element instanceof RowElement)) {
                    Expressions.writeNonNull(element, sqlBuilder, context);
                } else if (element instanceof Expression) {
                    ((ArmyExpression) element).appendSql(sqlBuilder, context);
                } else if (element instanceof SubQuery) {
                    context.appendSubQuery((SubQuery) element);
                } else if (element instanceof SelectionGroups.RowElementGroup) {
                    ((SelectionGroups.RowElementGroup) element).appendRowElement(sqlBuilder, context);
                } else {
                    throw _Exceptions.unknownRowElement(element);
                }

            } // for loop

            sqlBuilder.append(_Constant.RIGHT_PAREN);

        }


        @Override
        public int columnSize() {
            int columnCount = this.actualColumnCount;
            if (columnCount > -2) {
                return columnCount;
            }
            columnCount = 0;
            int count;
            for (Object e : this.elementList) {
                count = rowElementColumnSize(e);
                if (count < 0) {
                    columnCount = -1;
                    break;
                }
                columnCount += count;
            }
            this.actualColumnCount = columnCount;
            return columnCount;
        }

        @Override
        public String toString() {
            final List<?> elementList = this.elementList;
            final int elementItemSize;
            elementItemSize = elementList.size();
            final StringBuilder builder = new StringBuilder();

            Object element;
            builder.append(_Constant.SPACE)
                    .append("ROW")
                    .append(_Constant.LEFT_PAREN);
            for (int i = 0; i < elementItemSize; i++) {
                if (i > 0) {
                    builder.append(_Constant.SPACE_COMMA);
                }
                element = elementList.get(i);
                if (element == null) {
                    builder.append(_Constant.SPACE_NULL);
                } else {
                    if (!(element instanceof RowElement)) {
                        builder.append(_Constant.SPACE);
                    }
                    builder.append(element);
                }

            }//for
            return builder.append(_Constant.RIGHT_PAREN)
                    .toString();
        }


    } //RowConstructor


}
