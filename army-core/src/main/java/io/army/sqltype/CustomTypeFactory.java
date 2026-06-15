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
import io.army.util.ArrayUtils;
import io.army.util._StringUtils;

import java.util.List;
import java.util.Objects;

abstract class CustomTypeFactory {

    private CustomTypeFactory() {
        throw new UnsupportedOperationException();
    }


    static CustomType.Builder builder() {
        return new DataTypeBuilder();
    }


    private static final class ArmyCustomType implements CustomType {

        private final String dataTypeName;

        private final ArmyType componentType;

        private final Class<?> javaType;

        private final boolean componentCreateDdl;

        private final String safeTypeAlias;

        private final Class<?> listElementJavaType;

        private CustomType elementType;

        private ArmyCustomType(DataTypeBuilder builder) {
            this.dataTypeName = Objects.requireNonNull(builder.typeName);
            this.componentType = Objects.requireNonNull(builder.obtainComponentType());
            this.javaType = builder.javaType;
            this.componentCreateDdl = builder.componentCreateDdl;
            this.safeTypeAlias = Objects.requireNonNull(builder.safeTypeAlias);
            this.listElementJavaType = builder.listElementJavaType;
            this.elementType = builder.elementInstance;
        }

        @Override
        public String objectName() {
            return this.dataTypeName;
        }

        @Override
        public Class<?> javaType() {
            return this.javaType;
        }

        @Override
        public ArmyType armyType() {
            final ArmyType type;
            if (isArray()) {
                type = ArmyType.ARRAY;
            } else {
                type = this.componentType;
            }
            return type;
        }


        @Override
        public String typeName() {
            return this.dataTypeName;
        }

        @Override
        public String safeTypeAlias() {
            return this.safeTypeAlias;
        }

        @Override
        public boolean isArray() {
            return DataType.isArrayTypeName(this.dataTypeName);
        }

        @Override
        public boolean isComponentCreateDdl() {
            final boolean yes;
            if (isArray()) {
                yes = false;
            } else {
                yes = this.componentCreateDdl;
            }
            return yes;
        }

        @Override
        public String componentTypeName() {
            final String typeName;
            final int index;
            if ((index = this.dataTypeName.indexOf("[]")) > -1) {
                typeName = this.dataTypeName.substring(0, index);
            } else if (this.dataTypeName.startsWith("_")) {
                typeName = this.dataTypeName.substring(1);
            } else {
                typeName = this.dataTypeName;
            }
            return typeName;
        }

        @Override
        public boolean isUnknown() {
            return this.componentType == ArmyType.UNKNOWN;
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
            CustomType dataType;
            if (!isArray()) {
                dataType = null;
            } else if ((dataType = this.elementType) == null) {
                this.elementType = dataType = createElementType();
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
            } else if (obj instanceof ArmyCustomType o) {
                match = o.dataTypeName.equals(this.dataTypeName);
            } else {
                match = false;
            }
            return match;
        }

        @Override
        public String toString() {
            return this.dataTypeName;
        }

        private CustomType createElementType() {
            int index;
            final CustomType dataType;
            if ((index = this.dataTypeName.indexOf("[]")) > -1) {
                dataType = CustomType.builder()
                        .typeName(this.dataTypeName.substring(0, index))
                        .javaType(obtainComponentJavaType())
                        .componentType(this.componentType)
                        .safeTypeAlias(this.safeTypeAlias.substring(0, index))
                        .componentCreateDdl(this.componentCreateDdl)
                        .build();
            } else if (this.dataTypeName.startsWith("_")) {
                dataType = CustomType.builder()
                        .typeName(this.dataTypeName.substring(1))
                        .javaType(obtainComponentJavaType())
                        .componentType(this.componentType)
                        .safeTypeAlias(this.safeTypeAlias.substring(1))
                        .componentCreateDdl(this.componentCreateDdl)
                        .build();
            } else {
                throw new IllegalStateException("bug");
            }
            return dataType;
        }

        private Class<?> obtainComponentJavaType() {
            final Class<?> javaType = this.javaType;
            final Class<?> componentJavaType;
            if (javaType == Object.class) {
                componentJavaType = Object.class;
            } else if (List.class.isAssignableFrom(javaType)) {
                componentJavaType = Objects.requireNonNull(this.listElementJavaType);
            } else {
                componentJavaType = ArrayUtils.underlyingComponent(javaType);
            }
            return componentJavaType;
        }


    } // DatabaseBuildInType

