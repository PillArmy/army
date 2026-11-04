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

import io.army.criteria.SimplePredicate;
import io.army.criteria.TypedExpression;
import io.army.dialect._Constant;
import io.army.dialect._SqlContext;
import io.army.mapping.MappingType;

import java.util.List;

abstract class TypedFunctions extends SqlFunctions {

    private TypedFunctions() {
    }

    static TypedExpression zeroArgFunc(String name, MappingType returnType) {
        return new ZeroArgFunc(name, true, returnType);
    }

    static TypedExpression myZeroArgFunc(String name, MappingType returnType) {
        return new ZeroArgFunc(name, false, returnType);
    }

    static TypedExpression oneArgFunc(String name, MappingType returnType, Object arg) {
        return new OneArgFunc(name, returnType, arg);
    }

    static TypedExpression myOneArgFunc(String name, MappingType returnType, Object arg) {
        return new OneArgFunc(name, false, returnType, arg);
    }

    static TypedExpression twoArgFunc(String name, MappingType returnType, Object arg1, Object arg2) {
        return new TwoArgFunc(name, returnType, arg1, arg2);
    }

    static TypedExpression myTwoArgFunc(String name, MappingType returnType, Object arg1, Object arg2) {
        return new TwoArgFunc(name, false, returnType, arg1, arg2);
    }

    static TypedExpression threeArgFunc(String name, MappingType returnType, Object arg1, Object arg2, Object arg3) {
        return new ThreeArgFunc(name, returnType, arg1, arg2, arg3);
    }

    static TypedExpression myThreeArgFunc(String name, MappingType returnType, Object arg1, Object arg2, Object arg3) {
        return new ThreeArgFunc(name, false, returnType, arg1, arg2, arg3);
    }

    static TypedExpression multiArgFunc(String name, MappingType returnType, List<?> argList) {
        return new MultiArgFunc(name, returnType, argList);
    }

    static TypedExpression myMultiArgFunc(String name, MappingType returnType, List<?> argList) {
        return new MultiArgFunc(name, false, returnType, argList);
    }

    /// Create a function without parentheses,for example : {@code CURRENT_TIMESTAMP}
    ///
    /// @param name non-null,function name
    /// @return non-null
    static TypedExpression noParensFunc(String name, MappingType returnType) {
        return new NoParensFunc(name, returnType);
    }

    static SimplePredicate zeroArgPredicate(String name) {
        return new ZeroArgPredicate(name, true);
    }

    static SimplePredicate myZeroArgPredicate(String name) {
        return new ZeroArgPredicate(name, false);
    }

    static SimplePredicate oneArgPredicate(String name, Object arg) {
        return new OneArgPredicate(name, true, arg);
    }

    static SimplePredicate myOneArgPredicate(String name, Object arg) {
        return new OneArgPredicate(name, false, arg);
    }

    static SimplePredicate twoArgPredicate(String name, Object arg1, Object arg2) {
        return new TwoArgPredicate(name, true, arg1, arg2);
    }

    static SimplePredicate myTwoArgPredicate(String name, Object arg1, Object arg2) {
        return new TwoArgPredicate(name, false, arg1, arg2);
    }

    static SimplePredicate threeArgPredicate(String name, Object arg1, Object arg2, Object arg3) {
        return new ThreeArgPredicate(name, true, arg1, arg2, arg3);
    }

    static SimplePredicate myThreeArgPredicate(String name, Object arg1, Object arg2, Object arg3) {
        return new ThreeArgPredicate(name, false, arg1, arg2, arg3);
    }

    static SimplePredicate multiArgPredicate(String name, List<?> argList) {
        return new MultiArgPredicate(name, true, argList);
    }

    static SimplePredicate myMultiArgPredicate(String name, List<?> argList) {
        return new MultiArgPredicate(name, false, argList);
    }

