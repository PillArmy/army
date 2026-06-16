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

package io.army.mapping;

import io.army.criteria.CriteriaException;
import io.army.dialect.Database;
import io.army.dialect.LiteralHandler;
import io.army.dialect.UnsupportedDialectException;
import io.army.dialect._Constant;
import io.army.executor.DataAccessException;
import io.army.function.TextFunction;
import io.army.lang.Nullable;
import io.army.mapping.array.RangeArrayType;
import io.army.meta.ServerMeta;
import io.army.serialize.RangeDeserializer;
import io.army.sqltype.DataType;
import io.army.struct.DefinedType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;
import io.army.util._Assert;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/// @see <a href="https://www.postgresql.org/docs/current/rangetypes.html">Range Types</a>
public class RangeType extends _ArmyBuildInType implements MappingType.SqlRange {


    public static RangeType from(Class<?> javaType) {
        return CLASS_VALUE.get(javaType);
    }


    public static final RangeDeserializer PG_DESERIALIZER = RangeDeserializer.builder()
            .leftBoundaries(new char[]{'[', '('})
            .delim(_Constant.COMMA)
            .rightBoundaries(new char[]{']', ')'})
            .quoteChar(_Constant.DOUBLE_QUOTE)

            .backSlashEscapeOn(true)
            .quoteEscapeOn(true)

            .nullAsNull(false)
            .allowNothing(true)
            .allowWhitespace(true)

            .build();


    private static final ClassValue<RangeType> CLASS_VALUE = FuncClassValue.create(RangeType::createFrom);


    private final Class<?> javaType;

    private final DataType dataType;

    private final MappingType subType;

    private final RangeConstructor<Object, ?> constructor;

    private final Supplier<Object> emptyConstructor;

    private final Predicate<Object> emptyFunc;

    private final Predicate<Object> includeLowerFunc;

    private final Predicate<Object> includeUpperFunc;

    private final Function<Object, Object> lowerFunc;

    private final Function<Object, Object> upperFunc;


    /// private constructor
    private RangeType(Class<?> javaType,
                      DataType dataType,
                      RangeConstructor<Object, ?> constructor,
                      Supplier<Object> emptyConstructor,
                      MappingType subType,
                      Predicate<Object> emptyFunc,
                      Predicate<Object> includeLowerFunc,
                      Predicate<Object> includeUpperFunc,
                      Function<Object, Object> lowerFunc,
                      Function<Object, Object> upperFunc) {
        this.javaType = javaType;
        this.dataType = dataType;
        this.subType = subType;

        this.constructor = constructor;
        this.emptyConstructor = emptyConstructor;

        this.emptyFunc = emptyFunc;
        this.includeLowerFunc = includeLowerFunc;
        this.includeUpperFunc = includeUpperFunc;
        this.lowerFunc = lowerFunc;
        this.upperFunc = upperFunc;
    }


    @Override
    public Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public DataType map(ServerMeta meta) throws UnsupportedDialectException {
        if (meta.serverDatabase() != Database.PostgreSQL) {
            throw mapError(this, meta);
        }
        return this.dataType;
    }

    /// @see <a href="https://www.postgresql.org/docs/current/rangetypes.html#RANGETYPES-IO">Range Input/Output</a>
    @Override
    public String beforeBind(final DataType dataType, MappingEnv env, final Object source) throws CriteriaException {
        _Assert.isTrue(dataType == this.dataType, ""); // assert container match

        return serialize(this, env, source, new StringBuilder(3 + 10 * 2))
                .toString();
    }

