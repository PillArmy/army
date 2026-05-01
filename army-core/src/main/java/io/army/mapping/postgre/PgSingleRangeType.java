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

package io.army.mapping.postgre;

import io.army.annotation.Mapping;
import io.army.criteria.CriteriaException;
import io.army.executor.DataAccessException;
import io.army.lang.Nullable;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingSupport;
import io.army.mapping.MappingType;
import io.army.mapping.NoMatchMappingException;
import io.army.mapping.postgre.array.PostgreSingleRangeArrayType;
import io.army.meta.MetaException;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ArrayUtils;
import io.army.util._Exceptions;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;

/// 
/// This class representing Postgre Built-in Range  Types type {@link MappingType}
/// @see <a href="https://www.postgresql.org/docs/15/rangetypes.html#RANGETYPES-BUILTIN">Built-in Range and Multirange Types</a>
public final class PgSingleRangeType extends PgRangeType implements PgRangeType.SingleRangeType {


/// @param javaType non-array class. If javaType isn't String array,then must declare static 'create' factory method.
/// see {@link PostgreRange}
/// @param param    from {@link Mapping#params()} ,it's the name of 
/// - {@link PgType#INT4RANGE}
/// - {@link PgType#INT8RANGE}
/// - {@link PgType#NUMRANGE}
/// - {@link PgType#DATERANGE}
/// - {@link PgType#TSRANGE}
/// - {@link PgType#TSTZRANGE}
/// 
/// @throws IllegalArgumentException throw when javaType error
/// @throws MetaException            throw when param error.
    public static PgSingleRangeType from(final Class<?> javaType, final String param) throws MetaException {
        final PgType sqlType;
        try {
            sqlType = PgType.valueOf(param);
        } catch (IllegalArgumentException e) {
            throw new MetaException(e.getMessage(), e);
        }
        if (isNotSingleRangeType(sqlType)) {
            throw new MetaException(sqlTypeErrorMessage(sqlType));
        }
        return from(javaType, sqlType);
    }


/// @param javaType non-array class. If javaType isn't String array,then must declare static 'create' factory method.
/// see {@link PostgreRange}
/// @param sqlType  valid instance:
/// - {@link PgType#INT4RANGE}
/// - {@link PgType#INT8RANGE}
/// - {@link PgType#NUMRANGE}
/// - {@link PgType#DATERANGE}
/// - {@link PgType#TSRANGE}
/// - {@link PgType#TSTZRANGE}
/// 
    public static PgSingleRangeType from(final Class<?> javaType, final PgType sqlType)
            throws IllegalArgumentException {

        final RangeFunction<?, ?> rangeFunc;

        final PgSingleRangeType instance;
        if (isNotSingleRangeType(sqlType)) {
            throw new IllegalArgumentException(sqlTypeErrorMessage(sqlType));
        } else if (javaType == String.class) {
            instance = textInstance(sqlType);
        } else if (javaType.isArray()) {
            throw errorJavaType(PgSingleRangeType.class, javaType);
        } else if ((rangeFunc = tryCreateDefaultRangeFunc(javaType, boundJavaType(sqlType))) == null) {
            throw errorJavaType(PgSingleRangeType.class, javaType);
        } else {
            instance = new PgSingleRangeType(sqlType, javaType, rangeFunc);
        }
        return instance;
    }


/// @param javaType non-array and non-string class.
/// see {@link PostgreRange}
/// @param sqlType  valid instance: 
/// - {@link PgType#INT4RANGE}
/// - {@link PgType#INT8RANGE}
/// - {@link PgType#NUMRANGE}
/// - {@link PgType#DATERANGE}
/// - {@link PgType#TSRANGE}
/// - {@link PgType#TSTZRANGE}
/// 
/// @throws IllegalArgumentException throw when javaType or sqlType error
    public static <T, R> PgSingleRangeType fromFunc(final Class<? extends R> javaType, final PgType sqlType,
                                                    final RangeFunction<T, R> rangeFunc)
            throws IllegalArgumentException {
        Objects.requireNonNull(rangeFunc);
        if (isNotSingleRangeType(sqlType)) {
            throw new IllegalArgumentException(sqlTypeErrorMessage(sqlType));
        } else if (javaType == String.class || javaType.isArray()) {
            throw errorJavaType(PgSingleRangeType.class, javaType);
        }
        return new PgSingleRangeType(sqlType, javaType, rangeFunc);
    }


/// @param javaType non-array and non-string class.
/// see {@link PostgreRange}
/// @param param    from {@link Mapping#params()} ,it's the name of 
/// - {@link PgType#INT4RANGE}
/// - {@link PgType#INT8RANGE}
/// - {@link PgType#NUMRANGE}
/// - {@link PgType#DATERANGE}
/// - {@link PgType#TSRANGE}
/// - {@link PgType#TSTZRANGE}
/// 
/// @throws IllegalArgumentException throw when javaType error
/// @throws MetaException            throw when param or methodName error.
    public static PgSingleRangeType fromMethod(final Class<?> javaType, final String param,
                                               final String methodName) throws MetaException {

        final PgType sqlType;
        try {
            sqlType = PgType.valueOf(param);
        } catch (IllegalArgumentException e) {
            throw new MetaException(e.getMessage(), e);
        }

        if (isNotSingleRangeType(sqlType)) {
            throw new MetaException(sqlTypeErrorMessage(sqlType));
        } else if (javaType == String.class || javaType.isArray()) {
            throw errorJavaType(PgSingleRangeType.class, javaType);
        }
        final RangeFunction<?, ?> rangeFunc;
        rangeFunc = PgRangeType.createRangeFunction(javaType, boundJavaType(sqlType), methodName);
        return new PgSingleRangeType(sqlType, javaType, rangeFunc);
    }


