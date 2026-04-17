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
import io.army.criteria.impl.inner._Selection;
import io.army.criteria.impl.inner._SelfDescribed;
import io.army.dialect._Constant;
import io.army.dialect._SqlContext;
import io.army.lang.Nullable;
import io.army.mapping.MappingType;
import io.army.meta.FieldMeta;
import io.army.meta.TypeMeta;
import io.army.util._StringUtils;

abstract class ArmySelections implements _Selection {

    static _Selection forExp(final Expression exp, final String alias) {
        final _Selection selection;
        if (exp instanceof NamedExpression && ((NamedExpression) exp).label().equals(alias)) {
            selection = (_Selection) exp;
        } else if (exp instanceof FieldSelection) {
            selection = new RenameFieldSelection((FieldSelection) exp, alias);
        } else if (exp instanceof TypedSelection) {
            selection = new RenameTypedSelection((TypedSelection) exp, alias);
        } else {
            selection = new ExpressionSelection((ArmyExpression) exp, alias);
        }
        return selection;
    }

    static Selection renameSelection(final Selection selection, final String alias) {
        final Selection s;
        if (selection instanceof AnonymousSelection) {
            s = new RenameSelection(selection, alias);
        } else if (selection instanceof FieldSelection) {
            if (selection.label().equals(alias)) {
                s = selection;
            } else {
                s = new RenameFieldSelection((FieldSelection) selection, alias);
            }
        } else if (selection instanceof TypedSelection) {
            if (selection.label().equals(alias)) {
                s = selection;
            } else {
                s = new RenameTypedSelection((TypedSelection) selection, alias);
            }
        } else if (selection.label().equals(alias)) {
            s = selection;
        } else {
            s = new RenameSelection(selection, alias);
        }
        return s;
    }


    static Selection forName(final @Nullable String alias) {
        if (alias == null) {
            throw ContextStack.clearStackAndNullPointer();
        }
        return new SelectionForName(alias);
    }

    static Selection forName(final @Nullable String alias, MappingType type) {
        if (alias == null) {
            throw ContextStack.clearStackAndNullPointer();
        }
        return new TypedSelectionForName(alias, type);
    }

    static Selection forAnonymous() {
        return AnonymousSelectionImpl.INSTANCE;
    }

    static Selection forColumnFunc(Functions._ColumnFunction func, String alias) {
        return new ColumnFuncSelection(func, alias);
    }


    final String alias;

    private ArmySelections(String alias) {
        this.alias = alias;
    }


    @Override
    public final String label() {
        return this.alias;
    }

    static final class ExpressionSelection extends ArmySelections {

        final ArmyExpression expression;

        private ExpressionSelection(ArmyExpression expression, String alias) {
            super(alias);
            this.expression = expression;
        }


        @Override
        public void appendSelectItem(final StringBuilder sqlBuilder, final _SqlContext context) {
            this.expression.appendSql(sqlBuilder, context);

            sqlBuilder.append(_Constant.SPACE_AS_SPACE);

            context.identifier(this.alias, sqlBuilder);
        }

        @Nullable
        @Override
        public TableField tableField() {
            //always null
            return null;
        }

        @Override
        public Expression underlyingExp() {
            return this.expression;
        }

        @Override
        public String toString() {
            return String.format(" %s AS %s", this.expression, this.alias);
        }


    }//ExpressionSelection


    static final class RenameTypedSelection extends ArmySelections implements TypedSelection {


        private final TypedSelection selection;

        private RenameTypedSelection(TypedSelection selection, String alias) {
            super(alias);
            this.selection = selection;
        }

        @Override
        public TypeMeta typeMeta() {
            return this.selection.typeMeta();
        }

        @Nullable
        @Override
        public TableField tableField() {
            return null;
        }

        @Nullable
        @Override
        public Expression underlyingExp() {
            if (this.selection instanceof Expression) {
                return (Expression) this.selection;
            }
            return null;
        }

        @Override
        public void appendSelectItem(StringBuilder sqlBuilder, _SqlContext context) {
            ((_SelfDescribed) this.selection).appendSql(sqlBuilder, context);
            sqlBuilder.append(_Constant.SPACE_AS_SPACE);
            context.identifier(this.alias, sqlBuilder);
        }

        @Override
        public String toString() {
            return _StringUtils.builder()
                    .append(this.selection)
                    .append(_Constant.SPACE_AS_SPACE)
                    .append(this.alias)
                    .toString();
        }


    } // RenameTypedSelection


    static final class RenameFieldSelection extends ArmySelections implements FieldSelection, _SelfDescribed {

        final FieldSelection selection;

        private RenameFieldSelection(FieldSelection selection, String alias) {
            super(alias);
            this.selection = selection;
        }


        @Override
        public TypeMeta typeMeta() {
            return this.selection.typeMeta();
        }

        @Override
        public FieldMeta<?> fieldMeta() {
            final FieldSelection field = this.selection;
            final FieldMeta<?> fieldMeta;
            if (field instanceof FieldMeta) {
                fieldMeta = (FieldMeta<?>) field;
            } else {
                fieldMeta = field.fieldMeta();
            }
            return fieldMeta;
        }


        @Override
        public void appendSelectItem(final StringBuilder sqlBuilder, final _SqlContext context) {
            this.appendSql(sqlBuilder, context);

            sqlBuilder.append(_Constant.SPACE_AS_SPACE);

            context.identifier(this.alias, sqlBuilder);
        }

