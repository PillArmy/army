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

import static io.army.dialect.Database.MySQL;
import static io.army.dialect.Database.PostgreSQL;

/**
 * <p>
 * This interface representing json {@link Expression}.
 *
 * @since 0.6.0
 */
@Deprecated
public interface JsonExpression extends SimpleExpression,TypedExpression {


    @Support({MySQL, PostgreSQL})
    JsonExpression arrayElement(int index);

    @Support({MySQL, PostgreSQL})
    JsonExpression objectAttr(String keyName);

    @Support({MySQL, PostgreSQL})
    JsonExpression atPath(String jsonPath);

    @Support({MySQL, PostgreSQL})
    JsonExpression atPath(Expression jsonPath);

    /**
     * @param funcRef the reference of method,Note: it's the reference of method,not lambda. Valid method:
     *                <ul>
     *                    <li>{@link SQLs#param(TypeInfer, Object)}</li>
     *                    <li>{@link SQLs#literal(TypeInfer, Object)}</li>
     *                    <li>{@link SQLs#namedParam(TypeInfer, String)} ,used only in INSERT( or batch update/delete ) syntax</li>
     *                    <li>{@link SQLs#namedLiteral(TypeInfer, String)} ,used only in INSERT( or batch update/delete in multi-statement) syntax</li>
     *                    <li>developer custom method</li>
     *                </ul>.
     *                The first argument of funcRef always is {@link io.army.mapping.optional.JsonPathType#INSTANCE}.
     * @param value   non-null,it will be passed to funcRef as the second argument of funcRef
     */
    @Support({MySQL, PostgreSQL})
    <T> JsonExpression atPath(BiFunction<MappingType, T, Expression> funcRef, T value);

}