    public static final PgSingleRangeType INT4_RANGE_TEXT = new PgSingleRangeType(PgType.INT4RANGE, String.class, null);

    public static final PgSingleRangeType INT8_RANGE_TEXT = new PgSingleRangeType(PgType.INT8RANGE, String.class, null);

    public static final PgSingleRangeType NUM_RANGE_TEXT = new PgSingleRangeType(PgType.NUMRANGE, String.class, null);

    public static final PgSingleRangeType DATE_RANGE_TEXT = new PgSingleRangeType(PgType.DATERANGE, String.class, null);

    public static final PgSingleRangeType TS_RANGE_TEXT = new PgSingleRangeType(PgType.TSRANGE, String.class, null);

    public static final PgSingleRangeType TS_TZ_RANGE_TEXT = new PgSingleRangeType(PgType.TSTZRANGE, String.class, null);


    /// package method
    static PgSingleRangeType fromMultiType(final PgMultiRangeType type) {
        final PgType sqlType;
        switch (type.dataType) {
            case INT4MULTIRANGE:
                sqlType = PgType.INT4RANGE;
                break;
            case INT8MULTIRANGE:
                sqlType = PgType.INT8RANGE;
                break;
            case NUMMULTIRANGE:
                sqlType = PgType.NUMRANGE;
                break;
            case DATEMULTIRANGE:
                sqlType = PgType.DATERANGE;
                break;
            case TSMULTIRANGE:
                sqlType = PgType.TSRANGE;
                break;
            case TSTZMULTIRANGE:
                sqlType = PgType.TSTZRANGE;
                break;
            default:
                throw _Exceptions.unexpectedEnum(type.dataType);
        }
        final Class<?> javaType;
        javaType = type.javaType.getComponentType();
        assert !javaType.isArray();

        final PgSingleRangeType instance;
        if (javaType == String.class) {
            instance = textInstance(sqlType);
        } else {
            instance = new PgSingleRangeType(sqlType, javaType, type.rangeFunc);
        }
        return instance;
    }


    /// 
/// package constructor
    private PgSingleRangeType(PgType sqlType, Class<?> javaType, @Nullable RangeFunction<?, ?> rangeFunc) {
        super(sqlType, javaType, rangeFunc);
    }


