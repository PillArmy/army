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
import io.army.criteria.TypeInfer;
import io.army.criteria.TypeItem;
import io.army.dialect.UnsupportedDialectException;
import io.army.dialect._Constant;
import io.army.executor.DataAccessException;
import io.army.executor.StmtExecutor;
import io.army.mapping.optional.CompositeField;
import io.army.meta.ServerMeta;
import io.army.meta.TypeMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.SQLType;
import io.army.util._StringUtils;

import java.util.List;

public sealed interface MappingType extends TypeMeta, TypeInfer, TypeItem permits AbstractMappingType {

    /**
     * document type mapping ,perhaps return null value ,for example : {@link JsonType#afterGet(DataType, MappingEnv, Object)}
     */
    Object DOCUMENT_NULL_VALUE = new Object();

    Class<?> javaType();


    MappingType arrayTypeOfThis() throws CriteriaException;

    <Z> MappingType compatibleFor(final DataType dataType, final Class<Z> targetType)
            throws NoMatchMappingException;

    DataType map(ServerMeta meta) throws UnsupportedDialectException;

    /**
     * @param dataType from {@link #map(ServerMeta)}
     * @param source   never null
     * @return non-null, the instance of the type that {@link SQLType} allow.
     */
    Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException;

    /**
     * @param dataType from {@link  StmtExecutor}
     * @param source   never null
     * @return non-null, the instance of {@link #javaType()}.
     */

    Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException;


    @Override
    int hashCode();

    @Override
    boolean equals(Object obj);






    interface GenericsMappingType {

        Class<?> javaType();

    }

    enum LengthType {

        TINY(1),
        SMALL(2),
        MEDIUM(3),

        DEFAULT(4),

        LONG(5),
        BIG_LONG(6);

        private final byte value;

        LengthType(int value) {
            this.value = (byte) value;
        }

        public final int compareWith(final LengthType o) {
            return this.value - o.value;
        }

        @Override
        public final String toString() {
            return _StringUtils.builder()
                    .append(LengthType.class.getSimpleName())
                    .append(_Constant.DOT)
                    .append(this.name())
                    .toString();
        }


    } //LengthType


    interface SqlBooleanType {

    }

    /**
     * <p>
     * This interface is base interface of below:
     * <ul>
     *     <li>{@link SqlNumberType}</li>
     *     <li>{@link SqlStringType}</li>
     * </ul>
     */
    interface SqlNumberOrStringType {

    }

    /**
     * <p>
     * This interface is base interface of below:
     * <ul>
     *     <li>{@link SqlNumberType}</li>
     *     <li>{@link SqlBitType}</li>
     * </ul>
     */
    interface SqlNumberOrBitType {

    }

    interface SqlNumberType extends SqlNumberOrStringType, SqlNumberOrBitType {

    }

    interface SqlFloatType extends SqlNumberType {

    }

    interface SqlUnsignedNumberType extends SqlNumberType {

    }

    interface SqlIntegerOrDecimalType extends SqlNumberType {

    }

    interface SqlIntegerType extends SqlIntegerOrDecimalType {

        LengthType lengthType();

    }

    interface SqlDecimalType extends SqlIntegerOrDecimalType {

    }

    /**
     * <p>
     * This interface is base interface of below:
     * <ul>
     *     <li>{@link SqlStringOrBinaryType }</li>
     *     <li>{@link SqlBitType }</li>
     * </ul>
     */
    interface SqlSqlStringOrBinaryOrBitType {

    }

    /**
     * <p>
     * This interface is base interface of below:
     * <ul>
     *     <li>{@link SqlStringType }</li>
     *     <li>{@link SqlBinaryType }</li>
     * </ul>
     */
    interface SqlStringOrBinaryType extends SqlSqlStringOrBinaryOrBitType {

        LengthType lengthType();
    }

    interface SqlBinaryType extends SqlStringOrBinaryType {


    }

    interface SqlStringType extends SqlStringOrBinaryType, SqlNumberOrStringType {


    }

    interface SqlTextType extends SqlStringType {


    }

    interface SqlBlobType extends SqlBinaryType {

    }

    interface SqlBitType extends SqlSqlStringOrBinaryOrBitType, SqlNumberOrBitType {

    }


    interface SqlDocumentType {

    }

    /**
     * <p>
     * This interface is base interface of below:
     * <ul>
     *     <li>{@link SqlJsonType}</li>
     *     <li>{@link SqlJsonbType}</li>
     * </ul>
     */
    interface SqlJsonDocumentType extends SqlDocumentType {

    }

    interface SqlJsonType extends SqlJsonDocumentType {

    }

    interface SqlJsonbType extends SqlJsonDocumentType {

    }

    interface SqlJsonPathType {

    }

    interface SqlTimeValueType {

    }

    interface SqlTemporalType extends SqlTimeValueType {

    }

    interface SqlLocalTemporalType extends SqlTemporalType {

    }

    interface SqlOffsetTemporalType extends SqlTemporalType {

    }

    interface SqlTemporalFieldType extends SqlTimeValueType {

    }

    interface SqlTemporalAmountType extends SqlTemporalType {

    }

    interface SqlDurationType extends SqlTemporalAmountType {

    }

    interface SqlPeriodType extends SqlTemporalAmountType {

    }

    interface SqlIntervalType extends SqlTemporalAmountType {

    }

    interface SqlLocalTimeType extends SqlLocalTemporalType {

    }

    interface SqlLocalDateType extends SqlLocalTemporalType {

    }

    interface SqlLocalDateTimeType extends SqlLocalTemporalType {

    }

    interface SqlOffsetTimeType extends SqlOffsetTemporalType {

    }

    interface SqlOffsetDateTimeType extends SqlOffsetTemporalType {

    }

    interface SqlArray {

        Class<?> underlyingJavaType();

        MappingType elementType();

    }

    interface SqlGeometryType {

    }

    interface SqlPointType extends SqlGeometryType {

    }

    interface SqlCurveType extends SqlGeometryType {

    }

    interface SqlLineStringType extends SqlCurveType {

    }

    interface SqlLineType extends SqlLineStringType {

    }

    interface SqlLinearRingType extends SqlLineStringType {

    }

    interface SqlSurfaceType extends SqlGeometryType {

    }

    interface SqlPolygonType extends SqlSurfaceType {

    }

    interface SqlGeometryCollectionType extends SqlGeometryType {

    }

    interface SqlMultiPointType extends SqlGeometryCollectionType {

    }

    interface SqlMultiCurveType extends SqlGeometryCollectionType {

    }

    interface SqlMultiLineStringType extends SqlMultiCurveType {

    }

    interface SqlMultiSurfaceType extends SqlGeometryCollectionType {

    }

    interface SqlMultiPolygonType extends SqlMultiSurfaceType {

    }


    interface SqlUserDefined {

        String typeName();
    }

    interface SqlComposite extends SqlUserDefined {


        List<CompositeField> fieldList();

    }

    /**
     * <p>This interface representing row(record) type
     */
    interface SqlRecordColumnType {

    }


}
