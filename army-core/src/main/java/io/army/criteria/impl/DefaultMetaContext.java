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

package io.army.criteria.impl;

import io.army.lang.Nullable;
import io.army.mapping.MappingType;
import io.army.meta.IndexColumnMeta;
import io.army.meta.MetaException;
import io.army.meta.SchemaMeta;
import io.army.meta.TableMeta;
import io.army.modelgen._MetaBridge;
import io.army.util._ResourceUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

final class DefaultMetaContext implements MetaContext {

    private Map<SchemaMeta, Map<String, Class<?>>> tableNameValidMap;

    private Map<Class<?>, Map<String, Boolean>> columnNameValidMap;

    private StringBuilder tempBuilder;

    private Map<String, Map<String, String>> generatorParamMap;

    private Properties tableMetaProperties;

    private Map<List<IndexColumnMeta>, List<IndexColumnMeta>> minColumnMetaMap;

    private Map<List<IndexColumnMeta>, List<IndexColumnMeta>> columnMetaMap;

    private Map<List<Class<?>>, List<Class<?>>> classListMap;


    private Map<Class<?>, List<Method>> factoryMethodMap;


    @Override
    public void validateColumnName(Class<?> domainClass, String columnName) {
        if (_MetaBridge.isCamelCase(columnName)) {
            throw new MetaException(String.format("%s %s.%s %s", domainClass.getName(), "Column", "name", "camel"));
        }
        Map<Class<?>, Map<String, Boolean>> map = this.columnNameValidMap;
        if (map == null) {
            this.columnNameValidMap = map = new HashMap<>();
        }

        final Boolean oldValue;
        oldValue = map.computeIfAbsent(domainClass, _ -> new HashMap<>())
                .putIfAbsent(columnName, Boolean.TRUE);
        if (oldValue != null) {
            String m = String.format("%s %s.%s duplication", domainClass.getName(), "Column", "name");
            throw new MetaException(m);
        }
    }

    @Override
    public List<IndexColumnMeta> minIndexColumnMetaList(final List<IndexColumnMeta> list) {
        Map<List<IndexColumnMeta>, List<IndexColumnMeta>> map = this.minColumnMetaMap;
        if (map == null) {
            this.minColumnMetaMap = map = new HashMap<>();
        }
        return map.computeIfAbsent(list, List::copyOf);
    }

    @Override
    public List<IndexColumnMeta> indexColumnMetaList(List<IndexColumnMeta> list) {
        Map<List<IndexColumnMeta>, List<IndexColumnMeta>> map = this.columnMetaMap;
        if (map == null) {
            this.columnMetaMap = map = new HashMap<>();
        }
        return map.computeIfAbsent(list, List::copyOf);
    }

    @Override
    public List<Class<?>> classList(final List<Class<?>> list) {
        if (list.isEmpty()) {
            return List.of();
        }
        Map<List<Class<?>>, List<Class<?>>> map = this.classListMap;
        if (map == null) {
            this.classListMap = map = new HashMap<>();
        }
        return map.computeIfAbsent(list, List::copyOf);
    }

    @Override
    public Properties tableMetaProperties() {
        Properties properties = this.tableMetaProperties;
        if (properties == null) {
            this.tableMetaProperties = properties = _ResourceUtils.loadArmyProperties(TableMeta.class.getSimpleName());
        }
        return properties;
    }


    @Override
    public StringBuilder tempBuilderAndClear() {
        StringBuilder builder = this.tempBuilder;
        if (builder == null) {
            this.tempBuilder = builder = new StringBuilder(40);
        } else {
            builder.setLength(0); // clear
        }
        return builder;
    }

    @Nullable
    @Override
    public Map<String, String> getGeneratorParamMap(String key) {
        Map<String, Map<String, String>> map = this.generatorParamMap;
        if (map == null) {
            return null;
        }
        return map.get(key);
    }

    @Override
    public void putGeneratorParamMap(String key, Map<String, String> value) {
        Map<String, Map<String, String>> map = this.generatorParamMap;
        if (map == null) {
            this.generatorParamMap = map = new HashMap<>();
        }
        map.put(key, Map.copyOf(value));
    }

    @Override
    public List<Method> factoryMethodList(final Class<?> typeClass) {
        Map<Class<?>, List<Method>> map = this.factoryMethodMap;
        List<Method> list;
        if (map != null && (list = map.get(typeClass)) != null) {
            return list;
        }

        list = createFactoryMethodList(typeClass);
        if (map == null) {
            map = new HashMap<>();
        }
        map.put(typeClass, list);
        return list;
    }


