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
import io.army.executor.DataAccessException;
import io.army.executor.StmtExecutor;
import io.army.mapping.optional.CompositeField;
import io.army.meta.ServerMeta;
import io.army.meta.TypeMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.SQLType;

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


    interface GenericsMapping {

    }


    interface SqlBoolean {

    }


    interface SqlNumber {

    }

    interface SqlFloat extends SqlNumber {

    }

    interface SqlUnsignedNumber extends SqlNumber {

    }

    interface SqlIntegerOrDecimalType extends SqlNumber {

    }

    interface SqlInteger {


    }

    interface SqlDecimal {

    }


    interface SqlBinary {


    }

    interface SqlString {


    }

    interface SqlText extends SqlString {


    }

    interface SqlBlob extends SqlBinary {

    }

    interface SqlBit {

    }


    interface SqlDocument {

    }

    /**
     * <p>
     * This interface is base interface of below:
     * <ul>
     *     <li>{@link SqlJsonb}</li>
     * </ul>
     */
    interface SqlJson extends SqlDocument {

    }


    interface SqlJsonb extends SqlDocument {

    }

    interface SqlJsonPath {

    }

    interface SqlTimeValue {

    }

    interface SqlTemporal extends SqlTimeValue {

    }

    interface SqlLocalTemporal extends SqlTemporal {

    }

    interface SqlOffsetTemporal extends SqlTemporal {

    }

    interface SqlTemporalField extends SqlTimeValue {

    }

    interface SqlTemporalAmount extends SqlTemporal {

    }

    interface SqlDuration extends SqlTemporalAmount {

    }

    interface SqlPeriod extends SqlTemporalAmount {

    }

    interface SqlInterval extends SqlTemporalAmount {

    }

    interface SqlLocalTime extends SqlLocalTemporal {

    }

    interface SqlLocalDate extends SqlLocalTemporal {

    }

    interface SqlLocalDateTime extends SqlLocalTemporal {

    }

    interface SqlOffsetTime extends SqlOffsetTemporal {

    }

    interface SqlOffsetDateTime extends SqlOffsetTemporal {

    }

    interface SqlArray {

        Class<?> underlyingJavaType();

        MappingType elementType();

    }

    interface SqlGeometry {

    }

    interface SqlPoint extends SqlGeometry {

    }

    interface SqlCurve extends SqlGeometry {

    }

    interface SqlLineString extends SqlCurve {

    }

    interface SqlLine extends SqlLineString {

    }

    interface SqlLinearRing extends SqlLineString {

    }

    interface SqlSurface extends SqlGeometry {

    }

    interface SqlPolygon extends SqlSurface {

    }

    interface SqlGeometryCollection extends SqlGeometry {

    }

    interface SqlMultiPoint extends SqlGeometryCollection {

    }

    interface SqlMultiCurve extends SqlGeometryCollection {

    }

    interface SqlMultiLineString extends SqlMultiCurve {

    }

    interface SqlMultiSurface extends SqlGeometryCollection {

    }

    interface SqlMultiPolygon extends SqlMultiSurface {

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
    interface SqlRecord {

    }


}
