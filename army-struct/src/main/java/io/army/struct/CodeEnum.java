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

package io.army.struct;


/// This interface is base interface of the enum that mapping to {@code io.army.mapping.CodeEnumType}.
/// 
/// Army will persist {@link #code()} to database table column not {@link Enum#ordinal()}.
/// If you want to persist {@link Enum#name()},then you should use {@code io.army.mapping.NameEnumType},
/// but never persist {@link Enum#ordinal()}.
/// @see LabelEnum
/// @since 0.6.0
public interface CodeEnum {


    /// @see Enum#name()
    String name();

    /// @return code that can representing this enum instance
    int code();


    /// @return enum alias
    default String alias() {
        return name();
    }

    default CodeEnum family() {
        return this;
    }


}
