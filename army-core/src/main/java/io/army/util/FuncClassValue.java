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

import java.util.function.Function;

public final class FuncClassValue<T> extends ClassValue<T> {

    public static <T> ClassValue<T> create(Function<Class<?>, T> function) {
        return new FuncClassValue<>(function);
    }

    private final Function<Class<?>, T> function;

    private FuncClassValue(Function<Class<?>, T> function) {
        this.function = function;
    }

    @Override
    protected T computeValue(Class<?> type) {
        return this.function.apply(type);
    }

}
