/*
 * Copyright 2023-2043 the original author or authors.
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

package io.army.criteria;

import io.army.criteria.impl.SQLs;
import io.army.mapping.MappingType;

import java.util.function.BiFunction;

import static io.army.dialect.Database.H2;
import static io.army.dialect.Database.PostgreSQL;

/**
 * <p>
 * This interface representing array {@link Expression}.
 *
 * @see <a href="https://www.postgresql.org/docs/current/arrays.html">Arrays</a>
 * @since 0.6.0
 */
@Deprecated
public interface ArrayExpression extends SimpleExpression, TypedExpression {


    @Support({H2, PostgreSQL})
    SimpleExpression atElement(int index);

    SimpleExpression atElement(int index1, int index2);

    SimpleExpression atElement(int index1, int index2, int index3, int... restIndex);

    SimpleExpression atElement(Expression index);

    SimpleExpression atElement(Expression index1, Expression index2);

    SimpleExpression atElement(Expression index1, Expression index2, Expression index3, Expression... restIndex);

    @Support({H2, PostgreSQL})
    ArrayExpression atArray(int index);

    @Support({PostgreSQL})
    ArrayExpression atArray(int index1, int index2);

    @Support({PostgreSQL})
    ArrayExpression atArray(int index1, int index2, int index3, int... restIndex);

    @Support({PostgreSQL})
    ArrayExpression atArray(ArraySubscript index);

    @Support({PostgreSQL})
    ArrayExpression atArray(ArraySubscript index1, ArraySubscript index2);

    @Support({PostgreSQL})
    ArrayExpression atArray(ArraySubscript index1, ArraySubscript index2, ArraySubscript index3, ArraySubscript... restIndex);

    @Support({PostgreSQL})
    <T> ArrayExpression atArray(BiFunction<MappingType, T, Expression> funcRef, T value);

    @Support({PostgreSQL})
    <T> ArrayExpression atArray(BiFunction<MappingType, T, Expression> funcRef, T value1, T value2);

    @Support({PostgreSQL})
    <T> ArrayExpression atArray(BiFunction<MappingType, T, Expression> funcRef, T value1, T value2, T value3);

    @Support({PostgreSQL})
    <T, U> ArrayExpression atArray(BiFunction<MappingType, T, Expression> funcRef1, T value1, BiFunction<MappingType, U, Expression> funcRef2, U value2);

    @Support({PostgreSQL})
    <T, U, V> ArrayExpression atArray(BiFunction<MappingType, T, Expression> funcRef1, T value1, BiFunction<MappingType, U, Expression> funcRef2, U value2, BiFunction<MappingType, V, Expression> funcRef3, V value3);


}
