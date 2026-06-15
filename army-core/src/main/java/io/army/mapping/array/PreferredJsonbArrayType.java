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

package io.army.mapping.array;

import io.army.criteria.CriteriaException;
import io.army.dialect.UnsupportedDialectException;
import io.army.mapping.MappingType;
import io.army.mapping.PreferredJsonbType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;


/// @see PreferredJsonbType
public class PreferredJsonbArrayType extends ArmyJsonArrayType {

    public static PreferredJsonbArrayType from(final Class<?> javaType) {
        final PreferredJsonbArrayType instance;
        if (!javaType.isArray()) {
            throw errorJavaType(PreferredJsonbArrayType.class, javaType);
        } else if (javaType == String[].class) {
            instance = TEXT_LINEAR;
        } else {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        }
        return instance;
    }


    public static final PreferredJsonbArrayType TEXT_LINEAR = new PreferredJsonbArrayType(String[].class);


    /// private constructor
    private PreferredJsonbArrayType(Class<?> javaType) {
        super(javaType, ArrayUtils.underlyingComponent(javaType));
    }

    @Override
    public final DataType map(ServerMeta meta) throws UnsupportedDialectException {
        return JsonbArrayType.mapDataType(this, meta); // currently ,same
    }

    @Override
    public MappingType underlyingType() {
        return PreferredJsonbType.from(this.underlyingJavaType);
    }

    @Override
    public final MappingType elementType() {
        final MappingType instance;
        final Class<?> javaType = this.javaType;
        if (ArrayUtils.dimensionOf(javaType) == 1) {
            instance = underlyingType();
        } else {
            instance = from(javaType.getComponentType());
        }
        return instance;
    }


    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        return from(ArrayUtils.arrayClassOf(this.javaType));
    }

    @Override
    public final boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof PreferredJsonbArrayType o) {
            match = o.javaType == this.javaType
                    && o.underlyingJavaType == this.underlyingJavaType;
        } else {
            match = false;
        }
        return match;
    }

    private static final class ClassValueHolder {

        private static final ClassValue<PreferredJsonbArrayType> CLASS_VALUE = FuncClassValue.create(PreferredJsonbArrayType::new);

    }


}
