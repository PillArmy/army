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

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.List;

final class CompositeMeta {

    static CompositeMeta of(TypeElement typeElement, TypeElement mappingType, List<VariableElement> fieldList) {
        return new CompositeMeta(typeElement, mappingType, fieldList);
    }

    final TypeElement typeElement;

    final TypeElement mappingType;

    final List<VariableElement> fieldList;

    private CompositeMeta(TypeElement typeElement, TypeElement mappingType, List<VariableElement> fieldList) {
        this.typeElement = typeElement;
        this.mappingType = mappingType;
        this.fieldList = List.copyOf(fieldList);
    }


}
