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

import io.army.codec.XmlCodec;
import io.army.criteria.CriteriaException;
import io.army.lang.Nullable;
import io.army.mapping.array.XmlArrayType;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.MySQLType;
import io.army.sqltype.PgType;
import io.army.sqltype.SQLiteType;
import io.army.util.ArrayUtils;
import io.army.util.FuncClassValue;

import java.util.*;

/// @see io.army.mapping.array.XmlArrayType
public class XmlType extends _ArmyBuildInType implements XmlMappingType {


    public static XmlType from(final Class<?> javaType) {
        final XmlType instance;
        if (javaType == String.class) {
            instance = TEXT;
        } else if (Map.class.isAssignableFrom(javaType)) {
            throw errorJavaType(XmlType.class, javaType);
        } else if (Collection.class.isAssignableFrom(javaType)) {
            throw errorJavaType(XmlType.class, javaType);
        } else {
            instance = ClassValueHolder.CLASS_VALUE.get(javaType);
        }
        return instance;
    }

    public static XmlType fromMap(Class<?> keyClass, Class<?> valueClass) {
        if (keyClass == String.class && valueClass == Object.class) {
            return MapXmlType.INSTANCE;
        }
        return new MapXmlType(keyClass, valueClass);
    }

    public static XmlType fromList(Class<?> elementClass) {
        return ListXmlType.CLASS_VALUE.get(elementClass);
    }

    public static XmlType fromSet(Class<?> elementClass) {
        return SetXmlType.CLASS_VALUE.get(elementClass);
    }


    public static final XmlType TEXT = new XmlType(String.class);


    final Class<?> javaType;

    /// private constructor
    private XmlType(Class<?> javaType) {
        this.javaType = javaType;
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }

    @Override
    public final DataType map(final ServerMeta meta) {
        final DataType dataType;
        switch (meta.serverDatabase()) {
            case MySQL:
                dataType = MySQLType.TEXT;
                break;
            case PostgreSQL:
                dataType = PgType.XML;
                break;
            case SQLite:
                dataType = SQLiteType.TEXT;
                break;
            case Oracle:
            case H2:
            default:
                throw MAP_ERROR_HANDLER.apply(this, meta);
        }
        return dataType;
    }

    @Override
    public final String beforeBind(DataType dataType, MappingEnv env, Object source) {
        if (!this.javaType.isInstance(source)) {
            throw PARAM_ERROR_HANDLER.apply(this, dataType, source, null);
        }
        final String value;
        if (source instanceof String) {
            value = (String) source;
        } else {
            try {
                value = env.xmlCodec().encode(source);
            } catch (Exception e) {
                throw PARAM_ERROR_HANDLER.apply(this, dataType, source, e);
            }
        }
        return value;
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, Object source) {
        return deserialize(this, dataType, env, source);
    }


    boolean isInstance(Object source) {
        return this.javaType.isInstance(source);
    }

    @Nullable
    Object decode(XmlCodec codec, String value) {
        return codec.decode(value, this.javaType);
    }


