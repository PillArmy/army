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

import io.army.codec.XmlCodec;
import io.army.criteria.CriteriaException;
import io.army.dialect.Database;
import io.army.dialect.UnsupportedDialectException;
import io.army.executor.DataAccessException;
import io.army.function.TextFunction;
import io.army.mapping.*;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.PgType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

import java.util.*;
import java.util.function.BiConsumer;

public class XmlArrayType extends _ArmyCoreArrayType {


    public static XmlArrayType from(final Class<?> javaType) {
        final Class<?> componentType;

        final XmlArrayType instance;
        if (javaType == String[].class) {
            instance = TEXT_LINEAR;
        } else if (!javaType.isArray()) {
            throw errorJavaType(XmlArrayType.class, javaType);
        } else if (Map.class.isAssignableFrom((componentType = ArrayUtils.underlyingComponent(javaType)))) {
            throw errorJavaType(XmlArrayType.class, javaType);
        } else if (Collection.class.isAssignableFrom(componentType)) {
            throw errorJavaType(XmlArrayType.class, javaType);
        } else {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        }
        return instance;
    }

    public static XmlArrayType fromTypeArg(final Class<?> javaType, final Class<?> typeArg) {
        if (!javaType.isArray()) {
            throw errorJavaType(XmlArrayType.class, javaType);
        }
        final Class<?> componentType = ArrayUtils.underlyingComponent(javaType);
        final XmlType underlyingType;
        if (componentType == List.class) {
            underlyingType = XmlType.fromList(typeArg);
        } else if (componentType == Set.class) {
            underlyingType = XmlType.fromSet(typeArg);
        } else {
            throw errorJavaType(XmlArrayType.class, javaType);
        }
        return new CollectionXmlArrayType(javaType, underlyingType);
    }

    public static XmlArrayType fromTypeArgs(final Class<?> javaType, final Class<?> keyClass, final Class<?> valueClass) {
        if (!javaType.isArray()) {
            throw errorJavaType(XmlArrayType.class, javaType);
        }
        final Class<?> componentType = ArrayUtils.underlyingComponent(javaType);
        if (componentType != Map.class) {
            throw errorJavaType(XmlArrayType.class, javaType);
        }

        return new MapXmlArrayType(javaType, XmlType.fromMap(keyClass, valueClass));
    }

    public static final XmlArrayType UNLIMITED = new XmlArrayType(Object.class, XmlType.TEXT);

    public static final XmlArrayType TEXT_LINEAR = new XmlArrayType(String[].class, XmlType.TEXT);

    static {
        addArrayFromFunc(XmlArrayType.class, XmlArrayType::from);
        addArrayFromTypeArgFunc(XmlArrayType.class, XmlArrayType::fromTypeArg);
        addArrayFromTypeArgsFunc(XmlArrayType.class, XmlArrayType::fromTypeArgs);
    }

    final Class<?> javaType;

    final XmlType underlyingType;

    private XmlArrayType(Class<?> javaType, XmlType underlyingType) {
        this.javaType = javaType;
        this.underlyingType = underlyingType;
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final DataType map(final ServerMeta meta) throws UnsupportedDialectException {
        if (meta.serverDatabase() != Database.PostgreSQL) {
            throw MAP_ERROR_HANDLER.apply(this, meta);
        }
        return PgType.XML_ARRAY;
    }

    @Override
    public final Object beforeBind(DataType dataType, MappingEnv env, Object source) throws CriteriaException {
        final XmlCodec codec;
        codec = env.xmlCodec();

        final BiConsumer<Object, StringBuilder> consumer;
        consumer = (element, appender) -> {
            PostgreArrays.encodeElement(codec.encode(element), appender);
        };
        return PostgreArrays.arrayBeforeBind(source, consumer, dataType, this);
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) throws DataAccessException {
        final TextFunction<?> function;
        function = (text, offset, end) -> XmlType.deserialize(this.underlyingType, dataType, env, text.subSequence(offset, end).toString());
        return PostgreArrays.arrayAfterGet(this, dataType, source, function, null);
    }

    @Override
    public final Class<?> underlyingJavaType() {
        return this.underlyingType.javaType();
    }

    @Override
    public final MappingType underlyingType() {
        return this.underlyingType;
    }

    @Override
    public final MappingType elementType() {
        final Class<?> javaType, componentType;
        javaType = this.javaType;

        final MappingType instance;
        if (javaType == Object.class) {
            instance = this;
        } else if (!(componentType = this.javaType.getComponentType()).isArray()) {
            instance = this.underlyingType;
        } else if (this instanceof UnaryGenericsMapping ug) {
            instance = fromTypeArg(componentType, ug.genericsType());
        } else if (this instanceof DualGenericsMapping du) {
            instance = fromTypeArgs(componentType, du.firstGenericsType(), du.secondGenericsType());
        } else {
            instance = from(componentType);
        }
        return instance;
    }

    @Override
    public final MappingType arrayTypeOfThis() throws CriteriaException {
        final Class<?> javaType = this.javaType;

        final MappingType instance;
        if (javaType == Object.class) {
            instance = this;
        } else if (this instanceof UnaryGenericsMapping ug) {
            instance = fromTypeArg(ArrayUtils.arrayClassOf(javaType), ug.genericsType());
        } else if (this instanceof DualGenericsMapping du) {
            instance = fromTypeArgs(ArrayUtils.arrayClassOf(javaType), du.firstGenericsType(), du.secondGenericsType());
        } else {
            instance = from(ArrayUtils.arrayClassOf(javaType));
        }
        return instance;
    }


    @Override
    public final int hashCode() {
        return Objects.hash(this.javaType, this.underlyingType);
    }

    @Override
    public final boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof XmlArrayType o) {
            match = o.getClass() == this.getClass()
                    && o.javaType == this.javaType
                    && o.underlyingType.equals(this.underlyingType);
        } else {
            match = false;
        }
        return match;
    }


    private static final class CollectionXmlArrayType extends XmlArrayType implements UnaryGenericsMapping {

        private CollectionXmlArrayType(Class<?> javaType, XmlType underlyingType) {
            super(javaType, underlyingType);
        }

        @Override
        public Class<?> genericsType() {
            return ((UnaryGenericsMapping) this.underlyingType).genericsType();
        }


    } // CollectionXmlArrayType

    private static final class MapXmlArrayType extends XmlArrayType implements DualGenericsMapping {

        private MapXmlArrayType(Class<?> javaType, XmlType underlyingType) {
            super(javaType, underlyingType);
        }

        @Override
        public Class<?> firstGenericsType() {
            return ((DualGenericsMapping) this.underlyingType).firstGenericsType();
        }

        @Override
        public Class<?> secondGenericsType() {
            return ((DualGenericsMapping) this.underlyingType).secondGenericsType();
        }


    } // MapXmlArrayType

    private static final class ClassValueHolder {

        private static XmlArrayType fromObj(Class<?> javaType) {
            return new XmlArrayType(javaType, XmlType.from(ArrayUtils.underlyingComponent(javaType)));
        }

        private static final ClassValue<XmlArrayType> CLASS_VALUE = FuncClassValue.create(ClassValueHolder::fromObj);

    } //PojoClassValueHolder


}
