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

package io.army.mapping.mysql;

import io.army.dialect.Database;
import io.army.dialect._Constant;
import io.army.mapping.*;
import io.army.meta.ServerMeta;
import io.army.sqltype.DataType;
import io.army.sqltype.MySQLType;
import io.army.struct.CodeEnum;
import io.army.struct.TextEnum;
import io.army.util.ClassUtils;
import io.army.util.FuncClassValue;
import io.army.util._Collections;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/// @see <a href="https://dev.mysql.com/doc/refman/9.7/en/set.html">SET</a>
@SuppressWarnings("unused")
public final class MySQLTextEnumSetType extends _ArmyBuildInType implements UnaryGenericsMapping {


    public static MySQLTextEnumSetType fromSet(Class<?> elementClass) {
        return CLASS_VALUE.get(TextEnumType.checkEnumClass(MySQLTextEnumSetType.class, elementClass));
    }

    private static final ClassValue<MySQLTextEnumSetType> CLASS_VALUE = FuncClassValue.create(MySQLTextEnumSetType::new);

    private final Class<?> enumClass;

    private final Map<String, TextEnum> textEnumMap;

    /// private constructor
    private MySQLTextEnumSetType(Class<?> javaType) {
        this.enumClass = ClassUtils.enumClass(javaType);
        this.textEnumMap = Map.copyOf(TextEnumType.createTextMap(this.enumClass));
    }


    @Override
    public Class<?> javaType() {
        return Set.class;
    }

    @Override
    public DataType map(final ServerMeta meta) {
        if (meta.serverDatabase() != Database.MySQL) {
            throw mapError(this, meta);
        }
        return MySQLType.SET;
    }


    @Override
    public String beforeBind(DataType dataType, MappingEnv env, Object source) {
        if (!(source instanceof Set<?> set)) {
            throw paramError(this, dataType, source, null);
        }
        final StringBuilder builder = new StringBuilder(set.size() * 10);
        final Class<?> enumClass = this.enumClass;
        int index = 0;
        String textValue;
        for (Object e : set) {
            if (!enumClass.isInstance(e)) {
                throw paramError(this, dataType, source, null);
            }
            if (index > 0) {
                builder.append(_Constant.COMMA);
            }
            builder.append(((TextEnum) e).text());    // https://dev.mysql.com/doc/refman/9.7/en/set.html
            index++;
        }
        return builder.toString();
    }

    @Override
    public Set<?> afterGet(DataType dataType, MappingEnv env, Object source) {
        if (!(source instanceof String)) {
            throw dataAccessError(this, dataType, source, null);
        }
        final String[] array = ((String) source).split(",");
        final Set<TextEnum> set = _Collections.hashSetForSize(array.length);
        TextEnum textEnum;
        final Map<String, TextEnum> textEnumMap = this.textEnumMap;
        for (String text : array) {
            textEnum = textEnumMap.get(text);
            if (textEnum == null) {
                String m = String.format("%s unknown text[%s] instance.", this.enumClass.getName(), text);
                throw dataAccessError(this, dataType, source, new IllegalArgumentException(m));
            }
            set.add(textEnum);
        }
        return set;
    }

    @Override
    public MappingType compatibleFor(DataType dataType, Class<?> javaType, List<Class<?>> genericsTypeList)
            throws NoMatchMappingException {
        final Class<?> elementType;
        final MappingType type;
        if (genericsTypeList.size() != 1) {
            throw noMatchCompatibleMapping(this, dataType, javaType);
        } else if (javaType != Set.class) {
            throw noMatchCompatibleMapping(this, dataType, javaType);
        } else if (!Enum.class.isAssignableFrom((elementType = genericsTypeList.getFirst()))) {
            throw noMatchCompatibleMapping(this, dataType, javaType);
        } else if (CodeEnum.class.isAssignableFrom(elementType)) {
            throw noMatchCompatibleMapping(this, dataType, javaType);
        } else if (TextEnum.class.isAssignableFrom(elementType)) {
            type = fromSet(elementType);
        } else {
            throw noMatchCompatibleMapping(this, dataType, javaType);
        }
        return type;
    }

    @Override
    public Class<?> genericsType() {
        return this.enumClass;
    }

    @SuppressWarnings("unused")
    public Map<String, TextEnum> getTextEnumMap() {
        return this.textEnumMap;
    }


    @Override
    public int hashCode() {
        return Objects.hash(this.enumClass);
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof MySQLTextEnumSetType o) {
            match = o.enumClass == this.enumClass;
        } else {
            match = false;
        }
        return match;
    }

}