    private static final class ZeroArgFunc extends OperationExpression.TypedFunctionExpression
            implements FunctionUtils.NoArgFunction {

        private ZeroArgFunc(String name, boolean buildIn, MappingType returnType) {
            super(name, buildIn, returnType);
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

    private static final class OneArgFunc extends OperationExpression.TypedFunctionExpression {

        private final Object arg;

        private OneArgFunc(String name, boolean buildIn, MappingType returnType, Object arg) {
            super(name, buildIn, returnType);
            this.arg = arg;
        }

        private OneArgFunc(String name, MappingType returnType, Object arg) {
            super(name, true, returnType);
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

    private static final class TwoArgFunc extends OperationExpression.TypedFunctionExpression {

        private final Object arg1;

        private final Object arg2;

        private TwoArgFunc(String name, boolean buildIn, MappingType returnType, Object arg1, Object arg2) {
            super(name, buildIn, returnType);
            this.arg1 = arg1;
            this.arg2 = arg2;
        }

        private TwoArgFunc(String name, MappingType returnType, Object arg1, Object arg2) {
            super(name, true, returnType);
            this.arg1 = arg1;
            this.arg2 = arg2;
        }

        @Override
        void appendArg(StringBuilder sqlBuilder, _SqlContext context) {
            appendOneArg(this.name, this.arg1, sqlBuilder, context);
            sqlBuilder.append(_Constant.COMMA);
            appendOneArg(this.name, this.arg2, sqlBuilder, context);
        }

        @Override
        void argToString(StringBuilder builder) {
            builder.append(this.arg1);
            builder.append(_Constant.COMMA);
            builder.append(this.arg2);
        }


    } // OneArgFunc

    private static final class ThreeArgFunc extends OperationExpression.TypedFunctionExpression {

        private final Object arg1;

        private final Object arg2;

        private final Object arg3;

        private ThreeArgFunc(String name, boolean buildIn, MappingType returnType, Object arg1, Object arg2,
                             Object arg3) {
            super(name, buildIn, returnType);
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.arg3 = arg3;
        }

        private ThreeArgFunc(String name, MappingType returnType, Object arg1, Object arg2, Object arg3) {
            super(name, true, returnType);
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.arg3 = arg3;
        }

        @Override
        void appendArg(StringBuilder sqlBuilder, _SqlContext context) {
            appendOneArg(this.name, this.arg1, sqlBuilder, context);
            sqlBuilder.append(_Constant.COMMA);
            appendOneArg(this.name, this.arg2, sqlBuilder, context);
            sqlBuilder.append(_Constant.COMMA);
            appendOneArg(this.name, this.arg3, sqlBuilder, context);
        }

        @Override
        void argToString(StringBuilder builder) {
            builder.append(this.arg1);
            builder.append(_Constant.COMMA);
            builder.append(this.arg2);
            builder.append(_Constant.COMMA);
            builder.append(this.arg3);
        }


    } // ThreeArgFunc

    private static final class MultiArgFunc extends OperationExpression.TypedFunctionExpression {

        private final List<?> argList;

        private MultiArgFunc(String name, boolean buildIn, MappingType returnType, List<?> argList) {
            super(name, buildIn, returnType);
            this.argList = argList;
        }

        private MultiArgFunc(String name, MappingType returnType, List<?> argList) {
            super(name, true, returnType);
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

    private static final class NoParensFunc extends OperationExpression.TypedFunctionExpression
            implements FunctionUtils.NoParensFunction {

        /// @see #noParensFunc(String, MappingType)
        private NoParensFunc(String name, MappingType returnType) {
            super(name, true, returnType); //no parens function must be build-in,currently
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

    private static final class ZeroArgPredicate extends OperationPredicate.SqlFunctionPredicate
            implements FunctionUtils.NoArgFunction {

        private ZeroArgPredicate(String name, boolean buildIn) {
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


    } // ZeroArgPredicate

    private static final class OneArgPredicate extends OperationPredicate.SqlFunctionPredicate {

        private final Object arg;

        private OneArgPredicate(String name, boolean buildIn, Object arg) {
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


    } // OneArgPredicate

    private static final class TwoArgPredicate extends OperationPredicate.SqlFunctionPredicate {

        private final Object arg1;

        private final Object arg2;

        private TwoArgPredicate(String name, boolean buildIn, Object arg1, Object arg2) {
            super(name, buildIn);
            this.arg1 = arg1;
            this.arg2 = arg2;
        }

        @Override
        void appendArg(StringBuilder sqlBuilder, _SqlContext context) {
            appendOneArg(this.name, this.arg1, sqlBuilder, context);
            sqlBuilder.append(_Constant.COMMA);
            appendOneArg(this.name, this.arg2, sqlBuilder, context);
        }

        @Override
        void argToString(StringBuilder builder) {
            builder.append(this.arg1);
            builder.append(_Constant.COMMA);
            builder.append(this.arg2);
        }


    } // TwoArgPredicate

    private static final class ThreeArgPredicate extends OperationPredicate.SqlFunctionPredicate {

        private final Object arg1;

        private final Object arg2;

        private final Object arg3;

        private ThreeArgPredicate(String name, boolean buildIn, Object arg1, Object arg2, Object arg3) {
            super(name, buildIn);
            this.arg1 = arg1;
            this.arg2 = arg2;
            this.arg3 = arg3;
        }

        @Override
        void appendArg(StringBuilder sqlBuilder, _SqlContext context) {
            appendOneArg(this.name, this.arg1, sqlBuilder, context);
            sqlBuilder.append(_Constant.COMMA);
            appendOneArg(this.name, this.arg2, sqlBuilder, context);
            sqlBuilder.append(_Constant.COMMA);
            appendOneArg(this.name, this.arg3, sqlBuilder, context);
        }

        @Override
        void argToString(StringBuilder builder) {
            builder.append(this.arg1);
            builder.append(_Constant.COMMA);
            builder.append(this.arg2);
            builder.append(_Constant.COMMA);
            builder.append(this.arg3);
        }


    } // ThreeArgPredicate

    private static final class MultiArgPredicate extends OperationPredicate.SqlFunctionPredicate {

        private final List<?> argList;

        private MultiArgPredicate(String name, boolean buildIn, List<?> argList) {
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


    } // MultiArgPredicate


}