    @Override
    public MappingType arrayTypeOfThis() throws CriteriaException {
        return XmlArrayType.from(ArrayUtils.arrayClassOf(this.javaType));
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.javaType);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof XmlType o) {
            match = o.javaType == this.javaType;
        } else {
            match = false;
        }
        return match;
    }

    public static Object deserialize(XmlType type, DataType dataType, MappingEnv env, Object source) {
        if (type.isInstance(source)) {
            return source;
        }
        if (!(source instanceof String xml)) {
            throw dataAccessError(type, dataType, source, null);
        }
        Object value;

        try {
            value = type.decode(env.xmlCodec(), xml);
        } catch (Exception e) {
            throw dataAccessError(type, dataType, source, e);
        }
        if (value == null) {
            value = DOCUMENT_NULL_VALUE;
        }
        return value;
    }

    private static final class ClassValueHolder {

        private static final ClassValue<XmlType> CLASS_VALUE = FuncClassValue.create(XmlType::new);


    } //ClassValueHolder


    private static final class SetXmlType extends XmlType implements UnaryGenericsMapping {

        private static final ClassValue<SetXmlType> CLASS_VALUE = FuncClassValue.create(SetXmlType::new);

        private final Class<?> elementClass;

        private SetXmlType(Class<?> elementClass) {
            this.elementClass = elementClass;
            super(Set.class);
        }

        @Override
        public Class<?> genericsType() {
            return this.elementClass;
        }

        @Override
        public MappingType arrayTypeOfThis() throws CriteriaException {
            return XmlArrayType.fromTypeArg(Set[].class, this.elementClass);
        }

        @Override
        public MappingType compatibleFor(DataType dataType, Class<?> javaType, List<Class<?>> genericsTypeList) throws NoMatchMappingException {
            if (javaType == Set.class
                    && genericsTypeList.size() == 1) {
                return XmlType.fromSet(genericsTypeList.getFirst());
            }
            throw noMatchCompatibleMapping(this, dataType, javaType);
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.javaType, this.elementClass);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof SetXmlType o) {
                match = o.javaType == this.javaType
                        && o.elementClass == this.elementClass;
            } else {
                match = false;
            }
            return match;
        }

        @Override
        boolean isInstance(Object source) {
            final boolean match;
            if (source instanceof Set<?> c) {
                match = ArmyJsonType.isMatchCollection(c, this.elementClass);
            } else {
                match = false;
            }
            return match;
        }

        @Nullable
        @Override
        Object decode(XmlCodec codec, String value) {
            final List<?> list;
            list = codec.decodeList(value, this.elementClass);
            final Set<?> set;
            if (list == null) {
                set = null;
            } else {
                set = new HashSet<>(list);
            }
            return set;
        }


    } // SetXmlType

    private static final class ListXmlType extends XmlType implements UnaryGenericsMapping {

        private static final ClassValue<ListXmlType> CLASS_VALUE = FuncClassValue.create(ListXmlType::new);

        private final Class<?> elementClass;

        private ListXmlType(Class<?> elementClass) {
            this.elementClass = elementClass;
            super(List.class);
        }


        @Override
        public MappingType arrayTypeOfThis() throws CriteriaException {
            return XmlArrayType.fromTypeArg(List[].class, this.elementClass);
        }

        @Override
        public MappingType compatibleFor(DataType dataType, Class<?> javaType, List<Class<?>> genericsTypeList)
                throws NoMatchMappingException {
            if (javaType == List.class
                    && genericsTypeList.size() == 1) {
                return XmlType.fromList(genericsTypeList.getFirst());
            }
            throw noMatchCompatibleMapping(this, dataType, javaType);
        }

        @Override
        public Class<?> genericsType() {
            return this.elementClass;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.javaType, this.elementClass);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof ListXmlType o) {
                match = o.javaType == this.javaType
                        && o.elementClass == this.elementClass;
            } else {
                match = false;
            }
            return match;
        }

        @Override
        boolean isInstance(Object source) {
            final boolean match;
            if (source instanceof List<?> c) {
                match = ArmyJsonType.isMatchCollection(c, this.elementClass);
            } else {
                match = false;
            }
            return match;
        }

        @Nullable
        @Override
        Object decode(XmlCodec codec, String value) {
            return codec.decodeList(value, this.elementClass);
        }


    } // ListXmlType

    private static final class MapXmlType extends XmlType implements DualGenericsMapping {

        private static final MapXmlType INSTANCE = new MapXmlType(String.class, Object.class);

        private final Class<?> keyClass;

        private final Class<?> valueClass;

        private MapXmlType(Class<?> keyClass, Class<?> valueClass) {
            this.keyClass = keyClass;
            this.valueClass = valueClass;
            super(Map.class);
        }

        @Override
        public MappingType arrayTypeOfThis() throws CriteriaException {
            return XmlArrayType.fromTypeArgs(Map[].class, this.keyClass, this.valueClass);
        }

        @Override
        public MappingType compatibleFor(DataType dataType, Class<?> javaType, List<Class<?>> genericsTypeList)
                throws NoMatchMappingException {
            if (javaType == Map.class
                    && genericsTypeList.size() == 2) {
                return XmlType.fromMap(genericsTypeList.getFirst(), genericsTypeList.getLast());
            }
            throw noMatchCompatibleMapping(this, dataType, javaType);
        }

        @Override
        public Class<?> firstGenericsType() {
            return this.keyClass;
        }

        @Override
        public Class<?> secondGenericsType() {
            return this.valueClass;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.javaType, this.keyClass, this.valueClass);
        }

        @Override
        public boolean equals(final Object obj) {
            final boolean match;
            if (obj == this) {
                match = true;
            } else if (obj instanceof MapXmlType o) {
                match = o.javaType == this.javaType
                        && o.keyClass == this.keyClass
                        && o.valueClass == this.valueClass;
            } else {
                match = false;
            }
            return match;
        }

        @Override
        boolean isInstance(Object source) {
            final boolean match;
            if (source instanceof Map<?, ?> map) {
                match = ArmyJsonType.isMatchMap(map, this.keyClass, this.valueClass);
            } else {
                match = false;
            }
            return match;
        }

        @Nullable
        @Override
        Object decode(XmlCodec codec, String value) {
            return codec.decodeMap(value, this.keyClass, this.valueClass);
        }


    } // MapXmlType


}
