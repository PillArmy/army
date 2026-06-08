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
import io.army.meta.CompositeField;
import io.army.meta.ServerMeta;
import io.army.meta.TypeMeta;
import io.army.meta.TypeObject;
import io.army.sqltype.DataType;
import io.army.sqltype.SQLType;

import java.util.List;

public sealed interface MappingType extends TypeMeta, TypeInfer, TypeItem permits AbstractMappingType, ArrayMappingType {

    /// document type mapping ,perhaps return null value ,for example : {@link JsonType#afterGet(DataType, MappingEnv, Object)}
    Object DOCUMENT_NULL_VALUE = new Object();

    Class<?> javaType();

    DataType map(ServerMeta meta) throws UnsupportedDialectException;

    /// Bind a Java value to its database representation for SQL parameters.
    ///
    /// @param dataType from {@link #map(ServerMeta)}
    /// @param env      mapping environment with database metadata
    /// @param source   never null
    /// @return non-null, the instance of the type that {@link SQLType} allow.
    Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException;

    /// Convert a database value to its Java representation after retrieval.
    ///
    /// @param dataType from {@link  StmtExecutor}
    /// @param env      mapping environment with database metadata
    /// @param source   never null
    /// @return non-null, the instance of {@link #javaType()}.

    Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException;


    /// Create an array mapping type for this element type.
    MappingType arrayTypeOfThis() throws CriteriaException;

    /// Find a compatible mapping type for the given data type and target Java type.
    ///
    /// @throws NoMatchMappingException if no compatible mapping found
    <Z> MappingType compatibleFor(final DataType dataType, final Class<Z> targetType)
            throws NoMatchMappingException;

    default MappingType compatibleFor(final DataType dataType, Class<?> javaType, List<Class<?>> genericsTypeList)
            throws NoMatchMappingException {
        throw new UnsupportedOperationException();
    }


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


    interface SqlInteger extends SqlNumber {


    }

    interface SqlDecimal extends SqlNumber {

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

    ///
    /// This interface is base interface of below:
    ///
    /// - {@link SqlJsonb}
    ///
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

        default MappingType unlimited() {
            throw new UnsupportedOperationException();
        }

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

    interface SqlVector {

    }

    /// This interface representing the type that need to create extension.
    ///
    /// For example: "CREATE EXTENSION IF NOT EXISTS vector" , "CREATE EXTENSION IF NOT EXISTS hstore"
    ///
    /// @see <a href="https://www.postgresql.org/docs/current/sql-createextension.html">CREATE EXTENSION</a>
    /// @see io.army.mapping.optional.VectorType
    /// @see io.army.mapping.postgre.PgHstoreType
    interface SqlExtension {

        String extensionName(ServerMeta serverMeta);
    }

    /// SqlUserDefined does not inherit from {@link MappingType}, because {@link MappingType} is a sealed interface.
    /// User defined type must override {@link #hashCode()} and {@link #equals(Object)}.
    interface SqlUserDefined extends TypeObject {

        /// @return upper case object name(type name)
        String typeName();

        @Override
        default String objectName() {
            return typeName();
        }

        Class<?> javaType();

        @Override
        int hashCode();

        @Override
        boolean equals(Object obj);
    }

    interface SqlEnum extends SqlUserDefined {

        List<String> enumLabelList();

    }

    /// Marker interface for composite (row) types.
    ///
    /// @see <a href="https://www.postgresql.org/docs/current/rowtypes.html">Composite Types</a>
    /// @see <a href="https://www.postgresql.org/docs/current/sql-createtype.html">CREATE TYPE</a>
    /// @see <a href="https://www.postgresql.org/docs/current/sql-altertype.html">ALTER TYPE</a>
    interface SqlComposite extends SqlUserDefined {


        List<CompositeField> fieldList();

    }

    /// Marker interface for domain types.
    ///
    /// @see <a href="https://www.postgresql.org/docs/current/domains.html">Domain Types</a>
    /// @see <a href="https://www.postgresql.org/docs/current/sql-createdomain.html">CREATE DOMAIN</a>
    /// @see <a href="https://www.postgresql.org/docs/current/sql-alterdomain.html">ALTER DOMAIN</a>
    interface SqlDomain extends SqlUserDefined {

        MappingType baseType();

        /// upper case type name
        String baseTypeName();

        /// @return empty or lower case default expression
        String defaultValue();

        boolean isNotNull();

        /// @return empty or lower case constraint
        String constraint();

        /// @return empty or lower case collation name
        String collation();

    }

    /// Marker interface for range types.
    ///
    /// @see <a href="https://www.postgresql.org/docs/current/rangetypes.html">Range Types</a>
    /// @see <a href="https://www.postgresql.org/docs/current/sql-createtype.html">CREATE TYPE</a>
    /// @see <a href="https://www.postgresql.org/docs/current/sql-altertype.html">ALTER TYPE</a>
    interface SqlRange extends SqlUserDefined {

        MappingType rangeSubType();

        /// upper case type name
        String rangeSubTypeName();

        /// If the subtype is collatable, and you want to use a non-default collation in the range's ordering,
        /// specify the desired collation with the collation option.
        ///
        /// @return empty or lower case collation name
        String subTypeCollation();

        /// The name of the corresponding multirange type.
        ///
        /// @return empty or upper case multi
        String multiRangeTypeName();

        /// The name of a b-tree operator class for the subtype.
        ///
        /// @return empty or  lower case subtype operator name
        String subtypeOperator();

        /// The name of the canonicalization function for the range type.
        ///
        /// @return empty or lower case canonicalization function name
        String canonicalFunc();

        /// The name of a difference function for the subtype.
        ///
        /// @return empty or lower case difference function name
        String subtypeDiffFunc();

    }

    /// This interface representing row(record) type
    interface SqlRecord {

    }


}