    @Override
    public void clear() {
        this.tempBuilder = null;

        Map<?, ?> map;

        map = this.tableNameValidMap;
        this.tableNameValidMap = null;
        if (map != null) {
            map.clear();
        }

        map = this.columnNameValidMap;
        this.columnNameValidMap = null;
        if (map != null) {
            map.clear();
        }

        map = this.tableMetaProperties;
        this.tableMetaProperties = null;
        if (map != null) {
            map.clear();
        }

        map = this.minColumnMetaMap;
        this.minColumnMetaMap = null;
        if (map != null) {
            map.clear();
        }

        map = this.columnMetaMap;
        this.columnMetaMap = null;
        if (map != null) {
            map.clear();
        }

        map = this.generatorParamMap;
        this.generatorParamMap = null;
        if (map != null) {
            map.clear();
        }

        map = this.factoryMethodMap;
        this.factoryMethodMap = null;
        if (map != null) {
            map.clear();
        }


    }


    private List<Method> createFactoryMethodList(final Class<?> typeClass) {
        if (MappingType.class.isAssignableFrom(typeClass)) {
            String m = String.format("%s is not %s", typeClass.getName(), MappingType.class.getName());
            throw new MetaException(m);
        }


        final List<Method> list = new ArrayList<>(4);
        int modifiers;
        String name;
        Class<?>[] paramTypeArray;

        topLoop:
        for (Method method : typeClass.getDeclaredMethods()) {
            modifiers = method.getModifiers();
            if (!Modifier.isPublic(modifiers)
                    || !Modifier.isStatic(modifiers)
                    || method.getReturnType() != typeClass) {
                continue;
            } // if

            name = method.getName();
            if (!name.startsWith("from")) {
                continue;
            }

            paramTypeArray = method.getParameterTypes();
            if (paramTypeArray.length == 0) {
                continue;
            }

            if (name.equals("fromJavaField")) {
                if (paramTypeArray[0] != Field.class) {
                    continue;
                }
            } else if (paramTypeArray[0] != Class.class) {
                continue;
            }

            switch (name) {
                case "from":
                case "fromList":
                case "fromSet":
                case "fromEnumSet":
                case "fromJavaFiled": {
                    if (paramTypeArray.length != 1) {
                        continue;
                    }
                }
                break;
                case "fromMap":
                case "fromEnumMap":
                case "fromTypeArg": {
                    if (paramTypeArray.length != 2) {
                        continue;
                    } else if (paramTypeArray[1] != Class.class) {
                        continue;
                    }
                }
                break;
                case "fromTypeArgs": {
                    if (paramTypeArray.length == 2) {
                        if (paramTypeArray[1] != Class[].class) {
                            continue;
                        }
                        break;
                    } else if (paramTypeArray.length < 3) {
                        continue;
                    }

                    for (int i = 1; i < paramTypeArray.length; i++) {
                        if (paramTypeArray[i] != Class.class) {
                            continue topLoop;
                        }
                    }
                }
                break;
                case "fromTypeArgAndType": {
                    if (paramTypeArray.length != 3) {
                        continue;
                    } else if (paramTypeArray[1] != Class.class) {
                        continue;
                    } else if (paramTypeArray[2] != MappingType.class) {
                        continue;
                    }
                }
                break;
                case "fromTypeArgsAndTypes": {
                    if (paramTypeArray.length != 3) {
                        continue;
                    } else if (paramTypeArray[1] != Class[].class) {
                        continue;
                    } else if (paramTypeArray[2] != MappingType[].class) {
                        continue;
                    }
                }
                break;
                case "fromTypeArgChain": {
                    if (paramTypeArray.length < 3) {
                        continue;
                    }
                    for (int i = 1; i < paramTypeArray.length; i++) {
                        if (paramTypeArray[i] != Class.class && paramTypeArray[i] != Class[].class) {
                            continue topLoop;
                        }
                    }
                }
                break;
                case "fromTypeArgChainAndTypes": {
                    if (paramTypeArray.length < 3) {
                        continue;
                    } else if ((paramTypeArray.length & 1) == 0) {
                        continue;
                    }

                    for (int i = 1; i < paramTypeArray.length; i += 2) {
                        if (paramTypeArray[i] != Class[].class) {
                            continue topLoop;
                        } else if (paramTypeArray[i + 1] != MappingType[].class) {
                            continue topLoop;
                        }
                    } // loop
                }
                break;
                case "fromParam": {
                    if (paramTypeArray.length != 2) {
                        continue;
                    } else if (paramTypeArray[1] != String.class) {
                        continue;
                    }
                }
                break;
                case "fromParams": {
                    if (paramTypeArray.length == 2) {
                        if (paramTypeArray[1] != String[].class) {
                            continue;
                        }
                        break;
                    } else if (paramTypeArray.length < 3) {
                        continue;
                    }

                    for (int i = 1; i < paramTypeArray.length; i++) {
                        if (paramTypeArray[i] != String.class) {
                            continue topLoop;
                        }
                    }
                }
                break;
                default:
                    continue;
            } // switch


            list.add(method);

        } // loop
        return List.copyOf(list);
    }


}