    private static final class DataTypeBuilder implements CustomType.Builder {

        private String typeName;

        private ArmyType componentType;

        private Class<?> javaType;

        private boolean componentCreateDdl;

        private String safeTypeAlias;

        private Class<?> listElementJavaType;

        private CustomType elementInstance;

        private DataTypeBuilder() {
        }

        @Override
        public CustomType.Builder typeName(String typeName) {
            this.typeName = typeName;
            return this;
        }

        @Override
        public CustomType.Builder componentType(ArmyType armyType) {
            this.componentType = armyType;
            return this;
        }

        @Override
        public CustomType.Builder elementInstance(CustomType customType) {
            this.elementInstance = customType;
            return this;
        }

        @Override
        public CustomType.Builder javaType(Class<?> javaType) {
            this.javaType = javaType;
            return this;
        }

        @Override
        public CustomType.Builder componentCreateDdl(boolean yes) {
            this.componentCreateDdl = yes;
            return this;
        }

        @Override
        public CustomType.Builder safeTypeAlias(String safeTypeAlias) {
            this.safeTypeAlias = safeTypeAlias;
            return this;
        }

        @Override
        public CustomType.Builder listElementJavaType(Class<?> elementJavaType) {
            this.listElementJavaType = elementJavaType;
            return this;
        }

        @Override
        public CustomType build() {
            final String typeName = this.typeName;
            final Class<?> javaType = this.javaType;
            if (javaType == null) {
                throw new IllegalArgumentException(String.format("%s must be set", "javaType"));
            }
            if (!_StringUtils.hasText(typeName)) {
                throw new IllegalArgumentException(String.format("%s must be set", "typeName"));
            }

            final boolean arrayTypeName = DataType.isArrayTypeName(typeName);
            final boolean listClass = List.class.isAssignableFrom(javaType);
            if (arrayTypeName && javaType != Object.class && !javaType.isArray() && !listClass) {
                throw new IllegalArgumentException(String.format("%s %s and %s not match", "javaType", javaType.getName(), typeName));
            }

            if (listClass && this.listElementJavaType == null) {
                throw new IllegalArgumentException(String.format("%s must be set", "listElementJavaType"));
            }


            final ArmyType armyType = this.componentType;
            final CustomType elementInstance = this.elementInstance;
            if (armyType == null && elementInstance == null) {
                String m = String.format("At least one of %s and %s must be set", "componentType", "elementInstance");
                throw new IllegalArgumentException(m);
            }
            if (armyType == ArmyType.ARRAY) {
                throw new IllegalArgumentException(String.format("%s must be not %s", "componentType", "ARRAY"));
            } else if (armyType != null && elementInstance != null) {
                if (armyType != elementInstance.armyType()) {
                    String m = String.format("%s %s and %s %s not match", "componentType", armyType, "elementInstance", elementInstance.armyType());
                    throw new IllegalArgumentException(m);
                }
            }
            if (elementInstance != null) {
                if (!DataType.isArrayTypeName(typeName)) {
                    throw new IllegalArgumentException();
                } else if (DataType.isArrayTypeName(elementInstance.typeName())) {
                    throw new IllegalArgumentException();
                } else if (typeName.endsWith("[]") && !typeName.startsWith(elementInstance.typeName())) {
                    throw new IllegalArgumentException();
                } else if (typeName.startsWith("_") && !typeName.endsWith(elementInstance.typeName())) {
                    throw new IllegalArgumentException();
                }
            }


            final String safeTypeAlias = this.safeTypeAlias;
            if (!_StringUtils.hasText(safeTypeAlias)) {
                this.safeTypeAlias = this.typeName;
            } else {
                final boolean aliasArrayTypeName = DataType.isArrayTypeName(safeTypeAlias);
                if (aliasArrayTypeName != arrayTypeName) {
                    throw new IllegalArgumentException(String.format("%s %s and %s not match", "typeName", typeName, safeTypeAlias));
                }
            }
            return new ArmyCustomType(this);
        }

        @Nullable
        private ArmyType obtainComponentType() {
            ArmyType armyType = this.componentType;

            if (armyType == null) {
                final CustomType elementInstance = this.elementInstance;
                if (elementInstance != null) {
                    armyType = elementInstance.armyType();
                }
            }

            return armyType;
        }

    } // DataTypeBuilder


}
