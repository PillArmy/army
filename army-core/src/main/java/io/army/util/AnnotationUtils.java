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


package io.army.util;


import io.army.lang.Nullable;
import io.army.meta.MetaException;
import io.army.struct.DefinedType;

public abstract class AnnotationUtils {

    private AnnotationUtils() {
    }


    @Nullable
    public static String getDefinedTypeName(Class<?> javaType) {
        if (javaType.isAnonymousClass()) {
            javaType = javaType.getSuperclass();
        }
        final DefinedType nno = javaType.getAnnotation(DefinedType.class);

        final String name;
        if (nno == null) {
            name = null;
        } else if (!_StringUtils.hasText(name = nno.name())) {
            String m = String.format("%s no text in %s", DefinedType.class.getName(), javaType.getName());
            throw new MetaException(m);
        } else if (_StringUtils.isCamelCase(name)) {
            String m = String.format("%s don't support CamelCase in %s", DefinedType.class.getName(), javaType.getName());
            throw new MetaException(m);
        }
        return name;
    }

}
