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

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

final class DiscriminatorHandlerImpl implements DiscriminatorHandler {

    static DiscriminatorHandler create(Consumer<String> errorMsgConsumer) {
        return new DiscriminatorHandlerImpl(errorMsgConsumer);
    }


    private final Consumer<String> errorMsgConsumer;

    private final Map<String, Set<String>> enumConstMap = new HashMap<>();

    private final Map<String, String> domainToEnumMap = new HashMap<>();


    private DiscriminatorHandlerImpl(Consumer<String> errorMsgConsumer) {
        this.errorMsgConsumer = errorMsgConsumer;
    }

    @Override
    public void handle(String domainName, VariableElement field) {
        final TypeMirror typeMirror = field.asType();
        final Element element;
        if (!(typeMirror instanceof DeclaredType)) {
            discriminatorNonCodeNum(domainName, field);
        } else if ((element = ((DeclaredType) typeMirror).asElement()).getKind() == ElementKind.ENUM) {
            storeEnumConsts(domainName, (TypeElement) element);
        } else {
            discriminatorNonCodeNum(domainName, field);
        }
    }


    private void storeEnumConsts(final String domainName, final TypeElement enumElement) {
        final String className = MetaUtils.getClassName(enumElement);
        if (this.enumConstMap.get(className) != null) {
            this.domainToEnumMap.put(domainName, className);
            return;
        }
        final Set<String> enumConstSet = new HashSet<>();
        for (Element e : enumElement.getEnclosedElements()) {
            if (e.getKind() != ElementKind.ENUM_CONSTANT) {
                continue;
            }
            enumConstSet.add(e.getSimpleName().toString());
        }
        this.domainToEnumMap.put(domainName, className);
        this.enumConstMap.put(className, Set.copyOf(enumConstSet));
    }

    private void discriminatorNonCodeNum(final String domainName, final VariableElement field) {
        String m = String.format("Discriminator field %s.%s isn't enum.", domainName, field.getSimpleName());
        this.errorMsgConsumer.accept(m);
    }


}
