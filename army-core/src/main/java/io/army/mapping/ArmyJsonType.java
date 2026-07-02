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


import io.army.codec.JsonCodec;
import io.army.sqltype.DataType;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/// Package class
/// This class is base class of following :
///
/// - {@link JsonType}
/// - {@link JsonbType}
/// - {@link PreferredJsonbType}
///
/// @since 0.6.0
abstract class ArmyJsonType extends _ArmyBuildInCoreType {

    final Class<?> javaType;

    private final BiFunction<String, JsonCodec, Object> decodeFunc;

    private final Predicate<Object> isInstanceFunc;

    /// Package constructor
    ArmyJsonType(Class<?> javaType) {
        this.javaType = javaType;
        if (this instanceof MappingType.GenericsMapping) {
            this.decodeFunc = createDecodeFunc(this);
            this.isInstanceFunc = createIsInstanceFunc(this);
        } else {
            this.decodeFunc = null;
            this.isInstanceFunc = javaType::isInstance;
        }
    }

    @Override
    public final Class<?> javaType() {
        return this.javaType;
    }


    @Override
    public final String beforeBind(DataType dataType, MappingEnv env, final Object source) {
        if (!this.javaType.isInstance(source)) {
            throw PARAM_ERROR_HANDLER.apply(this, dataType, source, null);
        }
        final String value;
        if (source instanceof String) {
            value = (String) source;
        } else {
            try {
                value = env.jsonCodec().encode(source);
            } catch (Exception e) {
                throw PARAM_ERROR_HANDLER.apply(this, dataType, source, e);
            }
        }
        return value;
    }

    @Override
    public final Object afterGet(DataType dataType, MappingEnv env, final Object source) {
        if (this.isInstanceFunc.test(source)) {
            return source;
        }
        if (!(source instanceof String json)) {
            throw dataAccessError(this, dataType, source, null);
        }
        Object value;

        try {
            final BiFunction<String, JsonCodec, Object> decodeFunc = this.decodeFunc;
            if (decodeFunc == null) {
                value = env.jsonCodec().decode(json, this.javaType);
            } else {
                value = decodeFunc.apply(json, env.jsonCodec());
            }
        } catch (Exception e) {
            throw dataAccessError(this, dataType, source, e);
        }
        if (value == null) {
            value = DOCUMENT_NULL_VALUE;
        }
        return value;
    }


    @Override
    public final int hashCode() {
        final int code;
        if (this instanceof UnaryGenericsMapping ug) {
            code = Objects.hash(this.javaType, ug.genericsType());
        } else if (this instanceof DualGenericsMapping dg) {
            code = Objects.hash(this.javaType, dg.firstGenericsType(), dg.secondGenericsType());
        } else {
            code = Objects.hash(this.javaType);
        }
        return code;
    }

    @Override
    public final boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (!(obj instanceof ArmyJsonType o)) {
            match = false;
        } else if (obj.getClass() != this.getClass()) {
            match = false;
        } else if (obj instanceof UnaryGenericsMapping ug) {
            match = o.javaType == this.javaType
                    && ug.genericsType() == ((UnaryGenericsMapping) this).genericsType();
        } else if (obj instanceof DualGenericsMapping dg) {
            match = o.javaType == this.javaType
                    && dg.firstGenericsType() == ((DualGenericsMapping) this).firstGenericsType()
                    && dg.secondGenericsType() == ((DualGenericsMapping) this).secondGenericsType();
        } else {
            match = o.javaType == this.javaType;
        }
        return match;
    }

    @Override
    public final MappingType compatibleFor(DataType dataType, Class<?> javaType, List<Class<?>> genericsTypeList)
            throws NoMatchMappingException {
        final MappingType type;
        if (javaType == Map.class) {
            if (genericsTypeList.size() != 2) {
                throw noMatchCompatibleMapping(this, dataType, javaType);
            }
            type = creatMapType(genericsTypeList.get(0), genericsTypeList.get(1));
        } else if (genericsTypeList.size() != 1) {
            throw noMatchCompatibleMapping(this, dataType, javaType);
        } else if (javaType == List.class) {
            type = createListType(genericsTypeList.getFirst());
        } else if (javaType == Set.class) {
            type = createSetType(genericsTypeList.getFirst());
        } else {
            throw noMatchCompatibleMapping(this, dataType, javaType);
        }
        return type;
    }


    abstract MappingType creatMapType(Class<?> keyClass, Class<?> valueClass);

    abstract MappingType createListType(Class<?> elementClass);

    abstract MappingType createSetType(Class<?> elementClass);


    static boolean isMatchCollection(final Collection<?> collection, final Class<?> elementClass) {
        boolean match = true;
        for (Object o : collection) {
            if (o == null) {
                continue;
            }
            if (!elementClass.isInstance(o)) {
                match = false;
                break;
            }
        } // loop

        return match;
    }

    static boolean isMatchMap(final Map<?, ?> map, final Class<?> keyClass, final Class<?> valueClass) {
        boolean match = true;
        Object value;
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (!keyClass.isInstance(entry.getKey())) {
                match = false;
                break;
            }

            value = entry.getValue();
            if (value == null) {
                continue;
            }
            if (!valueClass.isInstance(value)) {
                match = false;
                break;
            }

        } // loop
        return match;
    }


    private static BiFunction<String, JsonCodec, Object> createDecodeFunc(final ArmyJsonType type) {
        final BiFunction<String, JsonCodec, Object> func;
        if (type instanceof DualGenericsMapping t) {
            if (type.javaType != Map.class) {
                throw new IllegalArgumentException("bug");
            }
            func = (json, codec) -> codec.decodeMap(json, t.firstGenericsType(), t.secondGenericsType());
        } else if (!(type instanceof UnaryGenericsMapping t)) {
            throw new IllegalArgumentException("bug");
        } else if (type.javaType == List.class) {
            func = (json, codec) -> codec.decodeList(json, t.genericsType());
        } else if (type.javaType == Set.class) {
            func = (json, codec) -> {
                List<?> list;
                list = codec.decodeList(json, t.genericsType());
                if (list == null) {
                    return null;
                }
                return new HashSet<>(list);
            };
        } else {
            throw new IllegalArgumentException("bug");
        }
        return func;
    }

    private static Predicate<Object> createIsInstanceFunc(final ArmyJsonType type) {
        final Predicate<Object> func;
        if (type instanceof DualGenericsMapping t) {
            if (type.javaType != Map.class) {
                throw new IllegalArgumentException("bug");
            }
            final Class<?> keyClass, valueClass;
            keyClass = t.firstGenericsType();
            valueClass = t.secondGenericsType();

            func = source -> {
                if (!(source instanceof Map<?, ?> m)) {
                    return false;
                }
                return isMatchMap(m, keyClass, valueClass);
            };
        } else if (!(type instanceof UnaryGenericsMapping t)) {
            throw new IllegalArgumentException("bug");
        } else if (type.javaType == List.class) {
            final Class<?> elementClass = t.genericsType();
            func = (source) -> {
                if (!(source instanceof List<?> c)) {
                    return false;
                }
                return isMatchCollection(c, elementClass);
            };
        } else if (type.javaType == Set.class) {
            final Class<?> elementClass = t.genericsType();
            func = (source) -> {
                if (!(source instanceof Set<?> c)) {
                    return false;
                }

                return isMatchCollection(c, elementClass);
            };
        } else {
            throw new IllegalArgumentException("bug");
        }
        return func;
    }


}
