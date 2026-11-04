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

import io.army.criteria.Expression;
import io.army.dialect._Constant;
import io.army.dialect._SqlContext;
import io.army.lang.Nullable;
import io.army.mapping.MappingType;
import io.army.util._Exceptions;

import java.util.List;

abstract class SqlFunctions {

    SqlFunctions() {
        throw new UnsupportedOperationException();
    }


    static void appendOneArg(final String funcName, final @Nullable Object arg, final StringBuilder sqlBuilder,
                          final _SqlContext context) {
        if (arg == null) {
            sqlBuilder.append(_Constant.SPACE_NULL);
        } else if (arg instanceof Expression) {
            ((ArmyExpression) arg).appendSql(sqlBuilder, context);
        } else {
            final MappingType type;
            type = _MappingFactory.getDefaultIfMatch(arg.getClass());
            if (type == null) {
                throw _Exceptions.funcNotFoundMappingType(funcName, arg);
            }
            Expressions.writeNonNull(type, arg, sqlBuilder, context);
        }

    }


    static void appendArgList(final String funcName, final List<?> argList, final StringBuilder sqlBuilder,
                              final _SqlContext context) {
        final int size = argList.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                sqlBuilder.append(_Constant.COMMA);
            }
            appendOneArg(funcName, argList.get(i), sqlBuilder, context);
        } // loop

    }

    static void argListToString(List<?> argList, StringBuilder builder) {
        final int size;
        size = argList.size();
        for (int i = 0; i < size; i++) {
            if (i > 0) {
                builder.append(_Constant.COMMA);
            }
            builder.append(argList.get(i));
        }
    }


}
