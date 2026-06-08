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

package io.army.meta;

final class DefaultSchemaName implements SchemaName {

    static DefaultSchemaName create(String schemaName) {
        return new DefaultSchemaName(schemaName);
    }


    private final String name;

    private DefaultSchemaName(String name) {
        this.name = name;
    }

    @Override
    public String objectName() {
        return this.name;
    }

    @Override
    public Class<?> javaType() {
        return String.class;
    }


    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        final boolean match;
        if (obj == this) {
            match = true;
        } else if (obj instanceof DefaultSchemaName o) {
            match = o.name.equals(this.name);
        } else {
            match = false;
        }
        return match;
    }


}
