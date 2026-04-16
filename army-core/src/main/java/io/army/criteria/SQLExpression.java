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

package io.army.criteria;

/**
 * <p>
 * This interface is base interface of below:
 * <ul>
 *     <li>{@link Expression}</li>
 *     <li>{@link RowExpression}</li>
 * </ul>
 *
 * @since 0.6.0
 */
public interface SQLExpression extends RowElement, RightOperand {


    /**
     * <p>
     * <strong>= ANY</strong> operator
     *
     */
    IPredicate equalAny(SubQuery subQuery);

    /**
     * <p>
     * <strong>= SOME</strong> operator
     *
     */
    IPredicate equalSome(SubQuery subQuery);

    IPredicate equalAll(SubQuery subQuery);

    IPredicate notEqualAny(SubQuery subQuery);

    IPredicate notEqualSome(SubQuery subQuery);

    IPredicate notEqualAll(SubQuery subQuery);

    IPredicate lessAny(SubQuery subQuery);

    IPredicate lessSome(SubQuery subQuery);

    IPredicate lessAll(SubQuery subQuery);

    IPredicate lessEqualAny(SubQuery subQuery);

    IPredicate lessEqualSome(SubQuery subQuery);

    IPredicate lessEqualAll(SubQuery subQuery);

    IPredicate greaterAny(SubQuery subQuery);

    IPredicate greaterSome(SubQuery subQuery);

    IPredicate greaterAll(SubQuery subQuery);

    IPredicate greaterEqualAny(SubQuery subQuery);

    IPredicate greaterEqualSome(SubQuery subQuery);

    IPredicate greaterEqualAll(SubQuery subQuery);


    IPredicate in(SQLColumnSet row);

    IPredicate notIn(SQLColumnSet row);


}