        @Override
        public void appendSql(final StringBuilder sqlBuilder, final _SqlContext context) {

            final FieldSelection field = this.selection;
            if (field instanceof FieldMeta) {
                //here couldn't invoke appendSql() of this.field,avoid  visible field.
                context.appendField((FieldMeta<?>) this.selection);
            } else if (field instanceof QualifiedField) {
                //here couldn't invoke appendSql() of this.field,avoid  visible field.
                final QualifiedFieldImpl<?> qualifiedField = (QualifiedFieldImpl<?>) this.selection;
                context.appendField(qualifiedField.tableAlias, qualifiedField.field);
            } else {
                ((_SelfDescribed) field).appendSql(sqlBuilder, context);
            }
        }

        @Nullable
        @Override
        public Expression underlyingExp() {
            final FieldSelection selection = this.selection;
            final Expression exp;
            if (selection instanceof TableField) {
                exp = (TableField) selection;
            } else {
                exp = ((_Selection) selection).underlyingExp();
            }
            return exp;
        }

        @Nullable
        @Override
        public TableField tableField() {
            final FieldSelection selection = this.selection;
            final TableField field;
            if (selection instanceof TableField) {
                field = (TableField) selection;
            } else {
                field = ((_Selection) selection).tableField();
            }
            return field;
        }

        @Override
        public String toString() {
            return _StringUtils.builder()
                    .append(this.selection)
                    .append(_Constant.SPACE_AS_SPACE)
                    .append(this.alias)
                    .toString();
        }

    }//FieldSelectionImpl


    static final class RenameSelection extends ArmySelections {

        final Selection selection;

        private RenameSelection(Selection selection, String alias) {
            super(alias);
            this.selection = selection;
        }


        @Override
        public void appendSelectItem(final StringBuilder sqlBuilder, final _SqlContext context) {
            //no bug,never here
            throw new CriteriaException(String.format("%s couldn't be rendered.", RenameSelection.class.getName()));
        }

        @Override
        public TableField tableField() {
            final Selection selection = this.selection;
            final TableField field;
            if (selection instanceof _Selection) {
                field = ((_Selection) selection).tableField();
            } else {
                // probably UndoneColumnFunc
                field = null;
            }
            return field;
        }

        @Override
        public Expression underlyingExp() {
            final Selection selection = this.selection;
            final Expression expression;
            if (selection instanceof _Selection) {
                expression = ((_Selection) selection).underlyingExp();
            } else {
                // probably UndoneColumnFunc
                expression = null;
            }
            return expression;
        }


    }//RenameSelection


    private static final class TypedSelectionForName extends ArmySelections implements TypedSelection {

        private final MappingType type;

        private TypedSelectionForName(String alias, MappingType type) {
            super(alias);
            this.type = type;
        }

        @Override
        public TypeMeta typeMeta() {
            return this.type;
        }

        @Nullable
        @Override
        public TableField tableField() {
            return null;
        }

        @Nullable
        @Override
        public Expression underlyingExp() {
            return null;
        }

        @Override
        public void appendSelectItem(StringBuilder sqlBuilder, _SqlContext context) {
            //no-bug ,never here
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return _StringUtils.builder()
                    .append(_Constant.SPACE)
                    .append(this.alias)
                    .append(_Constant.SPACE_AS_SPACE)
                    .append(this.alias)
                    .toString();
        }


    } // TypedSelectionForName

    private static final class SelectionForName extends ArmySelections {


        private SelectionForName(String alias) {
            super(alias);
        }


        @Override
        public void appendSelectItem(final StringBuilder sqlBuilder, final _SqlContext context) {
            //no-bug ,never here
            throw new UnsupportedOperationException();

        }


        @Override
        public TableField tableField() {
            return null; // TODO
        }

        @Override
        public Expression underlyingExp() {
            return null; // TODO
        }

        @Override
        public String toString() {
            return _StringUtils.builder()
                    .append(_Constant.SPACE)
                    .append(this.alias)
                    .append(_Constant.SPACE_AS_SPACE)
                    .append(this.alias)
                    .toString();
        }


    }//SelectionForName


    private static final class ColumnFuncSelection extends ArmySelections {

        private final Functions._ColumnFunction func;

        private ColumnFuncSelection(Functions._ColumnFunction func, String alias) {
            super(alias);
            this.func = func;

        }

        @Override
        public void appendSelectItem(final StringBuilder sqlBuilder, final _SqlContext context) {

            ((_SelfDescribed) this.func).appendSql(sqlBuilder, context);

            sqlBuilder.append(_Constant.SPACE_AS_SPACE);

            context.identifier(this.alias, sqlBuilder);
        }


        @Override
        public TableField tableField() {
            //always null
            return null; // TODO
        }

        @Override
        public Expression underlyingExp() {
            //always null
            return null; // TODO
        }

        @Override
        public String toString() {
            return _StringUtils.builder()
                    .append(this.func)
                    .append(_Constant.SPACE_AS_SPACE)
                    .append(this.alias)
                    .toString();
        }


    }//ColumnFuncSelection


    private static final class AnonymousSelectionImpl implements AnonymousSelection {

        private static final AnonymousSelectionImpl INSTANCE = new AnonymousSelectionImpl();


        private AnonymousSelectionImpl() {
        }


        @Override
        public String label() {
            // no bug,never here
            throw new UnsupportedOperationException();
        }


    }//AnonymousSelectionImpl


}
