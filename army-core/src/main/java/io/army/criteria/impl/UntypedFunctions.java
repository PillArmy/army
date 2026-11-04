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

import io.army.criteria.SimpleExpression;
import io.army.dialect._Constant;
import io.army.dialect._SqlContext;
import io.army.lang.Nullable;

import java.util.List;


abstract class UntypedFunctions extends SqlFunctions {

    private UntypedFunctions() {
    }

    /*-------------------below expression function-------------------*/

    static SimpleExpression zeroArgFunc(String name) {
        return new ZeroArgFunc(name, true);
    }

    static SimpleExpression myZeroArgFunc(String name) {
        return new ZeroArgFunc(name, false);
    }

    static SimpleExpression oneArgFunc(String name, @Nullable Object arg) {
        return new OneArgFunc(name, true, arg);
    }

    static SimpleExpression myOneArgFunc(String name, @Nullable Object arg) {
        return new OneArgFunc(name, false, arg);
    }

    static SimpleExpression twoArgFunc(String name, @Nullable Object one, @Nullable Object two) {
        return new TwoArgFunc(name, true, one, two);
    }

    static SimpleExpression myTwoArgFunc(String name, @Nullable Object one, @Nullable Object two) {
        return new TwoArgFunc(name, false, one, two);
    }

    static SimpleExpression threeArgFunc(String name, @Nullable Object one, @Nullable Object two,
                                         @Nullable Object three) {
        return new ThreeArgFunc(name, true, one, two, three);
    }

    static SimpleExpression myThreeArgFunc(String name, @Nullable Object one, @Nullable Object two,
                                           @Nullable Object three) {
        return new ThreeArgFunc(name, false, one, two, three);
    }

    static SimpleExpression multiArgFunc(String name, List<?> argList) {
        return new MultiArgFunc(name, true, argList);
    }

    static SimpleExpression myMultiArgFunc(String name, List<?> argList) {
        return new MultiArgFunc(name, false, argList);
    }

    static SimpleExpression compositeFunc(String name, List<?> argList) {
        return new CompositeFunc(name, argList);
    }


    private static final class NoParensFunc extends OperationExpression.SqlFunctionExpression
            implements FunctionUtils.NoParensFunction {

        /// @see #noParensFunc(String)
        private NoParensFunc(String name) {
            super(name, true); //no parens function must be build-in,currently
        }

        @Override
        void appendArg(StringBuilder sqlBuilder, _SqlContext context) {
            // no-op
        }

        @Override
        void argToString(StringBuilder builder) {
            // no-op
        }


    } // NoParensFunc

    /// Create a function without parentheses,for example : {@code CURRENT_TIMESTAMP}
    ///
    /// @param name non-null,function name
    /// @return non-null
    static SimpleExpression noParensFunc(String name) {
        return new NoParensFunc(name);
    }

    private static final class ZeroArgFunc extends OperationExpression.SqlFunctionExpression
            implements FunctionUtils.NoArgFunction {

        private ZeroArgFunc(String name, boolean buildIn) {
            super(name, buildIn);
        }

        @Override
        void appendArg(StringBuilder sqlBuilder, _SqlContext context) {
            // no-op
        }

        @Override
        void argToString(StringBuilder builder) {
            // no-op
        }


    } // ZeroArgFunc

    private static final class OneArgFunc extends OperationExpression.SqlFunctionExpression {

        private final Object arg;

        private OneArgFunc(String name, boolean buildIn, @Nullable Object arg) {
            super(name, buildIn);
            this.arg = arg;
        }

        @Override
        void appendArg(StringBuilder sqlBuilder, _SqlContext context) {
            appendOneArg(this.name, this.arg, sqlBuilder, context);
        }

        @Override
        void argToString(StringBuilder builder) {
            builder.append(this.arg);
        }


    } // OneArgFunc

    private static final class TwoArgFunc extends OperationExpression.SqlFunctionExpression {

        private final Object one;

        private final Object two;

        private TwoArgFunc(String name, boolean buildIn, @Nullable Object one, @Nullable Object two) {
            super(name, buildIn);
            this.one = one;
            this.two = two;
        }

        @Override
        void appendArg(StringBuilder sqlBuilder, _SqlContext context) {
            appendOneArg(this.name, this.one, sqlBuilder, context);
            sqlBuilder.append(_Constant.COMMA);
            appendOneArg(this.name, this.two, sqlBuilder, context);
        }

        @Override
        void argToString(StringBuilder builder) {
            builder.append(this.one);
            builder.append(_Constant.COMMA);
            builder.append(this.two);
        }


    } // TwoArgFunc

    private static final class ThreeArgFunc extends OperationExpression.SqlFunctionExpression {

        private final Object one;

        private final Object two;

        private final Object three;

        private ThreeArgFunc(String name, boolean buildIn, @Nullable Object one, @Nullable Object two,
                             @Nullable Object three) {
            super(name, buildIn);
            this.one = one;
            this.two = two;
            this.three = three;
        }

        @Override
        void appendArg(StringBuilder sqlBuilder, _SqlContext context) {
            appendOneArg(this.name, this.one, sqlBuilder, context);
            sqlBuilder.append(_Constant.COMMA);
            appendOneArg(this.name, this.two, sqlBuilder, context);
            sqlBuilder.append(_Constant.COMMA);
            appendOneArg(this.name, this.three, sqlBuilder, context);
        }

        @Override
        void argToString(StringBuilder builder) {
            builder.append(this.one);
            builder.append(_Constant.COMMA);
            builder.append(this.two);
            builder.append(_Constant.COMMA);
            builder.append(this.three);
        }


    } // ThreeArgFunc

    private static final class MultiArgFunc extends OperationExpression.SqlFunctionExpression {

        private final List<?> argList;

        private MultiArgFunc(String name, boolean buildIn, List<?> argList) {
            super(name, buildIn);
            this.argList = argList;
        }

        @Override
        void appendArg(StringBuilder sqlBuilder, _SqlContext context) {
            appendArgList(this.name, this.argList, sqlBuilder, context);
        }

        @Override
        void argToString(StringBuilder builder) {
            argListToString(this.argList, builder);
        }


    } // MultiArgFunc

    private static final class CompositeFunc extends OperationExpression.SqlFunctionExpression {

        private final List<?> argList;

        private CompositeFunc(String name, List<?> argList) {
            super(name);
            this.argList = argList;
        }

        @Override
        void appendArg(StringBuilder sqlBuilder, _SqlContext context) {
            FuncExpUtils.appendCompositeList(this.name, this.argList, sqlBuilder, context);
        }

        @Override
        void argToString(StringBuilder builder) {
            FuncExpUtils.compositeListToString(this.argList, builder);
        }


    } // CompositeFunc




}