    @Override
    public <Z> MappingType compatibleFor(final DataType dataType, final Class<Z> targetType) throws NoMatchMappingException {
        final RangeFunction<?, ?> rangeFunc;
        final PgSingleRangeType instance;
        if (targetType == String.class) {
            instance = textInstance(this.dataType);
        } else if (targetType.isArray()) {
            throw noMatchCompatibleMapping(this, targetType);
        } else if ((rangeFunc = tryCreateDefaultRangeFunc(targetType, boundJavaType(this.dataType))) == null) {
            throw noMatchCompatibleMapping(this, targetType);
        } else {
            instance = new PgSingleRangeType(this.dataType, targetType, rangeFunc);
        }
        return instance;
    }

    @Override
    public String beforeBind(DataType dataType, MappingEnv env, final Object source) throws CriteriaException {
        return rangeBeforeBind(this::serialize, source, dataType, this, PARAM_ERROR_HANDLER);
    }


    @Override
    public Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        return rangeAfterGet(source, this.rangeFunc, this::deserialize, dataType, this, ACCESS_ERROR_HANDLER);
    }


    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        final PostgreSingleRangeArrayType instance;
        final RangeFunction<?, ?> rangeFunc = this.rangeFunc;
        if (rangeFunc != null) {
            instance = PostgreSingleRangeArrayType.fromFunc(ArrayUtils.arrayClassOf(this.javaType), this.dataType, rangeFunc);
        } else switch (this.dataType) {
            case INT4RANGE:
                instance = PostgreSingleRangeArrayType.INT4_RANGE_LINEAR;
                break;
            case INT8RANGE:
                instance = PostgreSingleRangeArrayType.INT8_RANGE_LINEAR;
                break;
            case NUMRANGE:
                instance = PostgreSingleRangeArrayType.NUM_RANGE_LINEAR;
                break;
            case DATERANGE:
                instance = PostgreSingleRangeArrayType.DATE_RANGE_LINEAR;
                break;
            case TSRANGE:
                instance = PostgreSingleRangeArrayType.TS_RANGE_LINEAR;
                break;
            case TSTZRANGE:
                instance = PostgreSingleRangeArrayType.TS_TZ_RANGE_LINEAR;
                break;
            default:
                throw _Exceptions.unexpectedEnum(this.dataType);
        }
        return instance;
    }

    @Override
    public MappingType multiRangeType() {
        return PgMultiRangeType.fromSingleType(this);
    }


    @Override
    public PgSingleRangeType _fromSingleArray(final PostgreSingleRangeArrayType type) {
        final PgType sqlType;
        switch (type.dataType) {
            case INT4RANGE_ARRAY:
                sqlType = PgType.INT4RANGE;
                break;
            case INT8RANGE_ARRAY:
                sqlType = PgType.INT8RANGE;
                break;
            case NUMRANGE_ARRAY:
                sqlType = PgType.NUMRANGE;
                break;
            case DATERANGE_ARRAY:
                sqlType = PgType.DATERANGE;
                break;
            case TSRANGE_ARRAY:
                sqlType = PgType.TSRANGE;
                break;
            case TSTZRANGE_ARRAY:
                sqlType = PgType.TSTZRANGE;
                break;
            default:
                throw _Exceptions.unexpectedEnum(type.dataType);
        }
        final Class<?> javaType;
        javaType = type.javaType.getComponentType();
        assert !javaType.isArray();

        final PgSingleRangeType instance;
        if (javaType == String.class) {
            instance = textInstance(sqlType);
        } else {
            instance = new PgSingleRangeType(sqlType, javaType, type.rangeFunc);
        }
        return instance;
    }


