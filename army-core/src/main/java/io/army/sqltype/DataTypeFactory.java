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

package io.army.sqltype;

import io.army.criteria.TypeDef;
import io.army.criteria.impl._SQLConsultant;
import io.army.lang.Nullable;
import io.army.util._Collections;
import io.army.util._StringUtils;

import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

abstract class DataTypeFactory {

    private DataTypeFactory() {
        throw new UnsupportedOperationException();
    }


    static DataType.CustomType typeFrom(String typeName, final ArmyType armyType, final boolean caseSensitivity) {
        if (!_StringUtils.hasText(typeName)) {
            throw new IllegalArgumentException("typeName must have text");
        }
        if (!caseSensitivity) {
            typeName = typeName.toUpperCase(Locale.ROOT);
        }
        return ArmyDataType.INSTANCE_MAP.computeIfAbsent(typeName, key -> new ArmyDataType(key, armyType));
    }


    private static final class ArmyDataType implements DataType.CustomType {

        private static final ConcurrentMap<String, ArmyDataType> INSTANCE_MAP = _Collections.concurrentHashMap();

        private final String dataTypeName;

        private final ArmyType armyType;

        public ArmyDataType(String dataTypeName, ArmyType armyType) {
            this.dataTypeName = dataTypeName;

            if (isArray()) {
                this.armyType = ArmyType.ARRAY;
            } else {
                this.armyType = armyType;
            }

        }

        @Override
        public String objectName() {
            return this.dataTypeName;
        }

        @Override
        public Class<?> javaType() {
            return String.class;
        }

        @Override
        public ArmyType armyType() {
            return this.armyType;
        }

        @Override
        public String name() {
            return this.dataTypeName;
        }

        @Override
        public String typeName() {
            return this.dataTypeName;
        }

        @Override
        public boolean isArray() {
            return this.dataTypeName.endsWith("[]") || this.dataTypeName.startsWith("_");
        }

        @Override
        public boolean isUnknown() {
            return false;
        }


        @Override
        public TypeDef._TypeDefCharacterSetSpec parens(final long precision) {
            return _SQLConsultant.precision(this, true, precision, 0xFFFF_FFFFL);
        }

        @Override
        public TypeDef parens(final int precision, final int scale) {
            return _SQLConsultant.precisionAndScale(this, precision, scale, Integer.MAX_VALUE, Integer.MAX_VALUE);
        }

        @Nullable
        @Override
        public DataType elementType() {
            DataType dataType;
            if (this.dataTypeName.endsWith("[]")) {
                dataType = DataType.from(this.dataTypeName.substring(0, this.dataTypeName.length() - 2));
            } else if (this.dataTypeName.startsWith("_")) {
                dataType = DataType.from(this.dataTypeName.substring(1));
            } else {
                dataType = null;
            }
            return dataType;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.dataTypeName);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof ArmyDataType o) {
                match = o.dataTypeName.equals(this.dataTypeName);
            } else {
                match = false;
            }
            return match;
        }

        @Override
        public String toString() {
            return String.format("%s[typeName:%s,hash:%s]", getClass().getName(), this.dataTypeName,
                    System.identityHashCode(this));
        }


    }// DatabaseBuildInType
}