    @Override
    public Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        _Assert.isTrue(dataType == this.dataType, ""); // assert container match
        if (!(source instanceof String text)) {
            throw dataAccessError(this, this.dataType, source, null);
        }
        return deserialize(this, env, text, 0, text.length(), null);
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return RangeArrayType.from(ArrayUtils.arrayClassOf(this.javaType));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.javaType);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof RangeType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }


    public static StringBuilder serialize(final RangeType type, MappingEnv env, final Object source, final StringBuilder builder) {
        if (!(type.javaType.isInstance(source))) {
            throw paramError(type, type.dataType, source, null);
        }


        try {
            if (type.emptyFunc.test(source)) {
                builder.append("empty");
                return builder;
            }

            final LiteralHandler handler = env.literalHandler();

            if (type.includeLowerFunc.test(source)) {
                builder.append('[');
            } else {
                builder.append('(');
            }

            handler.safeLiteral(type, type.lowerFunc.apply(source), false, builder, type.dataType);

            builder.append(_Constant.COMMA);

            handler.safeLiteral(type, type.upperFunc.apply(source), false, builder, type.dataType);

            if (type.includeUpperFunc.test(source)) {
                builder.append(']');
            } else {
                builder.append(')');
            }
            return builder;
        } catch (Exception e) {
            throw paramError(type, type.dataType, source, e);
        }
    }


    public static Object deserialize(final RangeType type, MappingEnv env, final String source, final int offset,
                                     int endIndex, final @Nullable StringBuilder builder) {

        try {
            final RangeParserFunc func = new RangeParserFunc(type, env, source);
            PG_DESERIALIZER.deserialize(source, offset, endIndex, func, builder);

            final int count = func.consumeCount;

            final Object value;
            if (count < 0) {
                value = type.emptyConstructor.get();
            } else if (count == 4) {
                value = type.constructor.apply(func.includeLower, func.lower, func.upper, func.includeUpper);
            } else {
                throw dataAccessError(type, type.dataType, source, new IllegalStateException("bug"));
            }
            return value;
        } catch (DataAccessException e) {
            throw e;
        } catch (Exception e) {
            throw dataAccessError(type, type.dataType, source, e);
        }
    }


    private static RangeType createFrom(final Class<?> javaType) {
        final String className = javaType.getName();
        final DefinedType definedType;
        if (className.equals("com.google.common.collect.Range")) {

        } else if (className.equals("org.springframework.data.domain.Range")) {

        } else {

        }
        definedType = javaType.getAnnotation(DefinedType.class);

        throw new UnsupportedOperationException();
    }


    private static final class RangeParserFunc implements TextFunction<Object> {

        private final RangeType type;

        private final MappingEnv env;

        private final Object source;

        private int consumeCount = 0;

        private boolean includeLower;

        private Object lower;

        private boolean includeUpper;

        private Object upper;


        private RangeParserFunc(RangeType type, MappingEnv env, Object source) {
            this.type = type;
            this.env = env;
            this.source = source;
        }

        @Override
        public Object apply(final String text, final int offset, final int end) {
            final int length = text.length();
            final int count = this.consumeCount++;

            final String textValue;
            if (length > 1) {
                textValue = text.substring(offset, end);
            } else {
                textValue = null;
            }

            if (length == 0) {
                if (count != 0) {
                    throw dataAccessError(this.type, this.type.dataType, this.source, new IllegalStateException("bug"));
                }
                this.consumeCount = -8;   // empty
            } else if (length == 1) {
                final char ch = text.charAt(offset);
                if (count == 0) {
                    if (ch == '[') {
                        this.includeLower = true;
                    } else if (ch == '(') {
                        this.includeLower = false;
                    } else {
                        throw dataAccessError(this.type, this.type.dataType, this.source, new IllegalStateException("bug"));
                    }
                } else if (count != 3) {
                    throw dataAccessError(this.type, this.type.dataType, this.source, new IllegalStateException("bug"));
                } else if (ch == ']') {
                    this.includeUpper = true;
                } else if (ch == ')') {
                    this.includeUpper = false;
                } else {
                    throw dataAccessError(this.type, this.type.dataType, this.source, new IllegalStateException("bug"));
                }
            } else {
                if (count != 1 && count != 2) {
                    throw dataAccessError(this.type, this.type.dataType, this.source, new IllegalStateException("bug"));
                }
                final DataType subDataType;
                subDataType = this.type.subType.map(this.env.serverMeta());

                Object value;
                value = this.type.subType.afterGet(subDataType, this.env, textValue);

                if (value == MappingType.DOCUMENT_NULL_VALUE) {
                    value = null;
                }

                if (count == 1) {
                    this.lower = value;
                } else {
                    this.upper = value;
                }

            }
            return Boolean.TRUE;
        }


    } // RangeParserFunc




}