/// @param parseFunc 
/// - argument of function possibly is notion 'infinity',see {@link PgRangeType#INFINITY}
/// - function must return null when argument is notion 'infinity' and support it,see {@link PgRangeType#INFINITY}
/// - function must throw {@link IllegalArgumentException} when argument is notion 'infinity' and don't support it,see {@link PgRangeType#INFINITY}
/// 
/// @throws IllegalArgumentException when rangeFunc is null and {@link MappingType#javaType()} isn't {@link String#getClass()}
/// @throws CriteriaException        when text error and handler throw this type.
    @SuppressWarnings("unchecked")
    public static <T, R> R rangeConvert(final Object nonNull, final @Nullable RangeFunction<T, R> rangeFunc,
                                        final Function<String, T> parseFunc, final DataType dataType,
                                        final MappingType type, final ErrorHandler handler) {

        final R value;
        if (nonNull instanceof String) {
            value = parseRange((String) nonNull, rangeFunc, parseFunc, dataType, type, handler);
        } else if (type.javaType().isInstance(nonNull)) {
            value = (R) nonNull;
        } else {
            throw handler.apply(type, dataType, nonNull, null);
        }
        return value;
    }

    public static <T> String rangeBeforeBind(final BiConsumer<T, StringBuilder> boundSerializer, final Object nonNull,
                                             final DataType dataType, final MappingType type, final ErrorHandler handler)
            throws CriteriaException {

        final String value, text;
        char boundChar;
        if (!(nonNull instanceof String)) {
            if (!type.javaType().isInstance(nonNull)) {
                throw handler.apply(type, dataType, nonNull, null);
            }
            final StringBuilder builder = new StringBuilder();
            rangeToText(nonNull, boundSerializer, type, builder);
            value = builder.toString();
        } else if (EMPTY.equalsIgnoreCase((text = (String) nonNull).trim())) {
            value = EMPTY;
        } else if (text.length() < 3) {
            throw handler.apply(type, dataType, nonNull, null);
        } else if ((boundChar = text.charAt(0)) != '[' && boundChar != '(') {
            throw handler.apply(type, dataType, nonNull, null);
        } else if ((boundChar = text.charAt(text.length() - 1)) != ']' && boundChar != ')') {
            throw handler.apply(type, dataType, nonNull, null);
        } else {
            value = text;
        }
        return value;
    }

/// @param parseFunc 
/// - argument of function possibly is notion 'infinity',see {@link PgRangeType#INFINITY}
/// - function must return null when argument is notion 'infinity' and support it,see {@link PgRangeType#INFINITY}
/// - function must throw {@link IllegalArgumentException} when argument is notion 'infinity' and don't support it,see {@link PgRangeType#INFINITY}
/// 
/// @throws IllegalArgumentException            when rangeFunc is null and {@link MappingType#javaType()} isn't {@link String#getClass()}
/// @throws DataAccessException when text error and handler throw this type.
    public static <T, R> R rangeAfterGet(final Object nonNull, final @Nullable RangeFunction<T, R> rangeFunc,
                                         final Function<String, T> parseFunc, final DataType dataType,
                                         final MappingType type, final MappingSupport.ErrorHandler handler) {
        if (!(nonNull instanceof String)) {
            throw ACCESS_ERROR_HANDLER.apply(type, dataType, nonNull, null);
        }
        return parseRange((String) nonNull, rangeFunc, parseFunc, dataType, type, handler);
    }


    /*-------------------below private method -------------------*/


    private static boolean isNotSingleRangeType(final PgType sqlType) {
        final boolean match;
        switch (sqlType) {
            case INT4RANGE:
            case INT8RANGE:
            case NUMRANGE:
            case DATERANGE:
            case TSRANGE:
            case TSTZRANGE:
                match = false;
                break;
            default:
                match = true;

        }
        return match;
    }

    private static PgSingleRangeType textInstance(final PgType sqlType) {
        final PgSingleRangeType instance;
        switch (sqlType) {
            case INT4RANGE:
                instance = INT4_RANGE_TEXT;
                break;
            case INT8RANGE:
                instance = INT8_RANGE_TEXT;
                break;
            case NUMRANGE:
                instance = NUM_RANGE_TEXT;
                break;
            case DATERANGE:
                instance = DATE_RANGE_TEXT;
                break;
            case TSRANGE:
                instance = TS_RANGE_TEXT;
                break;
            case TSTZRANGE:
                instance = TS_TZ_RANGE_TEXT;
                break;
            default:
                throw _Exceptions.unexpectedEnum(sqlType);

        }
        return instance;
    }

    private static String sqlTypeErrorMessage(PgType sqlType) {
        return String.format("%s isn't postgre single-range type", sqlType);
    }


}
