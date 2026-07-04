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


package io.army.dialect;

import io.army.env.ArmyEnvironment;
import io.army.env.ArmyKey;
import io.army.env.EscapeMode;
import io.army.lang.Nullable;
import io.army.mapping.MappingEnv;
import io.army.mapping.MappingType;
import io.army.meta.FieldMeta;
import io.army.meta.ServerMeta;
import io.army.meta.TypeMeta;
import io.army.sqltype.DataType;
import io.army.util.ArrayUtils;
import io.army.util._TimeUtils;

import java.time.temporal.Temporal;

abstract class ArmyLiteralHandler<T extends ArmyParser> implements LiteralHandler {

    final T parser;

    final ServerMeta serverMeta;

    final ArmyEnvironment env;

    final EscapeMode literalEscapeMode;

    private final MappingEnv mappingEnv;

    /// @see ArmyKey#TRUNCATED_TIME_TYPE
    private final boolean truncatedTimeType;

    ArmyLiteralHandler(T parser) {
        this.parser = parser;
        this.serverMeta = parser.serverMeta;
        this.env = parser.env;
        this.literalEscapeMode = this.env.getOrDefault(ArmyKey.LITERAL_ESCAPE_MODE);
        this.mappingEnv = parser.mappingEnv;
        this.truncatedTimeType = this.env.getOrDefault(ArmyKey.TRUNCATED_TIME_TYPE);

    }

    @Override
    public final void safeLiteral(TypeMeta typeMeta, @Nullable Object value, boolean typeName, StringBuilder sqlBuilder
            , @Nullable DataType container) {
        final MappingType type;
        if (typeMeta instanceof MappingType) {
            type = (MappingType) typeMeta;
        } else {
            type = typeMeta.mappingType();
        }
        final DataType dataType;
        dataType = type.map(this.serverMeta);

        if (value != null) {
            value = type.beforeBind(dataType, this.mappingEnv, value);
        }

        if (value instanceof Temporal && typeMeta instanceof FieldMeta && this.truncatedTimeType) {
            value = _TimeUtils.truncatedIfNeed(((FieldMeta<?>) typeMeta).scale(), (Temporal) value);
        }

        //TODO validate non-field codec

        bindLiteral(typeMeta, dataType, value, typeName, sqlBuilder, container);
    }


    abstract void bindLiteral(TypeMeta typeMeta, DataType dataType, @Nullable Object value, boolean typeName,
                              StringBuilder sqlBuilder, @Nullable DataType container);


    static void arrayDimensionSuffix(TypeMeta typeMeta, StringBuilder sqlBuilder) {
        final MappingType type;
        if (typeMeta instanceof MappingType) {
            type = (MappingType) typeMeta;
        } else {
            type = typeMeta.mappingType();
        }
        sqlBuilder.repeat("[]", ArrayUtils.dimensionOfType(type));
    }


}
