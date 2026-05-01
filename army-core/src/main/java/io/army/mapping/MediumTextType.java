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
import io.army.dialect.UnsupportedDialectException;
import io.army.mapping.array.MediumTextArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.MySQLType;
import io.army.sqltype.PgType;
import io.army.sqltype.SQLiteType;
import io.army.struct.CodeEnum;
import io.army.struct.TextEnum;

import java.time.*;

/// 
/// This class is mapping class of {@link String}.
/// This mapping type can convert below java type:
/// 
/// - {@link Number}
/// - {@link Boolean} 
/// - {@link CodeEnum} 
/// - {@link TextEnum} 
/// - {@link Enum} 
/// - {@link LocalDate} 
/// - {@link LocalDateTime} 
/// - {@link LocalTime} 
/// - {@link OffsetDateTime} 
/// - {@link ZonedDateTime} 
/// - {@link OffsetTime} 
/// - {@link Year}  to {@link Year} string or {@link LocalDate} string
/// - {@link YearMonth}  to {@link LocalDate} string 
/// - {@link MonthDay} to {@link LocalDate} string
/// - {@link Instant} to {@link Instant#getEpochSecond()} string
/// - {@link java.time.Duration} 
/// - {@link java.time.Period} 
/// 
/// to {@link String},if error,throw {@link io.army.ArmyException}
/// * @see StringType
/// @see TextType
/// @since 0.6.0
public final class MediumTextType extends ArmyTextType {

    public static MediumTextType from(final Class<?> javaType) {
        if (javaType != String.class) {
            throw errorJavaType(MediumTextType.class, javaType);
        }
        return INSTANCE;
    }

    public static final MediumTextType INSTANCE = new MediumTextType();

    /// private constructor
    private MediumTextType() {
    }

    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return MediumTextArrayType.LINEAR;
    }

    @Override
    public DataType map(final ServerMeta meta) throws UnsupportedDialectException {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case MySQL:
                dataType = MySQLType.MEDIUMTEXT;
                break;
            case PostgreSQL:
                dataType = PgType.TEXT;
                break;
            case SQLite:
                dataType = SQLiteType.TEXT;
                break;
            default:
                throw MAP_ERROR_HANDLER.apply(this, meta);
        }
        return dataType;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof MediumTextType;
    }




}
