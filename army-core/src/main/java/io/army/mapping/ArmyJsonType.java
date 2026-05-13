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

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/// Package class
/// This class is base class of following :
///
/// - {@link JsonType}
/// - {@link JsonbType}
///
/// @since 0.6.0
abstract class ArmyJsonType extends _ArmyBuildInType {

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
        final String value;
        if (source instanceof String) {
            value = (String) source;
        } else {
            try {
                value = env.jsonCodec().encode(source);
            } catch (Exception e) {
                throw paramError(this, dataType, source, e);
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
        return this.javaType.hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj.getClass() == this.getClass()) {
            match = ((ArmyJsonType) obj).javaType.equals(this.javaType);
        } else {
            match = false;
        }
        return match;
    }

    @Override
    public final MappingType compatibleFor(DataType dataType, Class<?> javaType, List<Class<?>> genericsTypeList)
            throws NoMatchMappingException {
        final MappingType type;
        if (javaType == Map.class) {
            if (genericsTypeList.size() != 2) {
                throw noMatchCompatibleMapping(this, javaType);
            }
            type = creatMapType(genericsTypeList.get(0), genericsTypeList.get(1));
        } else if (genericsTypeList.size() != 1) {
            throw noMatchCompatibleMapping(this, javaType);
        } else if (javaType == List.class) {
            type = createListType(genericsTypeList.getFirst());
        } else if (javaType == Set.class) {
            type = createSetType(genericsTypeList.getFirst());
        } else {
            throw noMatchCompatibleMapping(this, javaType);
        }
        return type;
    }


    abstract MappingType creatMapType(Class<?> keyClass, Class<?> valueClass);

    abstract MappingType createListType(Class<?> elementClass);

    abstract MappingType createSetType(Class<?> elementClass);


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

                Object key, value;
                boolean keyMatch = false, valueMatch = false;
                for (Map.Entry<?, ?> entry : m.entrySet()) {
                    if (!keyMatch) {
                        key = entry.getKey();
                        if (key == null) {
                            keyMatch = true;
                        } else {
                            keyMatch = keyClass.isInstance(key);
                            break;
                        }
                    }

                    value = entry.getValue();
                    if (value == null) {
                        valueMatch = true;
                    } else {
                        valueMatch = valueClass.isInstance(value);
                        break;
                    }
                } // loop

                return keyMatch && valueMatch;
            };
        } else if (!(type instanceof UnaryGenericsMapping t)) {
            throw new IllegalArgumentException("bug");
        } else if (type.javaType == List.class) {
            final Class<?> elementClass = t.genericsType();
            func = (source) -> {
                if (!(source instanceof List<?> c)) {
                    return false;
                }

                boolean match = false;
                for (Object o : c) {
                    if (o == null) {
                        match = true;
                    } else {
                        match = elementClass.isInstance(o);
                        break;
                    }
                } // loop
                return match;
            };
        } else if (type.javaType == Set.class) {
            final Class<?> elementClass = t.genericsType();
            func = (source) -> {
                if (!(source instanceof Set<?> c)) {
                    return false;
                }

                boolean match = false;
                for (Object o : c) {
                    if (o == null) {
                        match = true;
                    } else {
                        match = elementClass.isInstance(o);
                        break;
                    }
                } // loop
                return match;
            };
        } else {
            throw new IllegalArgumentException("bug");
        }
        return func;
    }


}
