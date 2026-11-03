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

import io.army.criteria.TypedExpression;
import io.army.dialect._Constant;
import io.army.dialect._SqlContext;
import io.army.mapping.MappingType;

abstract class TypedFunctions extends SqlFunctions {

    private TypedFunctions() {
    }

    static TypedExpression oneArgFunc(String name, MappingType returnType, Object arg) {
        return new OneArgFunc(name, returnType, arg);
    }

    static TypedExpression myOneArgFunc(String name, MappingType returnType, Object arg) {
        return new OneArgFunc(name, false, returnType, arg);
    }

    static TypedExpression twoArgFunc(String name, MappingType returnType, Object arg1,Object arg2) {
        return new TwoArgFunc(name, returnType, arg1,arg2);
    }

    static TypedExpression myTwoArgFunc(String name, MappingType returnType, Object arg1,Object arg2) {
        return new TwoArgFunc(name, false, returnType, arg1,arg2);
    }

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

        private TwoArgFunc(String name, boolean buildIn, MappingType returnType, Object arg1,Object arg2) {
            super(name, buildIn, returnType);
            this.arg1 = arg1;
            this.arg2 = arg2;
        }

        private TwoArgFunc(String name, MappingType returnType, Object arg1,Object arg2) {
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




}
