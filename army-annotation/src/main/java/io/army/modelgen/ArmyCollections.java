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

package io.army.modelgen;

import java.util.HashMap;
import java.util.HashSet;

abstract class ArmyCollections {

    private ArmyCollections() {
        throw new UnsupportedOperationException();
    }



    static <K, V> HashMap<K, V> hashMapForSize(int size) {
        return new HashMap<>((int) (size / 0.75F));
    }

    static <E> HashSet<E> hashSetForSize(int size) {
        return new HashSet<>((int) (size / 0.75F));
    }





}
