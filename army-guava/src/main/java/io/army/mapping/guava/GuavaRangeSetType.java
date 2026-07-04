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

package io.army.mapping.guava;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;
import io.army.criteria.CriteriaException;
import io.army.dialect.Database;
import io.army.dialect.UnsupportedDialectException;
import io.army.dialect._Constant;
import io.army.executor.DataAccessException;
import io.army.function.TextFunction;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.mapping.UnaryGenericsMapping;
import io.army.mapping._ArmyBuildInType;
import io.army.mapping.guava.array.GuavaRangeSetArrayType;
import io.army.meta.ServerMeta;
import io.army.serialize.RangeDeserializer;
import io.army.serialize.RecordDeserializer;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.FuncClassValue;
import io.army.util._Assert;

import java.util.Objects;

/// @see GuavaRangeType
/// @see RangeSet
/// @see <a href="https://www.postgresql.org/docs/current/rangetypes.html#RANGETYPES-IO">Range Types</a>
public abstract class GuavaRangeSetType extends _ArmyBuildInType
        implements MappingType.SqlMultiRange, UnaryGenericsMapping {

    public static GuavaRangeSetType fromTypeArg(final Class<?> javaType, final Class<?> typeArg) {
        if (javaType != RangeSet.class) {
            throw errorJavaType(GuavaRangeSetType.class, javaType);
        }
        return BuildInMultiRangeType.CLASS_VALUE.get(typeArg);
    }

    /// private field
    private static final RecordDeserializer PG_DESERIALIZER = RecordDeserializer.builder()
            .dataTypeLabel("PostgreSQL Multi Range")
            .leftBoundary(_Constant.LEFT_BRACE)
            .delim(_Constant.COMMA)
            .rightBoundary(_Constant.RIGHT_BRACE)

            .backSlashEscapeOn(true)
            .quoteEscapeOn(true)
            .quoteChar(_Constant.DOUBLE_QUOTE)

            .allowQuote(false)
            .allowNothing(false)
            .allowWhitespace(false)

            .nullAsNull(false)
            .build();


    final GuavaRangeType rangeType;

    private final DataType dataType;

    /// private constructor
    private GuavaRangeSetType(GuavaRangeType rangeType, DataType dataType) {
        this.rangeType = rangeType;
        this.dataType = dataType;
    }

    @Override
    public final Class<?> javaType() {
        return RangeSet.class;
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        if (meta.serverDatabase() != Database.PostgreSQL) {
            throw mapError(this, meta);
        }
        return this.dataType;
    }

    @Override
    public final String beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        _Assert.isTrue(dataType == this.dataType, "");
        return serialize(this, env, source, new StringBuilder(128))
                .toString();
    }

    @Override
    public final RangeSet<?> afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {

        final RangeSet<?> value;
        if (source instanceof String text) {
            text = text.trim();
            value = deserialize(this, env, text, 0, text.length(), new StringBuilder(128));
        } else if (source instanceof RangeSet<?> rangeSet) {
            for (Range<?> range : rangeSet.asRanges()) {
                if (!this.rangeType.isInstance(range)) {
                    throw dataAccessError(this, this.dataType, source, null);
                }
            }
            value = rangeSet;
        } else {
            throw dataAccessError(this, this.dataType, source, null);
        }
        return value;
    }

    @Override
    public final Class<?> genericsType() {
        return this.rangeType.subJavaType;
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return GuavaRangeSetArrayType.fromTypeArg(RangeSet[].class, genericsType());
    }


    public static StringBuilder serialize(GuavaRangeSetType type, MappingEnv env, final Object source, final StringBuilder builder) {
        if (!(source instanceof RangeSet<?> rangeSet)) {
            throw paramError(type, type.dataType, source, null);
        }
        builder.append(_Constant.LEFT_BRACE);

        int count = 0;
        for (Range<?> range : rangeSet.asRanges()) {
            if (count > 0) {
                builder.append(_Constant.COMMA);
            }

            GuavaRangeType.serialize(type.rangeType, env, range, builder);

            count++;
        }

        return builder.append(_Constant.RIGHT_BRACE);
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    public static RangeSet deserialize(GuavaRangeSetType type, MappingEnv env, final CharSequence source,
                                       final int srcOffset, final int srcEndIndex, StringBuilder builder) {

        final RangeSet rangeSet = TreeRangeSet.create();

        final TextFunction<Boolean> function;
        function = (text, offset, endIndex) -> {
            final Range<?> value;
            value = GuavaRangeType.deserialize(type.rangeType, env, text, offset, endIndex, builder);
            rangeSet.add(value);
            return Boolean.TRUE;
        };

        final RangeDeserializer rangeDeserializer = GuavaRangeType.PG_DESERIALIZER;

        try {
            PG_DESERIALIZER.deserialize(source, srcOffset, srcEndIndex, function,
                    rangeDeserializer.copyLeftBoundaries(),
                    rangeDeserializer::skipRange,
                    builder);
        } catch (Exception e) {
            throw dataAccessError(type, type.dataType, source, e);
        }

        return rangeSet;
    }


    private static BuildInMultiRangeType mutableType(final Class<?> subJavaType) {
        final GuavaRangeType rangeType;
        rangeType = GuavaRangeType.fromTypeArg(Range.class, subJavaType);
        if (!(rangeType.dataType instanceof PgType)) {
            throw errorJavaType(GuavaRangeSetType.class, subJavaType);
        }

        final PgType multiRangeType;
        multiRangeType = switch ((PgType) rangeType.dataType) {
            case INT4RANGE -> PgType.INT4MULTIRANGE;
            case INT8RANGE -> PgType.INT8MULTIRANGE;
            case NUMRANGE -> PgType.NUMMULTIRANGE;
            case TSRANGE -> PgType.TSMULTIRANGE;
            case TSTZRANGE -> PgType.TSTZMULTIRANGE;
            case DATERANGE -> PgType.DATEMULTIRANGE;
            default -> throw errorJavaType(GuavaRangeSetType.class, subJavaType);
        };
        return new BuildInMultiRangeType(rangeType, multiRangeType);
    }


    private static final class BuildInMultiRangeType extends GuavaRangeSetType {

        private static final ClassValue<BuildInMultiRangeType> CLASS_VALUE = FuncClassValue.create(GuavaRangeSetType::mutableType);

        private BuildInMultiRangeType(GuavaRangeType rangeType, DataType dataType) {
            super(rangeType, dataType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.rangeType.subJavaType);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof BuildInMultiRangeType o) {
                match = o.rangeType.subJavaType == this.rangeType.subJavaType;
            } else {
                match = false;
            }
            return match;
        }


    } // MutableRangeSetType


}
