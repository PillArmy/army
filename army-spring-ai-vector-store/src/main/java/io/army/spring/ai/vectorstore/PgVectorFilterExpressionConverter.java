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

package io.army.spring.ai.vectorstore;

import io.army.dialect._Constant;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.converter.AbstractFilterExpressionConverter;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;


/// @see <a href="https://www.postgresql.org/docs/current/datatype-json.html#DATATYPE-JSONPATH">PostgreSQL jsonpath</a>
/// @see <a href="https://www.postgresql.org/docs/current/functions-json.html#FUNCTIONS-SQLJSON-PATH">PostgreSQL JSON Path Language</a>
/// @see <a href="https://dev.mysql.com/doc/refman/9.7/en/json.html#json-path-syntax">MySQL jsonpath</a>
final class PgVectorFilterExpressionConverter extends AbstractFilterExpressionConverter {


    PgVectorFilterExpressionConverter() {
    }

    @Override
    protected void doExpression(Filter.Expression expression, StringBuilder context) {
        final Filter.ExpressionType type = expression.type();
        Assert.state(type != null, "expression should have a right operand");
        switch (type) {
            case IN:
                handleIn(expression, context);
                break;
            case NIN:
                handleIn(expression, context.append('!'));
                break;
            default: {
                this.convertOperand(expression.left(), context);
                context.append(getOperationSymbol(expression));
                this.convertOperand(expression.right(), context);
            }
        }

    }

    @Override
    protected void doKey(Filter.Key filterKey, StringBuilder context) {
        context.append('$')
                .append('.')
                .append(filterKey.key());
    }

    @Override
    protected void doSingleValue(Object value, StringBuilder context) {
        if (value instanceof Date date) {
            emitJsonValue(ISO_DATE_FORMATTER.format(date.toInstant()), context);
        } else {
            emitJsonValue(value, context);
        }
    }


    @Override
    protected void doStartGroup(Filter.Group group, StringBuilder context) {
        context.append('(');
    }

    @Override
    protected void doEndGroup(Filter.Group group, StringBuilder context) {
        context.append(')');
    }


    private String getOperationSymbol(Filter.Expression exp) {
        return switch (exp.type()) {
            case AND -> " && ";
            case OR -> " || ";
            case EQ -> " == ";
            case NE -> " != ";
            case LT -> " < ";
            case LTE -> " <= ";
            case GT -> " > ";
            case GTE -> " >= ";
            default -> throw new RuntimeException("Not supported expression type: " + exp.type());
        };
    }

    private void handleIn(final Filter.Expression expression, final StringBuilder context) {
        final Filter.Value right = (Filter.Value) expression.right();
        Assert.state(right != null, "expression should have a right operand");

        context.append('(');

        final Object value = right.value();
        if (!(value instanceof List<?> valueList)) {
            String m = String.format("Expected a List, but got: %s", value.getClass().getSimpleName());
            throw new IllegalArgumentException(m);
        }
        final int listSize = valueList.size();

        String left = null;
        for (int i = 0; i < listSize; i++) {
            if (i > 0) {
                context.append(_Constant.SPACE)
                        .append("||")
                        .append(_Constant.SPACE);
            }

            if (left == null) {
                final int pos = context.length();
                this.convertOperand(expression.left(), context);
                left = context.substring(pos);
            } else {
                context.append(left);
            }

            context.append(_Constant.SPACE)
                    .append("==")
                    .append(_Constant.SPACE);

            this.doSingleValue(normalizeDateString(valueList.get(i)), context);

        } // loop

        context.append(')');
    }


}
